package recorder.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        if (Desk.allowed(context) && Gate.ready(context)) {
            context.startForegroundService(new Intent(context, RecordingService.class));
        }
    }
}
