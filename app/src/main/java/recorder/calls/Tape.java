package recorder.calls;

import java.io.File;
import java.time.LocalDateTime;

public final class Tape {
    public static final long MINIMUM_BYTES = 4096;

    private Tape() {
    }

    public static String fileName(String direction, LocalDateTime when) {
        return String.format(
                "%04d-%02d-%02d_%02d-%02d-%02d_%s.m4a",
                when.getYear(),
                when.getMonthValue(),
                when.getDayOfMonth(),
                when.getHour(),
                when.getMinute(),
                when.getSecond(),
                direction);
    }

    public static boolean keep(File file) {
        return file != null && file.isFile() && file.length() >= MINIMUM_BYTES;
    }

    public static String label(String fileName) {
        String stem = fileName.replace(".m4a", "");
        int mark = stem.lastIndexOf('_');
        if (mark < 0) {
            return fileName;
        }
        String when = stem.substring(0, mark).replace('_', ' ');
        String direction = stem.substring(mark + 1);
        String side = "in".equals(direction) ? "Incoming" : "Outgoing";
        return when + "  ·  " + side;
    }

    public static String fileNameFromLabel(String label) {
        String[] parts = label.split("  ·  ");
        String when = parts[0].replace(' ', '_');
        String direction = parts.length > 1 && parts[1].startsWith("Incoming") ? "in" : "out";
        return when + "_" + direction + ".m4a";
    }
}
