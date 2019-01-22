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

public class StorageHelperIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("StorageHelperIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input)
    {
        if (Util.supportsApl(input))
        {
            String roundTitle = "STORAGE<br>HELPER!";

            String title = "storage helper!";

            String message = "Okay, s3 storage is a cloud storage of amazon aws. " +
                    "Okay, i say a steps to how to store your files in amazon aws s3 storage. " +
                    "First step, go to amazon aws and create your account. " +
                    "And then second step is, type s3 in the search box and choose s3 (scalable storage in the cloud). " +
                    "And then third step is, click create bucket and type your bucket name and choose a region in the name and region tab." +
                    "And then click next and in the configure options tab uncheck all checked options. " +
                    "And click next and set manage system permissions as 'grant amazon s3 log delivery group write access to this bucket' in the set permissions tab. " +
                    "And click next and click create bucket. " +
                    "Now your bucket has been created. " +
                    "Click on your bucket name and click upload, choose your file from your laptop or pc. " +
                    "And set manage public permission as 'grant public read access to this object(s)' in the set permissions tab. "+
                    "And click next and again click next and click upload. " +
                    "Once your file is uploaded get the url of the storage path by clicking on the file name. " +
                    "Once you got bucket name,file name and file format come back and say it to me. " +
                    "If you don't know, how to say a bucket name,file name and file format to me. " +
                    "Simply, say ' file helper '. " +
                    "Otherwise, say the task name with the keyword task name.";

            String rePrompt = "Otherwise, say the task name with the keyword task name.";

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
