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
import static Utils.Util.getTaskNameResponse;
import static com.amazon.ask.request.Predicates.intentName;

public class TaskNameIntentRequestHandler implements RequestHandler
{

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName("TaskNameIntent"));
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
                        String taskName = intent.getSlots().get("task_name").getValue();

                        String sMessage = "Okay, we want s3 storage to handle the images. " +
                                "If you don't know what is s3 storage, simply say storage helper. " +
                                "Otherwise, say the bucket name,file name and file format one by one. " +
                                "If you don't know how to say the bucket name,file name and file format to me. " +
                                "Simply say file helper. " +
                                "Otherwise, say the bucket name with the keyword bucket name.";

                        if (taskName != null)
                        {
                            String rePrompt = "Otherwise, say the bucket name with the keyword bucket name.";

                            if (taskName.equals("image text extract"))
                            {
                                String roundTitle = "IMAGE TEXT<br>EXTRACT";

                                String message = "Okay, you choose image text extract task. " +
                                        "This task is useful to extract the handwritten or typed texts from the image. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("image object extract"))
                            {
                                String roundTitle = "IMAGE OBJECT<br>EXTRACT";

                                String message = "Okay, you choose image object extract task. " +
                                        "This task is useful to extract the objects from the image. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a text in image"))
                            {
                                String roundTitle = "FIND A TEXT<br>IN IMAGE";

                                String message = "Okay, you choose find a text in image task. " +
                                        "This task is useful to search some particular text is present or not in image. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a object in image"))
                            {
                                String roundTitle = "FIND A OBJECT<br>IN IMAGE";

                                String message = "Okay, you choose find a object in image task. " +
                                        "This task is useful to search some particular object is present or not in image. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a adult in image"))
                            {
                                String roundTitle = "FIND A ADULT<br>IN IMAGE";

                                String message = "Okay, you choose find a adult in image task. " +
                                        "This task is useful to detect any adult content is present or not image. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a face in image"))
                            {
                                String roundTitle = "FIND A FACE<br>IN IMAGE";

                                String message = "Okay, you choose find a face in image task. " +
                                        "This task is useful to search some particular face image is present or not in image. " +
                                        "Okay, we have two image files. " +
                                        "First one is source image file. " +
                                        "Second one is target image file. " +
                                        "The source image file must contain only one face. " +
                                        "And the target image file contains more than one face or one face, that not limit. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("video text extract"))
                            {
                                String roundTitle = "VIDEO TEXT<br>EXTRACT";

                                String message = "Okay, you choose video text extract task. " +
                                        "This task is useful to extract the texts from the video. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("video object extract"))
                            {
                                String roundTitle = "VIDEO OBJECT<br>EXTRACT";

                                String message = "Okay, you choose video object extract task. " +
                                        "This task is useful to extract the objects from the video. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a text in video"))
                            {
                                String roundTitle = "FIND A TEXT<br>IN VIDEO";

                                String message = "Okay, you choose find a text in video task. " +
                                        "This task is useful to search some particular text is present or not in video. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a object in video"))
                            {
                                String roundTitle = "FIND A OBJECT<br>IN VIDEO";

                                String message = "Okay, you choose find a object in video task. " +
                                        "This task is useful to search some particular object is present or not in video. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
                            }
                            else if (taskName.equals("find a adult in video"))
                            {
                                String roundTitle = "FIND A ADULT<br>IN VIDEO";

                                String message = "Okay, you choose find a adult in video task. " +
                                        "This task is useful to detect any adult content is present or not video. " +
                                        sMessage;

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getTaskNameResponse(input,roundTitle,taskName,message,rePrompt,session);
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
