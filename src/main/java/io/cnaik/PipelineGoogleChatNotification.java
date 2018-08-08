package io.cnaik;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import jenkins.model.JenkinsLocationConfiguration;
import jenkins.tasks.SimpleBuildStep;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import io.cnaik.service.CommonUtil;

import javax.annotation.Nonnull;

public class PipelineGoogleChatNotification extends Builder implements SimpleBuildStep, IGoogleChatNotification {

    private static final long serialVersionUID = 1L;

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    private final String url;
    private final String message;
    private String overrideStatus;

    @DataBoundConstructor
    public PipelineGoogleChatNotification(String url, String message) {
        this.url = url;
        this.message = message;
    }

    @DataBoundSetter
    public void setOverrideStatus(String overrideStatus) {
        this.overrideStatus = overrideStatus;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) {

        JenkinsLocationConfiguration globalConfig = new JenkinsLocationConfiguration();
        CommonUtil commonUtil = new CommonUtil(this);
        commonUtil.setTaskListener(listener);

        // Publish to Google Chat Notification
        commonUtil.sendNotification(commonUtil.formResultJSON(run, globalConfig));
    }

    @Symbol("googlechatnotification")
    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Builder> {

        @Override
        public String getDisplayName() {
            return "Google Chat Notification";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> t) {
            return true;
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getOverrideStatus() {
        return overrideStatus;
    }
}
