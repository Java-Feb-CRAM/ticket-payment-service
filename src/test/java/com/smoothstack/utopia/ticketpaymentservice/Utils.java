package com.smoothstack.utopia.ticketpaymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @author Rob Maes
 * Apr 05 2021
 */
public class Utils {

  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final XmlMapper xmlMapper = new XmlMapper();
  private static boolean initialized = false;

  private Utils() {}

  private static void init() {
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());
    xmlMapper.registerModule(new Jdk8Module());
    xmlMapper.registerModule(new JavaTimeModule());
    initialized = true;
  }

  public static String asJsonString(final Object obj) {
    try {
      if (!initialized) init();
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String asXmlString(final Object obj) {
    try {
      if (!initialized) init();
      return xmlMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static ObjectMapper getMapper() {
    if (!initialized) init();
    return objectMapper;
  }
}
