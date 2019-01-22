package Handlers;

import Utils.Util;
import com.amazon.ask.attributes.AttributesManager;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.fallbackResponse;
import static Utils.Util.getDirectivesResponse;
import static com.amazon.ask.request.Predicates.intentName;

public class ShowTheTaskMenuIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("ShowTheTaskMenuIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            AttributesManager attributesManager = input.getAttributesManager();

            String taskMenuMessage = "This is the task menu. " +
                    "First one is image text extract. " +
                    "Second one is image object extract. " +
                    "Third one is find a text in image. " +
                    "Fourth one is find a object in image. " +
                    "Fifth one is find a adult in image. " +
                    "Sixth one is find a face in image. " +
                    "Seventh one is video text extract. " +
                    "Eight one is video object extract. " +
                    "Nine one is find a text in video. " +
                    "Tenth one is find a object in video. " +
                    "Eleventh one is find a adult in video. " +
                    "This are the tasks i will do. " +
                    "Choose one task name from above and say to me with the keyword task name. " +
                    "Example, task name image extract text. ";

            String taskMenuRePromptMessage = "Choose one task name from above and say to me with the keyword task name. " +
                    "Example, task name image extract text. ";

            Map<String, Object> document = getDirectivesResponse("file","taskMenuIntent.json");

            Map<String, Object> dataSource = getDirectivesResponse("file","taskMenuIntentTemplateData.json");

            if (document != null && dataSource != null)
            {
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
                session.put("repeat_message",taskMenuMessage);
                session.put("repeat_re_prompt_message",taskMenuRePromptMessage);

                attributesManager.setSessionAttributes(session);

                RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                        .withToken("taskMenuToken")
                        .withDocument(document)
                        .withDatasources(dataSource)
                        .build();

                return input.getResponseBuilder()
                        .withSpeech(taskMenuMessage)
                        .withReprompt(taskMenuRePromptMessage)
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
