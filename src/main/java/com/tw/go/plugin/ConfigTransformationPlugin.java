package com.tw.go.plugin;

import com.google.gson.GsonBuilder;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPlugin;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.annotation.Extension;
import com.thoughtworks.go.plugin.api.exceptions.UnhandledRequestTypeException;
import com.thoughtworks.go.plugin.api.logging.Logger;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoApiResponse;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Extension
public class ConfigTransformationPlugin implements GoPlugin {
    private static final Logger LOG = Logger.getLoggerFor(ConfigTransformationPlugin.class);

    public static final String FIELD_SOURCE = "source";
    public static final String FIELD_DESTINATION = "destination";

    public void initializeGoApplicationAccessor(GoApplicationAccessor goApplicationAccessor) {}

    public GoPluginApiResponse handle(GoPluginApiRequest goPluginApiRequest) throws UnhandledRequestTypeException {
        if (goPluginApiRequest.requestName().equals("configuration")) {
            return this.handleConfiguration();
        }
        if (goPluginApiRequest.requestName().equals("view")) {
            return this.handleView();
        }
        if (goPluginApiRequest.requestName().equals("validate")) {
            return this.handleValidation();
        }
        if (goPluginApiRequest.requestName().equals("execute")) {
            return this.handleExecution(goPluginApiRequest);
        }
        throw new UnhandledRequestTypeException(goPluginApiRequest.requestName());
    }

    public GoPluginIdentifier pluginIdentifier() {
        return new GoPluginIdentifier("task", Arrays.asList("1.0"));
    }

    private GoPluginApiResponse handleConfiguration() {
        HashMap<String, Object> config = new HashMap<String, Object>();
        config.put(FIELD_SOURCE, createField("1", "Source", true));
        config.put(FIELD_DESTINATION, createField("2", "Destination", true));
        return createResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, config);
    }

    private GoPluginApiResponse handleView() {
        HashMap<String, String> view = new HashMap<String, String>();
        int responseCode = DefaultGoApiResponse.SUCCESS_RESPONSE_CODE;

        view.put("displayValue", "Config Transformation");
        try {
            view.put("template", IOUtils.toString(getClass().getResourceAsStream("/views/task.template.html"), "UTF-8"));
        } catch (Exception e) {
            responseCode = DefaultGoApiResponse.INTERNAL_ERROR;
            String errorMessage = String.format("Failed to load view template: %s", e.getMessage());
            view.put("exception", errorMessage);
            LOG.error(errorMessage, e);
        }

        return createResponse(responseCode, view);
    }

    private GoPluginApiResponse handleValidation() {
        return createResponse(DefaultGoApiResponse.SUCCESS_RESPONSE_CODE, new HashMap<String, Object>());
    }

    private GoPluginApiResponse handleExecution(GoPluginApiRequest request) {
        Map executionRequest = (Map)new GsonBuilder().create().fromJson(request.requestBody(), Object.class);
        Map config = (Map)executionRequest.get("config");
        Map context = (Map)executionRequest.get("context");
        JobConsoleLogger consoleLogger = JobConsoleLogger.getConsoleLogger();

        ConfigTransformationTask task = new ConfigTransformationTask(config, context, consoleLogger);
        Result result = task.execute();

        return createResponse(result.responseCode(), result.toMap());
    }

    private GoPluginApiResponse createResponse(int responseCode, Map body) {
        DefaultGoPluginApiResponse response = new DefaultGoPluginApiResponse(responseCode);
        response.setResponseBody(new GsonBuilder().serializeNulls().create().toJson(body));
        return response;
    }

    private Map<String, Object> createField(String displayOrder, String displayName, boolean isRequired) {
        HashMap<String, Object> field = new HashMap<String, Object>();
        field.put("display-order", displayOrder);
        field.put("display-name", displayName);
        field.put("required", isRequired);
        return field;
    }
}
