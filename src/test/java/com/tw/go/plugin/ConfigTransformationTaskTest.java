package com.tw.go.plugin;

import com.google.gson.GsonBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

public class ConfigTransformationTaskTest {
    private ConfigTransformationTask task;

    @Before
    public void init() {
        Map executionRequest = (Map) new GsonBuilder().create().fromJson(createRequestJson(), Object.class);
        Map config = (Map)executionRequest.get("config");
        Map context = (Map)executionRequest.get("context");
        task = new ConfigTransformationTask(config, context, null);
    }

    @Test
    public void getValueFromField() throws Exception {
        String source = task.getValueFromField("source");
        String destination = task.getValueFromField("destination");
        Assert.assertEquals("D:/WebSites/SAS/web.config.token", source);
        Assert.assertEquals("D:/WebSites/SAS/web.config", destination);
    }

    @Test
    public void getEnvironmentVariables() throws Exception {
        Map<String, String> environmentVariables = task.getEnvironmentVariables();
        Assert.assertTrue(environmentVariables.containsKey("connection_string"));
        Assert.assertTrue(environmentVariables.containsKey("url_corporativo"));
    }

    @Test
    public void transform() throws Exception {
        String content = createContent();
        String newContent = task.transform(content, task.getEnvironmentVariables());
        Assert.assertFalse(newContent.contains("#{connection_string}"));
        Assert.assertFalse(newContent.contains("#{url_corporativo}"));
    }

    private String createContent() {
        return "<configuration>\n" +
                "    <appSettings>\n" +
                "        <add key=\"connection_string\" value=\"#{connection_string}\" />\n" +
                "        <add key=\"url_corporativo\" value=\"#{url_corporativo}\" />\n" +
                "    </appSettings>\n" +
                "</configuration>";
    }

    private String createRequestJson() {
        return "{\n" +
                "    \"config\": {\n" +
                "        \"source\": {\n" +
                "            \"secure\": false,\n" +
                "            \"value\": \"D:/WebSites/SAS/web.config.token\",\n" +
                "            \"required\": true\n" +
                "        },\n" +
                "        \"destination\": {\n" +
                "            \"secure\": false,\n" +
                "            \"value\": \"D:/WebSites/SAS/web.config\",\n" +
                "            \"required\": true\n" +
                "        }\n" +
                "    },\n" +
                "    \"context\": {\n" +
                "        \"environmentVariables\": {\n" +
                "            \"connection_string\": \"Data Source=localhost; Initial Catalog=SASDB; User Id=user; Password=passwd\",\n" +
                "            \"url_corporativo\": \"http://testecorporativo.lanet.accorservices.net\"\n" +
                "        },\n" +
                "     \"workingDirectory\": \"D:/Go/Agent\"\n" +
                "    }\n" +
                "}";
    }
}