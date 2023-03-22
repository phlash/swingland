package com.ashbysoft.swingland.event;

public class ActionEvent extends AbstractEvent {
    private final String _command;
    public ActionEvent(Object source, int id, String command) { super(source, id); _command = command; }
    public String getCommand() { return _command; }
}
