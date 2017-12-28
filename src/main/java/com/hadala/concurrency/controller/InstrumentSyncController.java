package com.hadala.concurrency.controller;

import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Client;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.PricedInstrument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.stream.Collectors;

@RestController
public class InstrumentSyncController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;

   @Autowired
   public InstrumentSyncController(ClientService clientService,
                                   InstrumentsService instrumentsService,
                                   PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
   }

   @RequestMapping("/client/{clientId}/instruments")
   public Collection<PricedInstrument> getFavouriteInstruments(@PathVariable("clientId") int clientId) {
      final Client client = clientService.getClient(clientId); // ~1sec

      final Collection<Instrument> favouriteInstruments = instrumentsService.getFavouriteInstruments(clientId); // ~1.5sec

      return favouriteInstruments
            .stream()
            .map(
                  instrument -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode(), client.getCurrency()))) //~0.5sec
            .collect(Collectors.toList());
   }
}
