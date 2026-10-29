package recorder.calls;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity {
    private static final int ASK = 1;
    private MediaPlayer player;
    private String playing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.allow).setOnClickListener(v -> requestPermissions(Gate.required(), ASK));
    }

    @Override
    protected void onResume() {
        super.onResume();
        show();
    }

    @Override
    protected void onDestroy() {
        stopPlayback();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ASK && Gate.ready(this)) {
            Desk.allow(this);
            Desk.status(this, "Waiting for a call.");
            startForegroundService(new Intent(this, RecordingService.class));
        } else if (requestCode == ASK) {
            Desk.status(this, "Recording stays off until the microphone, phone, and notification permissions are allowed.");
        }
        show();
    }

    private void show() {
        boolean open = Desk.allowed(this) && Gate.ready(this);
        findViewById(R.id.setup).setVisibility(open ? View.GONE : View.VISIBLE);
        findViewById(R.id.home).setVisibility(open ? View.VISIBLE : View.GONE);
        TextView status = findViewById(R.id.status);
        status.setText(Desk.status(this));
        if (!open) {
            return;
        }
        if (Gate.ready(this)) {
            startForegroundService(new Intent(this, RecordingService.class));
        }
        List<String> names = recordings();
        TextView empty = findViewById(R.id.empty);
        empty.setVisibility(names.isEmpty() ? View.VISIBLE : View.GONE);
        ListView list = findViewById(R.id.list);
        list.setAdapter(new ArrayAdapter<>(this, R.layout.row_tape, R.id.rowLabel, names));
        list.setOnItemClickListener((parent, view, position, id) -> play(names.get(position)));
    }

    private List<String> recordings() {
        File dir = RecordingService.folder(this);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".m4a"));
        if (files == null || files.length == 0) {
            return new ArrayList<>();
        }
        Arrays.sort(files, (a, b) -> b.getName().compareTo(a.getName()));
        List<String> names = new ArrayList<>();
        for (File file : files) {
            names.add(Tape.label(file.getName()));
        }
        return names;
    }

    private void play(String label) {
        String fileName = Tape.fileNameFromLabel(label);
        if (fileName.equals(playing)) {
            stopPlayback();
            paint("Waiting for a call.");
            return;
        }
        stopPlayback();
        File file = new File(RecordingService.folder(this), fileName);
        player = new MediaPlayer();
        try {
            player.setDataSource(file.getAbsolutePath());
            player.setOnCompletionListener(mp -> {
                stopPlayback();
                paint("Waiting for a call.");
            });
            player.prepare();
            player.start();
            playing = fileName;
            paint("Playing " + label);
        } catch (Exception error) {
            stopPlayback();
            paint("That recording could not be played.");
        }
    }

    private void paint(String message) {
        Desk.status(this, message);
        ((TextView) findViewById(R.id.status)).setText(message);
    }

    private void stopPlayback() {
        if (player != null) {
            player.release();
            player = null;
        }
        playing = null;
    }
}
