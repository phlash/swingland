package com.ashbysoft.swingland.event;

public class ActionEvent extends AbstractEvent {
    public static final int ACTION_FIRED = 0;
    private final String _command;
    public ActionEvent(Object source, int id, String command) { super(source, id); _command = command; }
    public String getActionCommand() { return _command; }
    public String toString() {
        return pfxString()+",command="+_command+")";
    }
}
