package org.jekajops.app.loger;

import static org.jekajops.app.gui.GUI.LOG_COMPONENT;

public class GuiLogger implements Logger {

    @Override
    public void log(String from, String msg) {
        System.out.printf("LOG: %s%n", msg);
        LOG_COMPONENT.append(String.format(">From (%s):-----[%s].", from, msg));
        LOG_COMPONENT.append("\n");
    }

}
