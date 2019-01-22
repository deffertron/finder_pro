package Handlers;

import Utils.JsonHelper;
import Utils.Util;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1p2beta1.*;
import com.google.cloud.videointelligence.v1p2beta1.Feature;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static Utils.Util.*;
import static com.amazon.ask.request.Predicates.intentName;

public class SearchObjectIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("SearchObjectIntent"));
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

                        String searchObject = intent.getSlots().get("search_object").getValue();

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
                            if (taskName.equals("find a object in video"))
                            {
                                try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create())
                                {
                                    String hostName = "https://s3.amazonaws.com/";

                                    String fileUrl = hostName + bucketName + "/" + fileName + "." + fileFormat;

                                    URL url = new URL(fileUrl);

                                    InputStream in = new BufferedInputStream(url.openStream());

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                                    byte[] buf = new byte[1024];

                                    int n;

                                    while (-1 != (n = in.read(buf))) {
                                        byteArrayOutputStream.write(buf, 0, n);
                                    }

                                    byteArrayOutputStream.close();
                                    in.close();

                                    byte[] responseBytes = byteArrayOutputStream.toByteArray();

                                    AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
                                            .setInputContent(ByteString.copyFrom(responseBytes))
                                            .addFeatures(Feature.OBJECT_TRACKING)
                                            .setLocationId("us-east1")
                                            .build();

                                    OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> future =
                                            client.annotateVideoAsync(request);

                                    AnnotateVideoResponse response = future.get(900, TimeUnit.SECONDS);

                                    StringBuilder voiceBuilder = new StringBuilder();

                                    StringBuilder screenBuilder = new StringBuilder();

                                    String intro = "Okay, analyse has been finished. Now i tell the minute and seconds one by one, where the search object " + searchObject + " is detected in your video. ";

                                    voiceBuilder.append(intro);

                                    int count = 0;

                                    for (VideoAnnotationResults results : response.getAnnotationResultsList())
                                    {
                                        for (int i=0; i<results.getObjectAnnotationsList().size(); i++)
                                        {
                                            ObjectTrackingAnnotation annotation = results.getObjectAnnotations(i);

                                            Entity entity = annotation.getEntity();

                                            VideoSegment videoSegment = annotation.getSegment();
                                            Duration startTimeOffset = videoSegment.getStartTimeOffset();
                                            Duration endTimeOffset = videoSegment.getEndTimeOffset();

                                            long startSeconds = startTimeOffset.getSeconds();

                                            long endSeconds = endTimeOffset.getSeconds();

                                            int startMin = (int) startSeconds / 60;
                                            int endMin = (int) endSeconds / 60;

                                            int startSec = (int) startSeconds % 60;
                                            int endSec = (int) endSeconds % 60;

                                            voiceBuilder
                                                    .append((i + 1)).append(". ").append("object name ").append(entity.getDescription().toLowerCase()).append(", ")
                                                    .append("starting time ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("ending time ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            screenBuilder.append(i + 1).append(". ").append("Object name : ").append(entity.getDescription().toLowerCase()).append(",")
                                                    .append("<br>")
                                                    .append("Starting time : ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("<br>")
                                                    .append("Ending time : ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            if (i == results.getTextAnnotationsList().size() - 1)
                                            {
                                                if (entity.getDescription().toLowerCase().contains(searchObject))
                                                {
                                                    voiceBuilder.append(" seconds. ");

                                                    screenBuilder.append(" seconds. ");

                                                    count++;
                                                }
                                            }
                                            else
                                            {
                                                if (entity.getDescription().toLowerCase().contains(searchObject))
                                                {
                                                    voiceBuilder.append(" seconds, ");

                                                    screenBuilder.append(" seconds,<br>");

                                                    count++;
                                                }
                                            }
                                        }
                                    }

                                    if (count > 0)
                                    {
                                        String roundTitle = "FIND A<br>OBJECT IN VIDEO";

                                        String title = "find a object in video";

                                        String searchObjectMessage = voiceBuilder.toString() + "Okay, this is are the detected objects using your search object " + searchObject +" in your video. If you want to perform another task simply say the task name with keyword task name.";

                                        String searchObjectRePromptMessage = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String, Object> searchObjectExtractDocument = getDirectivesResponse("file","taskResult.json");

                                        Map<String, Object> searchObjectExtractDataSource = getDirectivesResponse("class", JsonHelper.convertSimpleWithHeader(roundTitle,title,screenBuilder.toString()));

                                        if (searchObjectExtractDocument != null && searchObjectExtractDataSource != null)
                                        {
                                            Map<String,Object> session = new HashMap<>();

                                            session.remove("task_name");
                                            session.remove("bucket_name");
                                            session.remove("file_name");
                                            session.remove("file_format");
                                            session.remove("search_object");
                                            session.put("repeat_message",searchObjectMessage);
                                            session.put("repeat_re_prompt_message",searchObjectRePromptMessage);

                                            input.getAttributesManager().setSessionAttributes(session);

                                            RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                                                    .withToken("taskResultToken")
                                                    .withDocument(searchObjectExtractDocument)
                                                    .withDatasources(searchObjectExtractDataSource)
                                                    .build();

                                            return input.getResponseBuilder()
                                                    .withSpeech(searchObjectMessage)
                                                    .withReprompt(searchObjectRePromptMessage)
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
                                        String roundTitle = "FIND A<br>OBJECT IN VIDEO";

                                        String title = "find a object in video";

                                        String message =  "Okay, analyse has been finished. Sorry, i could not find any object using your search object " + searchObject + " in your video. If you want to perform another task simply say the task name with keyword task name.";

                                        String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String,Object> session = new HashMap<>();

                                        session.remove("task_name");
                                        session.remove("bucket_name");
                                        session.remove("file_name");
                                        session.remove("file_format");
                                        session.remove("search_object");
                                        session.put("repeat_message",message);
                                        session.put("repeat_re_prompt_message",message);

                                        return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                    }
                                }
                                catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
                                {
                                    return fallbackResponse(input);
                                }
                            }
                            else if (taskName.equals("find a object in image"))
                            {
                                try (ImageAnnotatorClient client = ImageAnnotatorClient.create())
                                {
                                    String imageUrl = "https://s3.amazonaws.com/" + bucketName + "/" + fileName + "." + fileFormat;

                                    URL url = new URL(imageUrl);

                                    InputStream in = new BufferedInputStream(url.openStream());

                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                                    byte[] buf = new byte[1024];

                                    int n;

                                    while (-1 != (n = in.read(buf))) {
                                        byteArrayOutputStream.write(buf, 0, n);
                                    }

                                    byteArrayOutputStream.close();
                                    in.close();

                                    byte[] responseBytes = byteArrayOutputStream.toByteArray();

                                    List<AnnotateImageRequest> requests = new ArrayList<>();

                                    ByteString imgBytes = ByteString.copyFrom(responseBytes);

                                    Image img = Image.newBuilder().setContent(imgBytes).build();

                                    AnnotateImageRequest request =
                                            AnnotateImageRequest.newBuilder()
                                                    .addFeatures(com.google.cloud.vision.v1.Feature.newBuilder().setType(com.google.cloud.vision.v1.Feature.Type.OBJECT_LOCALIZATION))
                                                    .setImage(img)
                                                    .build();
                                    requests.add(request);

                                    BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                                    List<AnnotateImageResponse> responses = response.getResponsesList();
                                    client.close();

                                    int count = 0;

                                    for (AnnotateImageResponse res : responses)
                                    {
                                        for (int i=0; i<res.getLocalizedObjectAnnotationsList().size(); i++)
                                        {
                                            LocalizedObjectAnnotation entity = res.getLocalizedObjectAnnotations(i);

                                            if (entity.getName().equals(searchObject))
                                            {
                                                count++;
                                            }
                                        }
                                    }

                                    String message;

                                    if (count == 1)
                                    {
                                        message = "Okay, analyse has been finished. The search object " +
                                                searchObject +
                                                " has found " + count + " time in your image file." +
                                                "If you want to perform another task simply say the task name with keyword task name.";
                                    }
                                    else if (count > 1)
                                    {
                                        message = "Okay, analyse has been finished. The search object " +
                                                searchObject +
                                                " has found " + count + " times in your image file." +
                                                "If you want to perform another task simply say the task name with keyword task name.";
                                    }
                                    else
                                    {
                                        message = "Okay, analyse has been finished. Sorry, i could not find search object " +
                                                searchObject +
                                                " in your image file." +
                                                "If you want to perform another task simply say the task name with keyword task name.";
                                    }

                                    String roundTitle = "FIND A<br>OBJECT IN IMAGE";

                                    String title = "find a object in image";

                                    String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.remove("search_object");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                }
                                catch (IOException e)
                                {
                                    return fallbackResponse(input);
                                }
                            }
                            else
                            {
                                return fallbackResponse(input);
                            }
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
