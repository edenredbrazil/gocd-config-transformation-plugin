package com.tw.go.plugin;

import com.thoughtworks.go.plugin.api.task.JobConsoleLogger;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Map;

public class ConfigTransformationTask {
    private Map config;
    private Map context;
    private JobConsoleLogger console;

    public ConfigTransformationTask(Map config, Map context, JobConsoleLogger console) {
        this.config = config;
        this.context = context;
        this.console = console;
    }

    public Result execute() {
        log(":: Config Transformation ::");

        try {
            String source = getValueFromField(ConfigTransformationPlugin.FIELD_SOURCE);
            String destination = getValueFromField(ConfigTransformationPlugin.FIELD_DESTINATION);

            File file = new File(source);

            if (!file.exists()) {
                throw new FileNotFoundException("Configuration file not found: " + source);
            }

            log("Loading configuration file (source): " + source);
            String fileContent = readFile(source);

            log ("Replacing Parameters");
            String transformedFileContent = transform(fileContent, getEnvironmentVariables());

            log("Writing configuration file (destination): " + destination);
            writeFile(transformedFileContent, destination);

            log("Config transformation completed");
            return new Result(true, "Success");
        }
        catch (Exception e) {
            String errorMessage = "Execution interrupted. Reason: " + e.getMessage();
            this.log(errorMessage);
            return new Result(false, errorMessage, e);
        }
    }

    String getValueFromField(String fieldName) {
        return (String) ((Map) config.get(fieldName)).get("value");
    }

    Map<String, String> getEnvironmentVariables() {
        return (Map<String, String>) context.get("environmentVariables");
    }

    String transform(String content, Map<String, String> params) {
        String result = content;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            result = result.replace("#{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    private void log(String message) {
        console.printLine(message);
    }

    private String readFile(String source) throws IOException {
        FileInputStream inputStream = new FileInputStream(source);
        String fileContent = IOUtils.toString(inputStream, "UTF-8");
        inputStream.close();
        return fileContent;
    }

    private void writeFile(String content, String path) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(path);
        outputStream.write(content.getBytes());
        outputStream.close();
    }
}
