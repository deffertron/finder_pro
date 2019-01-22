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
import com.google.cloud.videointelligence.v1p2beta1.TextAnnotation;
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

public class SearchTextIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("SearchTextIntent"));
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

                        String searchText = intent.getSlots().get("search_text").getValue();

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
                            if (taskName.equals("find a text in video"))
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
                                            .addFeatures(Feature.TEXT_DETECTION)
                                            .build();

                                    OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> future =
                                            client.annotateVideoAsync(request);

                                    AnnotateVideoResponse response = future.get(900, TimeUnit.SECONDS);

                                    StringBuilder voiceBuilder = new StringBuilder();

                                    StringBuilder screenBuilder = new StringBuilder();

                                    String intro = "Okay, analyse has been finished. Now i tell the minute and seconds one by one, where the search text " + searchText + " is detected in your video. ";

                                    voiceBuilder.append(intro);

                                    int count = 0;

                                    for (VideoAnnotationResults results :response.getAnnotationResultsList())
                                    {
                                        for (int i=0; i<results.getTextAnnotationsList().size(); i++)
                                        {
                                            TextAnnotation annotation = results.getTextAnnotations(i);

                                            TextSegment textSegment = annotation.getSegments(0);

                                            VideoSegment videoSegment = textSegment.getSegment();
                                            Duration startTimeOffset = videoSegment.getStartTimeOffset();
                                            Duration endTimeOffset = videoSegment.getEndTimeOffset();

                                            long startSeconds = startTimeOffset.getSeconds();

                                            long endSeconds = endTimeOffset.getSeconds();

                                            int startMin = (int) startSeconds / 60;
                                            int endMin = (int) endSeconds / 60;

                                            int startSec = (int) startSeconds % 60;
                                            int endSec = (int) endSeconds % 60;

                                            voiceBuilder
                                                    .append((i + 1)).append(". ").append("text ").append(annotation.getText().toLowerCase()).append(", ")
                                                    .append("starting time ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("ending time ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            screenBuilder.append(i + 1).append(". ").append("Text : ").append(annotation.getText().toLowerCase()).append(",")
                                                    .append("<br>")
                                                    .append("Starting time : ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("<br>")
                                                    .append("Ending time : ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            if (i == results.getTextAnnotationsList().size() - 1)
                                            {
                                                if (annotation.getText().toLowerCase().contains(searchText))
                                                {
                                                    voiceBuilder.append(" seconds, ");

                                                    screenBuilder.append(" seconds. ");

                                                    count++;
                                                }
                                            }
                                            else
                                            {
                                                if (annotation.getText().toLowerCase().contains(searchText))
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
                                        String roundTitle = "FIND A<br>TEXT";

                                        String title = "find a text";

                                        String videoTextExtractMessage = voiceBuilder.toString() + "Okay, this is are the detected texts using your search text " + searchText +" in your video. If you want to perform another task simply say the task name with keyword task name.";

                                        String videoTextExtractRePromptMessage = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String, Object> videoTextExtractDocument = getDirectivesResponse("file","taskResult.json");

                                        Map<String, Object> videoTextExtractDataSource = getDirectivesResponse("class", JsonHelper.convertSimpleWithHeader(roundTitle,title,screenBuilder.toString()));

                                        if (videoTextExtractDocument != null && videoTextExtractDataSource != null)
                                        {
                                            Map<String,Object> session = new HashMap<>();

                                            session.remove("task_name");
                                            session.remove("bucket_name");
                                            session.remove("file_name");
                                            session.remove("file_format");
                                            session.remove("search_text");
                                            session.put("repeat_message",videoTextExtractMessage);
                                            session.put("repeat_re_prompt_message",videoTextExtractRePromptMessage);

                                            input.getAttributesManager().setSessionAttributes(session);

                                            RenderDocumentDirective documentDirective = RenderDocumentDirective.builder()
                                                    .withToken("taskResultToken")
                                                    .withDocument(videoTextExtractDocument)
                                                    .withDatasources(videoTextExtractDataSource)
                                                    .build();

                                            return input.getResponseBuilder()
                                                    .withSpeech(videoTextExtractMessage)
                                                    .withReprompt(videoTextExtractRePromptMessage)
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
                                        String roundTitle = "FIND A<br>TEXT IN VIDEO";

                                        String title = "find a text in video";

                                        String message =  "Okay, analyse has been finished. Sorry, i could not find any text using your search text " + searchText + " in your video. If you want to perform another task simply say the task name with keyword task name.";

                                        String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String,Object> session = new HashMap<>();

                                        session.remove("task_name");
                                        session.remove("bucket_name");
                                        session.remove("file_name");
                                        session.remove("file_format");
                                        session.remove("search_text");
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
                            else if (taskName.equals("find a text in image"))
                            {
                                try {
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
                                    com.google.cloud.vision.v1.Feature feat = com.google.cloud.vision.v1.Feature.newBuilder().setType(com.google.cloud.vision.v1.Feature.Type.DOCUMENT_TEXT_DETECTION).build();
                                    AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
                                    requests.add(request);

                                    try (ImageAnnotatorClient client = ImageAnnotatorClient.create())
                                    {
                                        BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
                                        List<AnnotateImageResponse> responses = response.getResponsesList();
                                        client.close();

                                        int count = 0;

                                        for (AnnotateImageResponse res : responses)
                                        {
                                            if (res.hasError())
                                            {
                                                return fallbackResponse(input);
                                            }
                                            else
                                            {
                                                com.google.cloud.vision.v1.TextAnnotation annotation = res.getFullTextAnnotation();

                                                if (annotation.getText().equals(searchText))
                                                {
                                                    count++;
                                                }
                                            }
                                        }

                                        String message;

                                        if (count == 1)
                                        {
                                            message = "Okay, analyse has been finished. The search text " +
                                                    searchText +
                                                    " has found " + count + " time in your image file." +
                                                    "If you want to perform another task simply say the task name with keyword task name.";
                                        }
                                        else if (count > 1)
                                        {
                                            message = "Okay, analyse has been finished. The search text " +
                                                    searchText +
                                                    " has found " + count + " times in your image file." +
                                                    "If you want to perform another task simply say the task name with keyword task name.";
                                        }
                                        else
                                        {
                                            message = "Okay, analyse has been finished.  Sorry, i could not find search text " +
                                                    searchText +
                                                    " in your image file." +
                                                    "If you want to perform another task simply say the task name with keyword task name.";
                                        }

                                        String roundTitle = "FIND A TEXT<br>IN IMAGE";

                                        String title = "find a text in image";

                                        String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String,Object> session = new HashMap<>();

                                        session.remove("task_name");
                                        session.remove("bucket_name");
                                        session.remove("file_name");
                                        session.remove("file_format");
                                        session.remove("search_text");
                                        session.put("repeat_message",message);
                                        session.put("repeat_re_prompt_message",message);

                                        return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                    }
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
