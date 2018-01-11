package com.hadala.concurrency.dao;

import com.google.common.collect.ImmutableList;
import com.hadala.concurrency.model.Instrument;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static com.hadala.concurrency.dao.Sleep.sleep;
@Service
public class SimpleInstrumentsService implements InstrumentsService {

   private static final Instrument CABEL = new Instrument("GBP", "Cabel");
   private static final Instrument GLD = new Instrument("GLD", "Gold");
   private static final Instrument BTC = new Instrument("BTC", "Bitcoin");
   private static final Instrument ETH = new Instrument("ETH", "Etherium");

   @Override
   public Collection<Instrument> getFavouriteInstruments(int clientId) {
      sleep(600);

      switch (clientId) {
         case 101:
            return ImmutableList.of(CABEL, GLD);
         case 102:
            return ImmutableList.of(CABEL);
         case 103:
            return ImmutableList.of(ETH);
         default:
            return  ImmutableList.of();
      }
   }

   public Collection<Instrument> getFavouriteCryptoCurrencies(int clientId) {
      sleep(300);
      switch (clientId) {
         case 101:
            return ImmutableList.of(BTC, ETH);
         case 102:
            return ImmutableList.of(BTC);
         case 103:
            return ImmutableList.of(ETH);
         default:
            return  ImmutableList.of();
      }
   }
}
