package recorder.calls;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;

public final class Gate {
    private Gate() {
    }

    public static String[] required() {
        List<String> names = new ArrayList<>();
        names.add(Manifest.permission.RECORD_AUDIO);
        names.add(Manifest.permission.READ_PHONE_STATE);
        if (Build.VERSION.SDK_INT >= 33) {
            names.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return names.toArray(new String[0]);
    }

    public static boolean ready(Context context) {
        for (String permission : required()) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
