package org.jekajops.app.loger;

public class BasicLoger implements Loger {
    @Override
    public void log(String from, String msg) {
        System.out.printf(">From (%s):-----[%s].\n", from, msg);
    }
}
