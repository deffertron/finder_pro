package Handlers;

import Utils.Util;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.getSimpleResponse;
import static com.amazon.ask.request.Predicates.requestType;

public class LaunchIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(requestType(LaunchRequest.class));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String message = "Hi, welcome to finder pro. " +
                    "My work is to analyse the image and video files. " +
                    "After analyse completed, i say results of the task. " +
                    "If you don't know what tasks are i will do, simply say show the task menu. " +
                    "Otherwise say the task name with keyword task name. ";

            String rePrompt = "If you don't know what tasks are i will do, simply say show the task menu. " +
                    "Otherwise say the task name with keyword task name. ";

            String title = "welcome!";

            Map<String,Object> session = new HashMap<>();

            session.put("repeat_message",message);
            session.put("repeat_re_prompt_message",rePrompt);

            return getSimpleResponse(input,title,title,message,message,rePrompt,session);
        }
        else
        {
            return input.getResponseBuilder()
                    .withSpeech(Util.unSupportDeviceFallbackMessage)
                    .build();
        }
    }
}
