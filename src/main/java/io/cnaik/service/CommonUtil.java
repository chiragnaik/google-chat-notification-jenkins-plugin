package io.cnaik.service;

import io.cnaik.IGoogleChatNotification;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import jenkins.model.JenkinsLocationConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.logging.Level;
import java.util.logging.Logger;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.with;

public class CommonUtil {

    private IGoogleChatNotification googleChatNotification;
    private TaskListener taskListener;

    private final static Logger LOGGER = Logger.getLogger(CommonUtil.class.getName());

    public CommonUtil(IGoogleChatNotification googleChatNotification) {
        this.googleChatNotification = googleChatNotification;
    }

    public Boolean sendNotification(String json) {

        String[] urlDetails = googleChatNotification.getUrl().split(",");
        Response response = null;

        for(String urlDetail: urlDetails) {

            String[] url = urlDetail.split("\\?");

            response = given(with().baseUri(url[0])).contentType(ContentType.JSON).queryParam(url[1]).urlEncodingEnabled(false).body(json).log()
                    .all().post();
        }

        LOGGER.log(Level.INFO, "Chat Notification Response: " + response.print());

        if(taskListener != null) {
            taskListener.getLogger().println("Chat Notification Response: " + response.print());
        }

        return response.statusCode() == HttpStatus.SC_OK ? true : false;
    }

    public String formResultJSON(Run build, JenkinsLocationConfiguration globalConfig) {

        String defaultMessage = escapeSpecialCharacter(replaceJenkinsKeywords(googleChatNotification.getMessage(), build, globalConfig));
        return "{ 'text': '" + defaultMessage + "'}";
    }

    public String replaceJenkinsKeywords(String inputString, Run build, JenkinsLocationConfiguration globalConfig) {

        if(StringUtils.isEmpty(inputString)) {
            return inputString;
        }

        String outputString = inputString;

        if(outputString.contains("$DEFAULT_SUBJECT")) {
            outputString = outputString.replace("$DEFAULT_SUBJECT", getDefaultSubject(build));
        }

        if(outputString.contains("$BUILD_URL")) {
            outputString = outputString.replace("$BUILD_URL", getBuildURL(build, globalConfig));
        }

        if(outputString.contains("$CONSOLE_URL")) {
            outputString = outputString.replace("$CONSOLE_URL", getConsoleURL(build, globalConfig));
        }

        if(outputString.contains("$CONSOLE_FULL_URL")) {
            outputString = outputString.replace("$CONSOLE_FULL_URL", getConsoleFullURL(build, globalConfig));
        }

        return outputString;
    }

    public String getDefaultSubject(Run build) {

        String status = "";

        if(StringUtils.isNotEmpty(googleChatNotification.getOverrideStatus())) {

            status = getFormattedStatus(googleChatNotification.getOverrideStatus());

        } else if(build != null && build.getResult() != null) {

            status = getFormattedStatus(build.getResult().toString());

        } else if(build != null && build.isBuilding()) {

            status = getFormattedStatus("STARTED");

        }

        if(taskListener != null) {
            taskListener.getLogger().println("formattedStatus: " + status);
        }

        StringBuilder outputString = new StringBuilder(build.getFullDisplayName());
        outputString.append(" ").append("-").append(" ").append("*").append(status).append("*");
        return outputString.toString();
    }

    public String getBuildURL(Run build, JenkinsLocationConfiguration globalConfig) {
        return getDefaultJenkinsURL(build, globalConfig) + "build";
    }

    public String getConsoleURL(Run build, JenkinsLocationConfiguration globalConfig) {
        return getDefaultJenkinsURL(build, globalConfig) + "console";
    }

    public String getConsoleFullURL(Run build, JenkinsLocationConfiguration globalConfig) {
        return getDefaultJenkinsURL(build, globalConfig) + "consoleFull";
    }

    public String getDefaultJenkinsURL(Run build, JenkinsLocationConfiguration globalConfig) {
        return globalConfig.getUrl() + build.getUrl();
    }

    public Boolean checkWhetherToSend(Run build) {
        Boolean result = false;

        if(build != null && build.getResult() != null) {
            if(googleChatNotification.isNotifyAborted()
                    && Result.ABORTED.toString().equalsIgnoreCase(build.getResult().toString())) {

                result = true;

            } else if(googleChatNotification.isNotifyBackToNormal()
                    && ( ( Result.ABORTED.toString().equalsIgnoreCase(build.getPreviousBuild().getResult().toString())
                    || Result.FAILURE.toString().equalsIgnoreCase(build.getPreviousBuild().getResult().toString())
                    || Result.UNSTABLE.toString().equalsIgnoreCase(build.getPreviousBuild().getResult().toString())
                    || Result.NOT_BUILT.toString().equalsIgnoreCase(build.getPreviousBuild().getResult().toString())
            ) && Result.SUCCESS.toString().equalsIgnoreCase(build.getResult().toString())
            )
                    ) {

                result = true;

            } else if(googleChatNotification.isNotifyFailure()
                    && Result.FAILURE.toString().equalsIgnoreCase(build.getResult().toString())) {

                result = true;

            } else if(googleChatNotification.isNotifyNotBuilt()
                    && Result.NOT_BUILT.toString().equalsIgnoreCase(build.getResult().toString())) {

                result = true;
            } else if(googleChatNotification.isNotifySuccess()
                    && Result.SUCCESS.toString().equalsIgnoreCase(build.getResult().toString())) {

                result = true;

            } else if(googleChatNotification.isNotifyUnstable()
                    && Result.UNSTABLE.toString().equalsIgnoreCase(build.getResult().toString())) {

                result = true;
            }
        }
        return result;
    }

    public void setTaskListener(TaskListener taskListener) {
        this.taskListener = taskListener;
    }

    public String getFormattedStatus(String status) {

        if(taskListener != null) {
            taskListener.getLogger().println("Result: " + status);
        }

        String formattedStatus = "";

        switch (status.toUpperCase()) {
            case "SUCCESS":
                formattedStatus = "PASSED";
                break;
            case "UNSTABLE":
                formattedStatus = "UNSTABLE";
                break;
            case "FAILURE":
                formattedStatus = "FAILED";
                break;
            case "NOT_BUILT":
                formattedStatus = "NOT_BUILT";
                break;
            case "ABORTED":
                formattedStatus = "ABORTED";
                break;
            default:
                formattedStatus = status;
                break;
        }
        return formattedStatus;
    }

    public String escapeSpecialCharacter(String input) {

        String output = input;

        if(taskListener != null) {
            taskListener.getLogger().println("escapeSpecialCharacter input ==: " + output);
        }

        if(StringUtils.isNotEmpty(output)) {
            output = output.replace("{", "\\{");
            output = output.replace("}", "\\}");
            output = output.replace("'", "\\'");
        }

        if(taskListener != null) {
            taskListener.getLogger().println("escapeSpecialCharacter output ==: " + output);
        }

        return output;
    }
}
