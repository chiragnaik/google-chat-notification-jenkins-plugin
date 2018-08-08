package io.cnaik;

public interface IGoogleChatNotification {

    default String getUrl() {
        return null;
    }

    default String getMessage() {
        return null;
    }

    default boolean isNotifyAborted() {
        return false;
    }

    default boolean isNotifyFailure() {
        return false;
    }

    default boolean isNotifyNotBuilt() {
        return false;
    }

    default boolean isNotifySuccess() {
        return false;
    }

    default boolean isNotifyUnstable() {
        return false;
    }

    default boolean isNotifyBackToNormal(){
        return false;
    }

    default String getOverrideStatus() {
        return null;
    }
}