package com.hadala.concurrency.model;

import java.math.BigDecimal;
import java.util.Collection;

public class Response {

   private Collection<Instrument> instruments;
   private long clientId;
   private Collection<PricedInstrument> pricedInstruments;
   private BigDecimal amount;
   private boolean canTrade;

   public Response(long clientId, Collection<PricedInstrument> instruments, boolean canTrade) {
      this.clientId = clientId;
      this.pricedInstruments = instruments;
      this.canTrade = canTrade;
   }

   public Response(int clientId, Collection<Instrument> instruments, BigDecimal amount) {

      this.clientId = clientId;
      this.instruments = instruments;
      this.amount = amount;
   }

   public long getClientId() {
      return clientId;
   }

   public Collection<Instrument> getInstruments() {
      return instruments;
   }

   public Collection<PricedInstrument> getPricedInstruments() {
      return pricedInstruments;
   }

   public BigDecimal getAmount() {
      return amount;
   }

   public boolean isCanTrade() {
      return canTrade;
   }
}
