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

public class StopOrCancelIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("AMAZON.StopIntent")) || input.matches(intentName("AMAZON.CancelIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String roundTitle = "STOP OR<br>CANCEL!";

            String title = "stop or cancel!";

            String message = "Would you like to stop or cancel all the tasks and conversations?. " +
                    "If yes say yes. " +
                    "If no say no. ";

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
            session.put("repeat_re_prompt_message",message);

            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
        }
        else
        {
            return input.getResponseBuilder()
                    .withSpeech(Util.unSupportDeviceFallbackMessage)
                    .build();
        }
    }
}
