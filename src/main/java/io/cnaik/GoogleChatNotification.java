package io.cnaik;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import jenkins.model.JenkinsLocationConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import io.cnaik.service.CommonUtil;

public class GoogleChatNotification extends Recorder implements IGoogleChatNotification {

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
                                  String message,
                                  boolean notifyAborted, boolean notifyFailure,
                                  boolean notifyNotBuilt, boolean notifySuccess,
                                  boolean notifyUnstable, boolean notifyBackToNormal) {

        this.url = url;
        this.message = message;
        this.notifyAborted = notifyAborted;
        this.notifyFailure = notifyFailure;
        this.notifyNotBuilt = notifyNotBuilt;
        this.notifySuccess = notifySuccess;
        this.notifyUnstable = notifyUnstable;
        this.notifyBackToNormal = notifyBackToNormal;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {

        JenkinsLocationConfiguration globalConfig = new JenkinsLocationConfiguration();
        CommonUtil commonUtil = new CommonUtil(this);

        if(commonUtil.checkWhetherToSend(build)) {
            // Publish to Google Chat Notification
            commonUtil.sendNotification(commonUtil.formResultJSON(build, globalConfig));
        }
        return true;
    }

    // Overridden for better type safety.
    // If your plugin doesn't really define any property on Descriptor,
    // you don't have to do this.
    @Override
    public Descriptor getDescriptor() {
        return (Descriptor)super.getDescriptor();
    }

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
        return BuildStepMonitor.NONE;
    }

    @Override
    public String getUrl() {
        if(url == null
                || url.equals("")) {
            return getDescriptor().getUrl();
        } else {
            return url;
        }
    }

    @Override
    public String getMessage() {
        if(message == null
                || message.equals("")) {
            return getDescriptor().getMessage();
        } else {
            return message;
        }
    }

    @Override
    public boolean isNotifyAborted() {
        return notifyAborted;
    }

    @Override
    public boolean isNotifyFailure() {
        return notifyFailure;
    }

    @Override
    public boolean isNotifyNotBuilt() {
        return notifyNotBuilt;
    }

    @Override
    public boolean isNotifySuccess() {
        return notifySuccess;
    }

    @Override
    public boolean isNotifyUnstable() {
        return notifyUnstable;
    }

    @Override
    public boolean isNotifyBackToNormal() {
        return notifyBackToNormal;
    }
}
