package recorder.calls;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class RecordingService extends Service {
    private static final String CHANNEL = "recorder";
    private static final int NOTE = 7;

    private CallWatch watch;
    private MediaRecorder recorder;
    private File current;
    private TelephonyManager telephony;
    private CallState callState;

    public static File folder(Context context) {
        File dir = new File(context.getFilesDir(), "tapes");
        dir.mkdirs();
        return dir;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        watch = new CallWatch();
        ensureChannel();
        startForeground(NOTE, note("Waiting for a call."), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        if (!Desk.allowed(this) || !Gate.ready(this)) {
            Desk.status(this, "Recording stays off until the microphone, phone, and notification permissions are allowed.");
            stopSelf();
            return;
        }
        telephony = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        callState = new CallState();
        telephony.registerTelephonyCallback(getMainExecutor(), callState);
        Desk.status(this, "Waiting for a call.");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (telephony != null && callState != null) {
            telephony.unregisterTelephonyCallback(callState);
        }
        finish(false);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void apply(int state) {
        if (!Gate.ready(this)) {
            finish(false);
            Desk.status(this, "Recording stopped because a permission was removed.");
            return;
        }
        String command = watch.onState(state);
        if (command == null) {
            return;
        }
        if ("idle".equals(command)) {
            finish(true);
            Desk.status(this, "Waiting for a call.");
            startForeground(NOTE, note("Waiting for a call."), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            return;
        }
        begin(command);
    }

    private void begin(String direction) {
        finish(false);
        File file = new File(folder(this), Tape.fileName(direction, LocalDateTime.now()));
        MediaRecorder next = new MediaRecorder(this);
        try {
            next.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL);
            next.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            next.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            next.setOutputFile(file.getAbsolutePath());
            next.prepare();
            next.start();
        } catch (IOException | RuntimeException error) {
            next.release();
            if (file.exists() && !file.delete()) {
                file.deleteOnExit();
            }
            Desk.status(this, "This phone does not expose call audio to this app. No file was saved.");
            startForeground(NOTE, note("Call audio is blocked on this phone."), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            return;
        }
        recorder = next;
        current = file;
        Desk.status(this, "in".equals(direction) ? "Recording an incoming call." : "Recording an outgoing call.");
        startForeground(NOTE, note("Recording"), ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
    }

    private void finish(boolean keepCheck) {
        if (recorder == null) {
            return;
        }
        try {
            recorder.stop();
        } catch (RuntimeException ignored) {
            keepCheck = false;
        }
        recorder.release();
        recorder = null;
        if (current != null && (!keepCheck || !Tape.keep(current))) {
            if (!current.delete()) {
                current.deleteOnExit();
            }
            if (keepCheck) {
                Desk.status(this, "The recording was empty, so it was discarded.");
            }
        }
        current = null;
    }

    private void ensureChannel() {
        NotificationManager manager = getSystemService(NotificationManager.class);
        NotificationChannel channel = new NotificationChannel(CHANNEL, "Recorder", NotificationManager.IMPORTANCE_LOW);
        manager.createNotificationChannel(channel);
    }

    private final class CallState extends TelephonyCallback implements TelephonyCallback.CallStateListener {
        @Override
        public void onCallStateChanged(int state) {
            apply(state);
        }
    }

    private Notification note(String text) {
        return new Notification.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_stat)
                .setContentTitle("Recorder")
                .setContentText(text)
                .setOngoing(true)
                .build();
    }
}
