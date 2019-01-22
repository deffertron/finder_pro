package Utils;

import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.SupportedInterfaces;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Util
{
    public static String fallbackMessage = "Sorry, some error has been occurred or some internal problem occurred. " +
            "Or, i could not understand your voice. " +
            "So, please start freshly with the task name. " ;

    public static String unSupportDeviceFallbackMessage = "Sorry, i could not support in this device. " +
            "So please try it on an Echo Show, Echo Spot or a Fire TV device.";

    private static Map<String, Object> fallbackDocument = getDirectivesResponse("file","fallbackIntent.json");

    private static Map<String, Object> fallbackDataSource = getDirectivesResponse("file","fallbackIntentTemplateData.json");

    public static boolean supportsApl(HandlerInput input)
    {
        SupportedInterfaces supportedInterfaces = input.getRequestEnvelope().getContext().getSystem().getDevice().getSupportedInterfaces();
        return supportedInterfaces.getAlexaPresentationAPL() != null;
    }

    private static Optional<Response> getFallbackResponse(HandlerInput input, Map<String, Object> fallbackDocument, Map<String, Object> fallbackDataSource)
    {
        AttributesManager attributesManager = input.getAttributesManager();

        Map<String,Object> session = new HashMap<>();

        session.remove("task_name");
        session.remove("bucket_name");
        session.remove("file_name");
        session.remove("file_format");
        session.remove("target_bucket_name");
        session.remove("target_file_name");
        session.remove("target_file_format");
        session.remove("search_text");
        session.remove("search_object");
        session.put("repeat_message",fallbackMessage);
        session.put("repeat_re_prompt_message",fallbackMessage);

        attributesManager.setSessionAttributes(session);

        RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                .withToken("fallbackToken")
                .withDocument(fallbackDocument)
                .withDatasources(fallbackDataSource)
                .build();

        return input.getResponseBuilder()
                .withSpeech(fallbackMessage)
                .withReprompt(fallbackMessage)
                .addDirective(documentDirective)
                .build();
    }

    public static Optional<Response> fallbackResponse(HandlerInput input)
    {
        if (fallbackDocument != null && fallbackDataSource != null)
        {
            return Util.getFallbackResponse(input,fallbackDocument,fallbackDataSource);
        }
        else
        {
            return input.getResponseBuilder()
                    .withSpeech(fallbackMessage)
                    .build();
        }
    }

    public static Map<String,Object> getDirectivesResponse(String type, String fileName)
    {
        ObjectMapper mapper = new ObjectMapper();

        TypeReference<HashMap<String, Object>> documentMapType = new TypeReference<HashMap<String, Object>>() {};

        try
        {
            if (type.equals("file"))
            {
                return mapper.readValue(new File(fileName), documentMapType);
            }
            else
            {
                return mapper.readValue(fileName, documentMapType);
            }

        }
        catch (IOException e)
        {
            return null;
        }
    }

    public static Optional<Response> getSimpleResponse(HandlerInput input,String roundTitle, String title, String message, String screenMessage, String prompt, Map<String,Object> session)
    {
        AttributesManager attributesManager = input.getAttributesManager();

        Map<String, Object> document = getDirectivesResponse("file","simpleWithHeader.json");

        Map<String, Object> dataSource = getDirectivesResponse("class",JsonHelper.convertSimpleWithHeader(roundTitle,title,screenMessage));

        if (document != null && dataSource != null)
        {
            attributesManager.setSessionAttributes(session);

            RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                    .withToken("simpleToken")
                    .withDocument(document)
                    .withDatasources(dataSource)
                    .build();

            return input.getResponseBuilder()
                    .withSpeech(message)
                    .withReprompt(prompt)
                    .addDirective(documentDirective)
                    .build();
        }
        else
        {
            return fallbackResponse(input);
        }
    }

    public static Optional<Response> getTaskNameResponse(HandlerInput input,String roundTitle, String title, String message, String prompt, Map<String,Object> session)
    {
        AttributesManager attributesManager = input.getAttributesManager();

        Map<String, Object> document = getDirectivesResponse("file","taskNameIntent.json");

        Map<String, Object> dataSource = getDirectivesResponse("class", JsonHelper.convertTaskName(roundTitle,title,message));

        if (document != null && dataSource != null)
        {
            attributesManager.setSessionAttributes(session);

            RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                    .withToken("simpleToken")
                    .withDocument(document)
                    .withDatasources(dataSource)
                    .build();

            return input.getResponseBuilder()
                    .withSpeech(message)
                    .withReprompt(prompt)
                    .addDirective(documentDirective)
                    .build();
        }
        else
        {
            return fallbackResponse(input);
        }
    }
}
