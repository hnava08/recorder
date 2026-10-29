# Recorder

A native Android app, written in Java, that records a phone call when the call starts and stops when the call ends. The first screen follows the public Figma file [Recording App Main Screen Design by Haider Irshad](https://www.figma.com/community/file/1382613282925245897/recording-app-main-screen-design-by-haider-irshad): a gray field, a black record button, and the line “Start recording”.

Recording begins only after the microphone, phone, and notification permissions are allowed. The service listens for call state, tries the call-audio source, and keeps a file only when it has real audio. If the phone blocks that source, the app says so and does not save a microphone file in its place. Recordings stay on the device. A tap plays one back.

```
gradle :app:assembleDebug
```

The debug APK is `app/build/outputs/apk/debug/app-debug.apk`.

Permissions: microphone, phone state, notifications, a foreground service, and start after boot.

Android 12 and newer reserve call audio for system apps on most phones. This project was not run on a handset, so incoming and outgoing cellular audio is not claimed for any model.
