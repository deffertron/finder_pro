package Handlers;

import Utils.Util;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.fallbackResponse;
import static Utils.Util.getSimpleResponse;
import static com.amazon.ask.request.Predicates.intentName;

public class ThisIsMyFileFormatIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("ThisIsMyFileFormatIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            if (input.getRequestEnvelope().getRequest().getType().equals("IntentRequest"))
            {
                Intent intent = ((IntentRequest)input.getRequestEnvelope().getRequest()).getIntent();

                if (intent != null)
                {
                    String intentName = intent.getName();

                    if (intentName != null)
                    {
                        String taskName = input.getAttributesManager().getSessionAttributes().getOrDefault("task_name",null).toString();

                        String bucketName = input.getAttributesManager().getSessionAttributes().getOrDefault("bucket_name",null).toString();

                        String fileName = input.getAttributesManager().getSessionAttributes().getOrDefault("file_name",null).toString();

                        String fileFormat = input.getAttributesManager().getSessionAttributes().getOrDefault("file_format",null).toString();

                        if (taskName == null)
                        {
                            String roundTitle = "TASK NAME<br>EMPTY";

                            String title = "task name empty";

                            String message = "Sorry, i could not find your task name. So please, first say the task name with the keyword task name.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else if (bucketName == null)
                        {
                            String roundTitle = "BUCKET NAME<br>EMPTY";

                            String title = "bucket name empty";

                            String message = "Sorry, i could not find your bucket name. So please, first say the bucket name with the keyword bucket name.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else if (fileName == null)
                        {
                            String roundTitle = "FILE NAME<br>EMPTY";

                            String title = "file name empty";

                            String message = "Sorry, i could not find your file name. So please, first say the file name with the keyword file name.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else if (fileFormat == null)
                        {
                            String roundTitle = "FILE FORMAT<br>EMPTY";

                            String title = "file format empty";

                            String message = "Sorry, i could not find your file format. So please, first say the file format with the keyword file format.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else
                        {
                            String hostName = "https://s3.amazonaws.com/";

                            String fileUrl = hostName + bucketName + "/" + fileName + "." + fileFormat;

                            String title = "CONFIRM!";

                            String message = "Okay, now confirm this " + fileUrl + " is your file url. " +
                                    "If yes, say yes this is my file url. " +
                                    "Or if no, say no this is not my file url. ";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("file_format",fileFormat);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,title,title,message,message,message,session);
                        }

                    }
                    else
                    {
                        return fallbackResponse(input);
                    }
                }
                else
                {
                    return fallbackResponse(input);
                }
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
