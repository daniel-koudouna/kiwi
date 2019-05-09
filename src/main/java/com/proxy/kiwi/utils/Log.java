package com.proxy.kiwi.utils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.LoggerWrapper;

public class Log {

    private static void log(BiConsumer<Logger,String> func, Class<?> clazz, String message) {
        Logger logger = new LoggerWrapper(LoggerFactory.getLogger(clazz), Log.class.getName());
        func.accept(logger, message);
    }

    public static void debug(Class<?> clazz, String message) {
        log(Logger::debug, clazz, message);
    }

    public static void info(Class<?> clazz, String message) {
        log(Logger::info, clazz, message);
    }

    public static void error(Class<?> clazz, String message) {
        log(Logger::debug, clazz, message);
    }

    public static <T> void list(Class<?> clazz, BiConsumer<Class<?>,String> func, List<T> list, String header) {
        func.accept(clazz, header);
        list.forEach(t -> func.accept(clazz, t.toString()));
    }

    public static <T> void list(Class<?> clazz, BiConsumer<Class<?>,String> func, Stream<T> stream, String header) {
        list(clazz, func, stream.collect(Collectors.toList()), header);
    }

}
