package recorder.calls;

import java.io.File;
import java.time.LocalDateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class CallWatchTest {
    @Test
    public void outgoingCallStartsWithoutARing() {
        CallWatch watch = new CallWatch();
        assertEquals("out", watch.onState(CallWatch.OFFHOOK));
        assertEquals("idle", watch.onState(CallWatch.IDLE));
    }

    @Test
    public void incomingCallWaitsUntilTheCallIsAnswered() {
        CallWatch watch = new CallWatch();
        assertNull(watch.onState(CallWatch.RINGING));
        assertEquals("in", watch.onState(CallWatch.OFFHOOK));
        assertNull(watch.onState(CallWatch.OFFHOOK));
        assertEquals("idle", watch.onState(CallWatch.IDLE));
        assertNull(watch.onState(CallWatch.IDLE));
    }

    @Test
    public void namesCarryTheDateAndDirection() {
        String name = Tape.fileName("in", LocalDateTime.of(2026, 10, 28, 22, 30, 0));
        assertEquals("2026-10-28_22-30-00_in.m4a", name);
        assertEquals("2026-10-28 22-30-00  ·  Incoming", Tape.label(name));
        assertEquals(name, Tape.fileNameFromLabel(Tape.label(name)));
    }

    @Test
    public void emptyFilesAreDiscarded() throws Exception {
        File file = File.createTempFile("tape", ".m4a");
        file.deleteOnExit();
        assertFalse(Tape.keep(file));
        try (java.io.FileOutputStream out = new java.io.FileOutputStream(file)) {
            out.write(new byte[(int) Tape.MINIMUM_BYTES]);
        }
        assertTrue(Tape.keep(file));
    }
}
