package org.jekajops.app.loger;

import static org.jekajops.app.gui.GUI.LOG_COMPONENT;

public class GuiLoger implements Loger {

    @Override
    public void log(String from, String msg) {
        System.out.printf("LOG: %s%n", msg);
        LOG_COMPONENT.append(String.format(">From (%s):-----[%s].", from, msg));
        LOG_COMPONENT.append("\n");
    }

}
