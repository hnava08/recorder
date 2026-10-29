package recorder.calls;

import android.content.Context;
import android.content.SharedPreferences;

public final class Desk {
    private static final String FILE = "desk";

    private Desk() {
    }

    public static boolean allowed(Context context) {
        return prefs(context).getBoolean("allowed", false);
    }

    public static void allow(Context context) {
        prefs(context).edit().putBoolean("allowed", true).apply();
    }

    public static void status(Context context, String message) {
        prefs(context).edit().putString("status", message).apply();
    }

    public static String status(Context context) {
        return prefs(context).getString("status", "Waiting for permission.");
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }
}
