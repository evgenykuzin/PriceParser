package org.jekajops.app.loger;

import org.jekajops.app.cnfg.AppConfig;

public interface Loggable {
    default void log(String msg){
        AppConfig.logger.log(getClass().getName(), msg);
    }
}
