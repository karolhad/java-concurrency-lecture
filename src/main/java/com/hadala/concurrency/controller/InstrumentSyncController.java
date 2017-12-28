package com.hadala.concurrency.controller;

import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Response;
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

   @RequestMapping("/sync/client/{clientId}/instruments")
   public Response getFavouriteInstruments(@PathVariable("clientId") int clientId) {
      boolean canTrade = clientService.hasPermissionToTrade(clientId); // ~1sec

      Collection<Instrument> favouriteInstruments = instrumentsService.getFavouriteInstruments(clientId); // ~0.6sec

      Collection<PricedInstrument>  pricedInstruments = favouriteInstruments
            .stream()
            .map(instrument -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode()))) //~0.5sec
            .collect(Collectors.toList());

      return new Response(clientId, pricedInstruments, canTrade);
   }
}
