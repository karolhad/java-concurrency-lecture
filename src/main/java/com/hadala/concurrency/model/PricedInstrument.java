package com.hadala.concurrency.model;

import lombok.ToString;

@ToString
public class PricedInstrument extends Instrument {

   private final Price price;

   public PricedInstrument(Instrument instrument, Price price) {
      super(instrument.getCode(), instrument.getName());
      this.price = price;
   }

   public Price getPrice() {
      return price;
   }
}
