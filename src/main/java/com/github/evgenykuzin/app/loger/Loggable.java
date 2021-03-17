package com.github.evgenykuzin.app.loger;

import com.github.evgenykuzin.app.cnfg.AppConfig;

public interface Loggable {
    default void log(String msg){
        AppConfig.logger.log(getClass().getName(), msg);
    }
}
