package com.hadala.concurrency.model;

import java.util.Collection;

public class Response {

   private final long clientId;
   private final Collection<PricedInstrument> instruments;
   private final boolean canTrade;

   public Response(long clientId, Collection<PricedInstrument> instruments, boolean canTrade) {
      this.clientId = clientId;
      this.instruments = instruments;
      this.canTrade = canTrade;
   }

   public long getClientId() {
      return clientId;
   }

   public Collection<PricedInstrument> getInstruments() {
      return instruments;
   }

   public boolean isCanTrade() {
      return canTrade;
   }
}
