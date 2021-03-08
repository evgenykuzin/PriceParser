package org.jekajops.app.loger;

public interface Logger {

     void log(String from, String msg);

    default void log(String msg) {
        log("main", msg);
    }

}
