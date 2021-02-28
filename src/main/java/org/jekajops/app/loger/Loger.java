package org.jekajops.app.loger;

public interface Loger {

     void log(String from, String msg);

    default void log(String msg) {
        log("main", msg);
    }

}
