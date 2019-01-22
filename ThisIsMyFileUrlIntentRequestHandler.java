package Handlers;

import Utils.Util;
import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.Response;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1p2beta1.*;
import com.google.cloud.videointelligence.v1p2beta1.Feature;
import com.google.cloud.videointelligence.v1p2beta1.TextAnnotation;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
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

public class ThisIsMyFileUrlIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("ThisIsMyFileUrlIntent"));
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
                            if (taskName.equals("image text extract"))
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

                                        StringBuilder stringBuilder = new StringBuilder();

                                        for (AnnotateImageResponse res : responses)
                                        {
                                            if (res.hasError())
                                            {
                                                return fallbackResponse(input);
                                            }
                                            else
                                            {
                                                com.google.cloud.vision.v1.TextAnnotation annotation = res.getFullTextAnnotation();

                                                stringBuilder.append(annotation.getText());
                                            }
                                        }

                                        String roundTitle = "IMAGE TEXT<br>EXTRACT";

                                        String title = "image text extract";

                                        String message = "Okay, analyse has been finished. Now i say the detected texts in your image file. " +
                                                stringBuilder.toString() +
                                                ". Okay, this are the detected texts in your image file. " +
                                                "If you want to perform another task simply say the task name with keyword task name.";

                                        String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                        Map<String,Object> session = new HashMap<>();

                                        session.remove("task_name");
                                        session.remove("bucket_name");
                                        session.remove("file_name");
                                        session.remove("file_format");
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
                            else if (taskName.equals("image object extract"))
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

                                    StringBuilder stringBuilder = new StringBuilder();

                                    for (AnnotateImageResponse res : responses)
                                    {
                                        for (int i=0; i<res.getLocalizedObjectAnnotationsList().size(); i++)
                                        {
                                            LocalizedObjectAnnotation entity = res.getLocalizedObjectAnnotations(i);

                                            if (i == res.getLocalizedObjectAnnotationsList().size() - 1)
                                            {
                                                stringBuilder.append(i+1).append(". ").append(entity.getName()).append(".");
                                            }
                                            else
                                            {
                                                stringBuilder.append(i+1).append(". ").append(entity.getName()).append(", ");
                                            }
                                        }
                                    }

                                    String roundTitle = "IMAGE OBJECT<br>EXTRACT";

                                    String title = "image object extract";

                                    String message = "Okay, analyse has been finished. Now i say the detected object names in your image file. " +
                                            stringBuilder.toString() +
                                            ". Okay, this are the detected objects in your image file. " +
                                            "If you want to perform another task simply say the task name with keyword task name.";

                                    String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                }
                                catch (IOException e)
                                {
                                    return fallbackResponse(input);
                                }
                            }
                            else if (taskName.equals("find a text in image"))
                            {
                                String roundTitle = "FIND A TEXT<br>IN IMAGE";

                                String title = "find a text in image";

                                String message = "Okay, now say the text you want to search in your image with keyword search text.";

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("bucket_name",bucketName);
                                session.put("file_name",fileName);
                                session.put("file_format",fileFormat);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                            }
                            else if (taskName.equals("find a adult in image"))
                            {
                                String image = fileName + "." + fileFormat;

                                AmazonRekognition rekognitionClient = getAmazonRekognitionClient();

                                try
                                {
                                    DetectModerationLabelsRequest request = new DetectModerationLabelsRequest()
                                            .withImage(new com.amazonaws.services.rekognition.model.Image().withS3Object(new S3Object().withName(image).withBucket(bucketName)));

                                    StringBuilder stringBuilder = new StringBuilder();

                                    DetectModerationLabelsResult result = rekognitionClient.detectModerationLabels(request);

                                    List<ModerationLabel> labels = result.getModerationLabels();

                                    stringBuilder.append("Okay, find a adult in image task is useful to detects explicit or suggestive adult content and unsafe in your image, and provides confidence scores. I completed the analysis on your image ").append(image).append(". ");

                                    if (labels.size() == 0)
                                    {
                                        stringBuilder.append("But i could not find any labels in your image. " +
                                                "So the image does not contains any explicit or suggestive adult content and the image is safe. " +
                                                "Okay, if you like to perform another task simply say the task name with keyword task name. ");
                                    }
                                    else
                                    {
                                        stringBuilder.append("Now, i give the name of the labels and also their confidence level. ");

                                        for (int i = 0; i < labels.size(); i++)
                                        {
                                            if (i == labels.size() - 1)
                                            {
                                                stringBuilder
                                                        .append(i + 1)
                                                        .append(". ")
                                                        .append("Name : ").append(labels.get(i).getName())
                                                        .append(",")
                                                        .append("\nConfidence : ").append(labels.get(i).getConfidence().toString()).append("% .");
                                            }
                                            else
                                            {
                                                stringBuilder
                                                        .append(i + 1)
                                                        .append(". ")
                                                        .append("Name : ").append(labels.get(i).getName())
                                                        .append(",")
                                                        .append("\nConfidence : ").append(labels.get(i).getConfidence().toString()).append("% ,");
                                            }
                                        }

                                        stringBuilder.append(". Okay, this are the detected adult contents in your image. If you like to perform another task simply say the task name with keyword task name.");
                                    }

                                    String roundTitle = "FIND A ADULT<br>IN IMAGE";

                                    String title = "find a adult in image";

                                    String message = stringBuilder.toString();

                                    String rePrompt = "If you like to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                }
                                catch (AmazonRekognitionException e)
                                {
                                    String roundTitle = "FIND A ADULT<br>IN IMAGE";

                                    String title = "find a adult in image";

                                    String message = "Unfortunately, i could not perform the image moderation task on your image now. " +
                                            "Because i could not find your image or may be some error has been occurred. " +
                                            "And also the task has been terminated. " +
                                            "Sorry for that. " +
                                            "Okay, please say a another task name to start a new task with the keyword task name. ";

                                    String rePrompt = "Okay, please say a another task name to start a new task with the keyword task name. ";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                                }
                            }
                            else if (taskName.equals("find a face in image"))
                            {
                                String roundTitle = "FIND A FACE<br>IN IMAGE";

                                String title = "find a face";

                                String message = "Okay, now say the target image file bucket name, file name and file format one by one. Okay, now first say the target image file bucket name with the keyword target bucket name.";

                                String rePrompt = "Okay, now first say the target image file bucket name with the keyword target bucket name.";

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("bucket_name",bucketName);
                                session.put("file_name",fileName);
                                session.put("file_format",fileFormat);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                            }
                            else if (taskName.equals("video text extract"))
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

                                    String intro = "Okay, analyse has been finished. Now i tell the detected texts from the video and also i tell which minutes and seconds the texts has been detected. Okay now i tell text one by one. ";

                                    voiceBuilder.append(intro);

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
                                                    .append(i+1).append(". ").append("text ").append(annotation.getText().toLowerCase()).append(", ")
                                                    .append("starting time ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("ending time ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            screenBuilder.append(i+1).append(". ").append("Text : ").append(annotation.getText().toLowerCase()).append(",")
                                                    .append("<br>")
                                                    .append("Starting time : ").append(startMin)
                                                    .append(" minute ").append(startSec).append(" seconds, ")
                                                    .append("<br>")
                                                    .append("Ending time : ").append(endMin).append(" minute ")
                                                    .append(endSec);

                                            if (i == results.getTextAnnotationsList().size() - 1)
                                            {
                                                voiceBuilder.append(" seconds. ");

                                                screenBuilder.append(" seconds. ");
                                            }
                                            else
                                            {
                                                voiceBuilder.append(" seconds, ");

                                                screenBuilder.append(" seconds,<br>");
                                            }
                                        }
                                    }

                                    String roundTitle = "VIDEO TEXT<br>EXTRACT";

                                    String title = "video text extract";

                                    String message = voiceBuilder.toString() + "Okay, this is are the detected texts in your video. If you want to perform another task simply say the task name with keyword task name.";

                                    String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,screenBuilder.toString(),rePrompt,session);
                                }
                                catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
                                {
                                    return fallbackResponse(input);
                                }
                            }
                            else if (taskName.equals("video object extract"))
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

                                    String intro = "Okay, analyse has been finished. Now i tell the detected objects from the video and also i tell which minutes and seconds the objects has been detected. Okay now i tell object names one by one. ";

                                    voiceBuilder.append(intro);

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
                                                voiceBuilder.append(" seconds. ");

                                                screenBuilder.append(" seconds. ");
                                            }
                                            else
                                            {
                                                voiceBuilder.append(" seconds, ");

                                                screenBuilder.append(" seconds,<br>");
                                            }
                                        }
                                    }

                                    String roundTitle = "VIDEO OBJECT<br>EXTRACT";

                                    String title = "video object extract";

                                    String message =  voiceBuilder.toString() + "Okay, this is are the detected objects in your video. If you want to perform another task simply say the task name with keyword task name.";

                                    String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,screenBuilder.toString(),rePrompt,session);
                                }
                                catch (IOException | InterruptedException | ExecutionException | TimeoutException e)
                                {
                                    return fallbackResponse(input);
                                }
                            }
                            else if (taskName.equals("find a text in video"))
                            {
                                String roundTitle = "FIND A TEXT<br>IN VIDEO";

                                String title = "find a text in video";

                                String message = "Okay, now say the text you want to search in your video with keyword search text.";

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("bucket_name",bucketName);
                                session.put("file_name",fileName);
                                session.put("file_format",fileFormat);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                            }
                            else if (taskName.equals("find a object in video"))
                            {
                                String roundTitle = "FIND A OBJECT<br>IN VIDEO";

                                String title = "find a object in video";

                                String message = "Okay, now say the object name you want to search in your video with keyword search object name.";

                                Map<String,Object> session = new HashMap<>();

                                session.put("task_name",taskName);
                                session.put("bucket_name",bucketName);
                                session.put("file_name",fileName);
                                session.put("file_format",fileFormat);
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                            }
                            else if (taskName.equals("find a adult in video"))
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
                                            .addFeatures(Feature.EXPLICIT_CONTENT_DETECTION)
                                            .build();

                                    OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
                                            client.annotateVideoAsync(request);

                                    StringBuilder voiceBuilder = new StringBuilder();

                                    StringBuilder screenBuilder = new StringBuilder();

                                    String intro = "Okay, analyse has been finished. Now i tell the minute and seconds one by one, where the adult content is detected in your video. ";

                                    voiceBuilder.append(intro);

                                    for (VideoAnnotationResults result : response.get().getAnnotationResultsList())
                                    {
                                        for (int i=0; i<result.getExplicitAnnotation().getFramesList().size(); i++)
                                        {
                                            ExplicitContentFrame frame = result.getExplicitAnnotation().getFrames(i);

                                            long seconds = frame.getTimeOffset().getSeconds();

                                            int min = (int) seconds / 60;
                                            int sec = (int) seconds % 60;

                                            voiceBuilder
                                                    .append(i+1)
                                                    .append(". location ")
                                                    .append(min).append(" minute ")
                                                    .append(sec);

                                            screenBuilder.append(i+1).append(". Location : ")
                                                    .append(min).append(" minute ")
                                                    .append(sec);

                                            if (i == result.getExplicitAnnotation().getFramesList().size() - 1)
                                            {
                                                voiceBuilder.append(" seconds. ");

                                                screenBuilder.append(" seconds. ");
                                            }
                                            else
                                            {
                                                voiceBuilder.append(" seconds. ");

                                                screenBuilder.append(" seconds,<br>");
                                            }
                                        }
                                    }

                                    String roundTitle = "FIND A ADULT<br>IN VIDEO";

                                    String title = "find a adult in video";

                                    String message = voiceBuilder.toString() + "Okay, this is are the detected adult content locations in your video. If you want to perform another task simply say the task name with keyword task name.";

                                    String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                    Map<String,Object> session = new HashMap<>();

                                    session.remove("task_name");
                                    session.remove("bucket_name");
                                    session.remove("file_name");
                                    session.remove("file_format");
                                    session.put("repeat_message",message);
                                    session.put("repeat_re_prompt_message",message);

                                    return getSimpleResponse(input,roundTitle,title,message,screenBuilder.toString(),rePrompt,session);
                                }
                                catch (IOException | InterruptedException | ExecutionException e)
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

    private AmazonRekognition getAmazonRekognitionClient()
    {
        return AmazonRekognitionClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials()
                {
                    @Override
                    public String getAWSAccessKeyId()
                    {
                        return "AKIAINP5QUYC67NNSFUA";
                    }

                    @Override
                    public String getAWSSecretKey()
                    {
                        return "CfmsXdlwrsrsqqkAuEQCOP8/CgEhkAfiTIhOnrpJ";
                    }
                }))
                .withRegion(Regions.US_EAST_1)
                .build();
    }
}
