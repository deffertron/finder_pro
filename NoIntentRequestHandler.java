package Handlers;

import Utils.JsonHelper;
import Utils.Util;
import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.*;
import static com.amazon.ask.request.Predicates.intentName;

public class NoIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("AMAZON.YesIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String roundTitle = "DON'T WORRY!";

            String title = "don't worry!";

            String message = "Ok, don't worry. We can continue the conversation. " +
                    "The all tasks has been terminated. " +
                    "So, please say the task name with keyword task name. ";

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
            session.remove("repeat_message");
            session.remove("repeat_re_prompt_message");

            AttributesManager attributesManager = input.getAttributesManager();

            Map<String, Object> document = getDirectivesResponse("file","simpleWithHeader.json");

            Map<String, Object> dataSource = getDirectivesResponse("class", JsonHelper.convertSimpleWithHeader(roundTitle,title,message));

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
                        .addDirective(documentDirective)
                        .build();
            }
            else
            {
                return fallbackResponse(input);
            }
        }
        else
        {
            return input.getResponseBuilder()
                    .withSpeech(Util.unSupportDeviceFallbackMessage)
                    .build();
        }
    }
}
