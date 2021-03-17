package com.github.evgenykuzin.app.loger;

import com.github.evgenykuzin.app.gui.GUI;

public class GuiLogger implements Logger {

    @Override
    public void log(String from, String msg) {
        System.out.printf("LOG: %s%n", msg);
        GUI.LOG_COMPONENT.append(String.format(">From (%s):-----[%s].", from, msg));
        GUI.LOG_COMPONENT.append("\n");
    }

}
