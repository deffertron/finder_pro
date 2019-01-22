package Utils;

import org.json.JSONObject;

public class JsonHelper
{
    static String convertTaskName(String roundTitle, String title, String taskNameMessage)
    {
        JSONObject jsonObject = new JSONObject();

        JSONObject jsonObjectTaskName = new JSONObject();

        jsonObjectTaskName.put("type","object");

        JSONObject jsonObjectPropertiesTaskName = new JSONObject();

        jsonObjectPropertiesTaskName.put("roundTitle",roundTitle + " TASK");
        jsonObjectPropertiesTaskName.put("title",title.toUpperCase() + " TASK");
        jsonObjectPropertiesTaskName.put("message",taskNameMessage);

        jsonObjectTaskName.put("properties",jsonObjectPropertiesTaskName);

        jsonObject.put("taskNameIntentTemplateData",jsonObjectTaskName);

        return jsonObject.toString();
    }

    public static String convertSimpleWithHeader(String roundTitle, String title, String message)
    {
        JSONObject jsonObject = new JSONObject();

        JSONObject jsonObjectSimple = new JSONObject();

        jsonObjectSimple.put("type","object");

        JSONObject jsonObjectSimpleProperties = new JSONObject();

        jsonObjectSimpleProperties.put("roundTitle",roundTitle);
        jsonObjectSimpleProperties.put("title",title.toUpperCase());
        jsonObjectSimpleProperties.put("message",message);

        jsonObjectSimple.put("properties",jsonObjectSimpleProperties);

        jsonObject.put("simpleWithHeaderTemplateData",jsonObjectSimple);

        return jsonObject.toString();
    }
}
