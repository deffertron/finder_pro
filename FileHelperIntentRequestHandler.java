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

public class FileHelperIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("FileHelperIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String roundTitle = "FILE<br>HELPER!";

            String title = "file helper!";

            String message = "Okay, the file path url of your file will be like ' https://s3.amazonaws.com/imagefiber/L1.PNG '. " +
                    "Here 'https://s3.amazonaws.com' is a host name of the web page. " +
                    "You don't say the host name, because i can automatically recognize the host name. " +
                    "Only say the bucket name,file name and file format without the hostname and the forward slashes. " +
                    "For example, i ask : say the bucket name. " +
                    "Now you say the bucket name with the keyword bucket name. " +
                    "Example \"bucket name 'name of your bucket'\", here bucket name is imagefiber. " +
                    "And i ask : say the file name.  " +
                    "Now you say the file name with the keyword file name. " +
                    "Example \"filename 'name of your file name'\", here file name is L1. " +
                    "And then finally i ask the file format. " +
                    "Now you say the format of your file with the keyword file format. " +
                    "Example \"file format 'format of your file'\", here format is PNG. " +
                    "I hope you understand the instructions. " +
                    "And also another thing, here is supports fue file formats only. " +
                    "That file formats are png and jpg or jpeg in image files. " +
                    "And mp4,mov,mpeg4 and avi in video files. " +
                    "Now you ready to say the file path url, simply say the task name with the keyword task name.";

            String rePrompt = "Now you ready to say the file path url, simply say the task name with the keyword task name.";

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
