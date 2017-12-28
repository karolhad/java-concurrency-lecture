package com.hadala.concurrency.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Instrument {

   private final String code;

   private final String name;

   public Instrument(String code, String name) {
      this.code = code;
      this.name = name;
   }

   public String getCode() {
      return code;
   }

   public String getName() {
      return name;
   }
}
