package com.hadala.concurrency.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@EqualsAndHashCode
public class Price {

   private final BigDecimal sell;

   private final BigDecimal buy;

   public Price(BigDecimal sell, BigDecimal buy) {
      this.sell = sell;
      this.buy = buy;
   }

   public BigDecimal getSell() {
      return sell;
   }

   public BigDecimal getBuy() {
      return buy;
   }
}
