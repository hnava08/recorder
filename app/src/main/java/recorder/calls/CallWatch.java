package recorder.calls;

/**
 * Turns phone-state changes into a recording command.
 * Ringing alone does not start a file. Off-hook after ringing is incoming.
 * Off-hook without ringing is outgoing.
 */
public final class CallWatch {
    public static final int IDLE = 0;
    public static final int RINGING = 1;
    public static final int OFFHOOK = 2;

    private boolean ringing;
    private boolean live;

    public String onState(int state) {
        if (state == RINGING) {
            ringing = true;
            return null;
        }
        if (state == OFFHOOK) {
            if (live) {
                return null;
            }
            live = true;
            String direction = ringing ? "in" : "out";
            ringing = false;
            return direction;
        }
        if (state == IDLE) {
            ringing = false;
            boolean wasLive = live;
            live = false;
            return wasLive ? "idle" : null;
        }
        return null;
    }
}
