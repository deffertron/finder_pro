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

public class GetTargetFileNameIntentRequestHandler implements RequestHandler
{

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("GetTargetFileNameIntent"));
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

                        String targetBucketName = input.getAttributesManager().getSessionAttributes().getOrDefault("target_bucket_name",null).toString();

                        String targetFileName = intent.getSlots().get("target_file_name").getValue().toLowerCase();

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
                        else if (targetBucketName == null)
                        {
                            String roundTitle = "TARGET BUCKET<br>NAME EMPTY";

                            String title = "target bucket name empty";

                            String message = "Sorry, i could not understand your voice. So, please say the target bucket name again with the keyword target bucket name.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("file_format",fileFormat);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else if (targetFileName.equals("") || targetFileName.equals("null"))
                        {
                            String roundTitle = "TARGET FILE<br>NAME EMPTY";

                            String title = "target file name empty";

                            String message = "Sorry, i could not understand your voice. So, please say the file name again with the keyword file name.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("file_format",fileFormat);
                            session.put("target_bucket_name",targetBucketName);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else
                        {
                            String title = "CONFIRM!";

                            String message = "Confirm " + targetFileName + " is your target file name. " +
                                    "If yes, say yes this is my target file name. " +
                                    "If no, say no this is not my target file name. ";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("file_format",fileFormat);
                            session.put("target_bucket_name",targetBucketName);
                            session.put("target_file_name",targetFileName);
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
