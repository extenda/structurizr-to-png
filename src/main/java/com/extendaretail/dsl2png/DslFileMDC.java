package com.extendaretail.dsl2png;

import java.io.File;
import org.slf4j.MDC;

class DslFileMDC implements AutoCloseable {

  private final static String KEY = "dsl";

  private String oldValue;
  private String newValue;

  DslFileMDC(File dslFile) {
    this.oldValue = MDC.get(KEY);
    this.newValue = dslFile.getName();
    MDC.put(KEY, newValue);
  }

  @Override
  public void close() {
    if (oldValue == null) {
      MDC.remove(KEY);
    } else {
      MDC.put(KEY, oldValue);
    }
  }
}