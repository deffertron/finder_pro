package Handlers;

import Utils.Util;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.getSimpleResponse;
import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("AMAZON.HelpIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String roundTitle = "HELP!";

            String title = "help!";

            String message = "Hi, it's a pleasure to help to you. " +
                    "My work is to analyse the image and video files. " +
                    "After analyse completed, i say results of the task. " +
                    "If you don't know what tasks are i will do, simply say show the task menu. " +
                    "Otherwise say the task name with keyword task name. ";

            String rePrompt = "If you don't know what tasks are i will do, simply say show the task menu. " +
                    "Otherwise say the task name with keyword task name. ";

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
            session.put("repeat_message",message);
            session.put("repeat_re_prompt_message",rePrompt);

            return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
        }
        else
        {
            return input.getResponseBuilder()
                    .withSpeech(Util.unSupportDeviceFallbackMessage)
                    .build();
        }
    }
}
