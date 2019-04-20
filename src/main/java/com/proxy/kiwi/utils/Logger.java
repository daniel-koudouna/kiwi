package com.proxy.kiwi.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Logger {

  public static <T> void stream(List<T> list, String header) {
    if (list.isEmpty()) {
      System.out.println("[LOG ] " + header + " :: Empty");
    } else {
      System.out.println("[LOG ] " + header);
      System.out.println("[LOG ] -------------------------");
      list.forEach(f -> System.out.println("[LOG ] " + f));
      System.out.println("[LOG ] -------------------------");
    }
  }

  public static <T> void stream(Stream<T> stream, String header) {
    stream(stream.collect(Collectors.toList()), header);
  }

}
