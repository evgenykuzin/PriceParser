package com.github.evgenykuzin.core.util.loger;

public interface Logger {

     void log(String from, String msg);

    default void log(String msg) {
        log("main", msg);
    }

}
