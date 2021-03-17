package com.github.evgenykuzin.app.loger;

public class BasicLogger implements Logger {
    @Override
    public void log(String from, String msg) {
        System.out.printf(">From (%s):-----[%s].\n", from, msg);
    }
}
