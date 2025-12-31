# SMS to Slack Notifier

Lightweight Android app that listens for incoming SMS messages and forwards matching messages to a Slack channel via an Incoming Webhook.

## Highlights
- Forward SMS to Slack when a keyword (and optionally a sender) matches.
- Matches either a phone number or sender name (case-insensitive).
- Runs in background via a BroadcastReceiver.
- Configurable from the app UI (webhook, target, keyword, enable).

## Requirements
- Android Studio or command-line Android SDK
- Min SDK: 24
- Runtime permissions: RECEIVE_SMS and READ_SMS (app requests them)

## Quickstart

1. Open the project in Android Studio (or use command line).
2. Build & install on device/emulator:
   - From Android Studio: Run app.
   - Command line:
     ./gradlew :app:installDebug

3. Launch the app, enter:
   - Slack Webhook URL: https://hooks.slack.com/services/...
   - Target Phone OR Name (optional): e.g. +905551234567 or BankName
   - Keywords: e.g. confirmation code
   - Enable "Enable SMS Monitoring" and grant SMS permissions when prompted.

## How matching works
- If Target is blank → any incoming SMS that contains the keyword (case-insensitive) triggers a Slack message.
- If Target is set → sender must match target AND the message must contain the keyword.
  - Phone matching normalizes digits and + sign (non-digits removed).
  - Name matching is case-insensitive and allows partial matches (e.g. "Amazon" matches "Amazon Pay").

## Slack payload
The app posts a simple JSON payload to the configured webhook:
{
  "text": "🤑 New Purchase Alert! 
<message>",
  "username": "SMS Code Bot",
  "icon_emoji": ":key:"
}

## Emulator testing
On the Android emulator you can simulate an incoming SMS:
adb emu sms send 12345 "Your confirmation code is 1234"

## Permissions & Manifest
Required permissions are declared in the manifest:
- android.permission.RECEIVE_SMS
- android.permission.READ_SMS
- android.permission.INTERNET

The app requests RECEIVE/READ SMS at runtime; deny will disable monitoring.

## Security & Privacy
- Treat your Slack webhook like a secret — anyone with it can post to your channel.
- Messages are forwarded in plain text to Slack. Do not use with highly sensitive data unless you control the Slack workspace and channels.

## Troubleshooting
- Nothing is posted to Slack:
  - Check webhook URL is correct and not empty in app settings.
  - Ensure "Enable SMS Monitoring" is ON and SMS permissions are granted.
  - Verify device has network connectivity (INTERNET permission required).
- Matching not happening: check keyword spelling and target format.
