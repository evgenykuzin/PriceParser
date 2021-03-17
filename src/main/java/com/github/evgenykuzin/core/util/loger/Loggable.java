package com.github.evgenykuzin.core.util.loger;

import com.github.evgenykuzin.core.util.cnfg.LogConfig;

public interface Loggable {
    default void log(String msg){
        LogConfig.logger.log(getClass().getName(), msg);
    }
}
