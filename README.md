# SMS to Slack Notifier

An Android application that monitors incoming SMS messages and sends notifications to Slack when specific criteria are met.

## Core Functionality

The app runs in the background and automatically:
*   Listens for incoming SMS messages.
*   Checks if messages match your configured criteria:
    *   **Target Sender (Optional)**: A specific phone number (e.g., `+70123456789`) **OR** a sender name (e.g., `Amazon`).
    *   **Keyword**: A text pattern to look for within the message body (e.g., `confirmation code`).
*   Sends the full message content to your configured Slack channel when both conditions match.
*   Works with messages from both phone numbers and named services.

## Key Features
*   **Flexible Matching**: Configure it to watch for a specific contact or any sender containing your keyword.
*   **Background Operation**: Continues monitoring even when the app is closed.
*   **Simple Setup**: Easy configuration through the app's interface.

Perfect for automatically forwarding important SMS messages—like verification codes, security alerts, or specific notifications from services—directly to your Slack workspace.
