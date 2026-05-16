package de.yannik.dreamveilCore.util;

import de.yannik.dreamveilCore.DreamveilCore;

import java.util.logging.Logger;

public class Log {

    private static Logger logger;
    private static String pluginName;

    public static void init(DreamveilCore plugin) {
        logger = plugin.getLogger();
        pluginName = plugin.getDescription().getName();
    }

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void warn(String msg) {
        logger.warning(msg);
    }

    public static void error(String msg) {
        logger.severe(msg);
    }
}