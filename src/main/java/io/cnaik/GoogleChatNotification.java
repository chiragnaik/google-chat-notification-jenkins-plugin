package io.cnaik;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import io.cnaik.service.CommonUtil;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;

public class GoogleChatNotification extends Recorder implements SimpleBuildStep {

    private static final long serialVersionUID = 1L;

    private String url;
    private String message;
    private boolean notifyAborted;
    private boolean notifyFailure;
    private boolean notifyNotBuilt;
    private boolean notifySuccess;
    private boolean notifyUnstable;
    private boolean notifyBackToNormal;

    @DataBoundConstructor
    public GoogleChatNotification(String url,
                                  String message) {

        this.url = url;
        this.message = message;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        CommonUtil commonUtil = new CommonUtil(this, listener, null);
        performAction(build, commonUtil.checkWhetherToSend(build), commonUtil);
        return true;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) {
        CommonUtil commonUtil = new CommonUtil(this, listener, workspace);
        performAction(run, commonUtil.checkPipelineFlag(run), commonUtil);
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public Descriptor getDescriptor() {
        return (Descriptor)super.getDescriptor();
    }

    @Symbol("googlechatnotification")
    @Extension
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        private String url;
        private String message;
        private boolean notifyAborted;
        private boolean notifyFailure;
        private boolean notifyNotBuilt;
        private boolean notifySuccess;
        private boolean notifyUnstable;
        private boolean notifyBackToNormal;

        public Descriptor() {
            load();
        }

        public FormValidation doCheckUrl(@QueryParameter String value) {
            if(value.length() == 0) {
                return FormValidation.error("Please add at least one google chat notification URL");
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckMessage(@QueryParameter String value) {
            if(value.length() == 0) {
                return FormValidation.error("Please add message");
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Google Chat Notification";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            // To persist global configuration information,
            // set that to properties and call save().
            url = formData.getString("url");
            message = formData.getString("message");
            notifyAborted = formData.getBoolean("notifyAborted");
            notifyFailure = formData.getBoolean("notifyFailure");
            notifyNotBuilt = formData.getBoolean("notifyNotBuilt");
            notifySuccess = formData.getBoolean("notifySuccess");
            notifyUnstable = formData.getBoolean("notifyUnstable");
            notifyBackToNormal = formData.getBoolean("notifyBackToNormal");

            // ^Can also use req.bindJSON(this, formData);
            //  (easier when there are many fields; need set* methods for this, like setUseFrench)
            save();
            return super.configure(req,formData);
        }

        public String getUrl() {
            return url;
        }
        public String getMessage() {
            return message;
        }

        public boolean isNotifyAborted() {
            return notifyAborted;
        }

        public boolean isNotifyFailure() {
            return notifyFailure;
        }

        public boolean isNotifyNotBuilt() {
            return notifyNotBuilt;
        }

        public boolean isNotifySuccess() {
            return notifySuccess;
        }

        public boolean isNotifyUnstable() {
            return notifyUnstable;
        }

        public boolean isNotifyBackToNormal() {
            return notifyBackToNormal;
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.BUILD;
    }

    public String getUrl() {
        if(url == null
                || url.equals("")) {
            return getDescriptor().getUrl();
        } else {
            return url;
        }
    }

    public String getMessage() {
        if(message == null
                || message.equals("")) {
            return getDescriptor().getMessage();
        } else {
            return message;
        }
    }

    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }

    @DataBoundSetter
    public void setNotifyAborted(boolean notifyAborted) {
        this.notifyAborted = notifyAborted;
    }

    @DataBoundSetter
    public void setNotifyFailure(boolean notifyFailure) {
        this.notifyFailure = notifyFailure;
    }

    @DataBoundSetter
    public void setNotifyNotBuilt(boolean notifyNotBuilt) {
        this.notifyNotBuilt = notifyNotBuilt;
    }

    @DataBoundSetter
    public void setNotifySuccess(boolean notifySuccess) {
        this.notifySuccess = notifySuccess;
    }

    @DataBoundSetter
    public void setNotifyUnstable(boolean notifyUnstable) {
        this.notifyUnstable = notifyUnstable;
    }

    @DataBoundSetter
    public void setNotifyBackToNormal(boolean notifyBackToNormal) {
        this.notifyBackToNormal = notifyBackToNormal;
    }

    private void performAction(Run run, boolean whetherToPerform, CommonUtil commonUtil) {
        if(whetherToPerform) {
            // Publish to Google Chat Notification
            commonUtil.sendNotification(commonUtil.formResultJSON(run));
        }
    }
}
