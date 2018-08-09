# google-chat-notification-jenkins-plugin

Google Chat Notification Jenkins Plugin to send build status to Google Chat Messenger https://chat.google.com/

This Jenkins plugin allows you to send Google Chat notification as a post build action or as a pipeline script.


## Prerequisites

- You must create a web hook in google chat group to send notification.

![Screenshot](docs/configure-web-hook.png)


## How to configure it in post build action

- Click on Add post-build action button

![Screenshot](docs/add-post-build-action.png)

- Click on Google Chat Notification

![Screenshot](docs/click-google-chat-notification.png)

- Configure URL (web hook URL configured in prerequisites), message (build message) and type of build result you want to send notification. You can configure multiple URLs separated by comma.

![Screenshot](docs/details.png)


## How to use it in pipeline script

Use below command
### googlechatnotification url: 'web hook(s) URL(s)', message: 'message to be sent'

Please find explanations for each fields as below:

1. **url**
   - Single/Multiple comma separated URLs.
   - This is a mandatory field.

2. **message**
   - Notification message to be sent.
   - This is a mandatory field.


## For user friendly messages all token macro variables are supported for both pipeline as well as build jobs.
