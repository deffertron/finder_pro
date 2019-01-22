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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static Utils.Util.fallbackResponse;
import static Utils.Util.getSimpleResponse;
import static com.amazon.ask.request.Predicates.intentName;

public class ThisIsMyTargetFileUrlIntentRequestHandler implements RequestHandler
{
    @Override
    public boolean canHandle(HandlerInput input)
    {
        return input.matches(intentName("ThisIsMyTargetFileUrlIntent"));
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

                        String targetFileName = input.getAttributesManager().getSessionAttributes().getOrDefault("target_file_name",null).toString();

                        String targetFileFormat = input.getAttributesManager().getSessionAttributes().getOrDefault("target_file_format",null).toString();

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
                            String roundTitle = "file name<br>empty";

                            String title = "file name empty";

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
                        else if (targetFileName == null)
                        {
                            String roundTitle = "TARGET FILE<br>NAME EMPTY";

                            String title = "target file name empty";

                            String message = "Sorry, i could not find your target file name. So please, first say the target file name with the keyword target file name.";

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
                        else if (targetFileFormat == null)
                        {
                            String roundTitle = "TARGET FILE<br>FORMAT EMPTY";

                            String title = "target file format empty";

                            String message = "Sorry, i could not find your target file format. So please, first say the target file format with the keyword target file format.";

                            Map<String,Object> session = new HashMap<>();

                            session.put("task_name",taskName);
                            session.put("bucket_name",bucketName);
                            session.put("file_name",fileName);
                            session.put("file_format",fileFormat);
                            session.put("target_bucket_name",targetBucketName);
                            session.put("target_file_name",targetFileName);
                            session.put("repeat_message",message);
                            session.put("repeat_re_prompt_message",message);

                            return getSimpleResponse(input,roundTitle,title,message,message,message,session);
                        }
                        else
                        {
                            String targetImage = fileName + "." + fileFormat;

                            String sourceImage = targetFileName + "." + targetFileFormat;

                            StringBuilder stringBuilder = new StringBuilder();

                            AmazonRekognition rekognitionClient = getAmazonRekognitionClient();

                            try
                            {
                                CompareFacesRequest request = new CompareFacesRequest()
                                        .withSourceImage(new Image().withS3Object(new S3Object().withName(sourceImage).withBucket(bucketName)))
                                        .withTargetImage(new Image().withS3Object(new S3Object().withName(targetImage).withBucket(targetBucketName)))
                                        .withSimilarityThreshold(70F);

                                CompareFacesResult result = rekognitionClient.compareFaces(request);

                                List<CompareFacesMatch> faceDetails = result.getFaceMatches();

                                if (faceDetails.size() == 0)
                                {
                                    stringBuilder.append("I could not any found faces in your target image. Okay if you like to perform another task simply say the task name with keyword task name.");
                                }
                                else if (faceDetails.size() == 1)
                                {
                                    stringBuilder
                                            .append("I found ")
                                            .append(faceDetails.size()).append(" face in your target image using your source image. Now i give the confidence level of the target face, how they are closely matches with your source image face. ")
                                            .append("Confidence level of target face : ")
                                            .append(faceDetails.get(0).getSimilarity())
                                            .append(". Okay, if you like to perform another task simply say the task name with keyword task name.");
                                }
                                else
                                {
                                    stringBuilder
                                            .append("I found ")
                                            .append(faceDetails.size())
                                            .append(" faces in your target image using your source image. Now i give the confidence level of the target faces, how they are closely matches with your source image face. ");

                                    for (int i = 0; i < faceDetails.size(); i++) {
                                        stringBuilder.append("Confidence level of target face ").append(i + 1).append(" : ").append(faceDetails.get(i).getSimilarity());
                                    }

                                    stringBuilder.append(". Okay this are the detected faces in your target image with confidence. If you like to perform another task simply say the task name with keyword task name.");
                                }

                                String roundTitle = "FIND A<br>FACE";

                                String title = "find a face";

                                String message = stringBuilder.toString();

                                String rePrompt = "If you want to perform another task simply say the task name with keyword task name.";

                                Map<String,Object> session = new HashMap<>();

                                session.remove("task_name");
                                session.remove("bucket_name");
                                session.remove("file_name");
                                session.remove("file_format");
                                session.remove("target_bucket_name");
                                session.remove("target_file_name");
                                session.remove("target_file_format");
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
                            }
                            catch (AmazonRekognitionException e)
                            {
                                String roundTitle = "FIND A<br>FACE";

                                String title = "find a face";

                                String message = "Unfortunately, i could not perform the image face comparison task on your images now. " +
                                        "Because i could not find your image or may be some error has been occurred. " +
                                        "Sorry for that. " +
                                        "So, please say a another task name with the keyword task name. ";

                                String rePrompt = "So, please say a another task name with the keyword task name. ";

                                Map<String,Object> session = new HashMap<>();

                                session.remove("task_name");
                                session.remove("bucket_name");
                                session.remove("file_name");
                                session.remove("file_format");
                                session.remove("target_bucket_name");
                                session.remove("target_file_name");
                                session.remove("target_file_format");
                                session.put("repeat_message",message);
                                session.put("repeat_re_prompt_message",message);

                                return getSimpleResponse(input,roundTitle,title,message,message,rePrompt,session);
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
