package com.hadala;

import com.hadala.concurrency.dao.*;
import org.apache.log4j.Logger;


public class ScratchPad {

   private static final Logger LOG = Logger.getLogger(ScratchPad.class);

   ClientService clientService = new SimpleClientService();
   InstrumentsService instrumentsService = new SimpleInstrumentsService();
   PriceService priceService = new SimplePriceService();
   AccountService accountService = new AccountService();



}
