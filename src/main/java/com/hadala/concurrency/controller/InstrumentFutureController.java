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
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RestController
public class InstrumentFutureController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;
   private final ExecutorService executorService;

   @Autowired
   public InstrumentFutureController(ClientService clientService,
                                     InstrumentsService instrumentsService,
                                     PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
      this.executorService = Executors.newCachedThreadPool();
   }

   @RequestMapping("/future/client/{clientId}/instruments")
   public Response getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {

      Future<Boolean> canTradeFuture = executorService.submit(() -> clientService.hasPermissionToTrade(clientId));

      Future<Collection<Instrument>> instrumentsFuture = executorService.submit(
            () -> instrumentsService.getFavouriteInstruments(clientId));

      boolean canTrade = canTradeFuture.get();

      Collection<Instrument> instruments = instrumentsFuture.get();

      List<Future<PricedInstrument>> pricedInstrumentFutures = instruments
            .stream()
            .map(
                  instrument -> executorService.submit(() -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode()))))
            .collect(Collectors.toList());


      Collection<PricedInstrument> pricedInstruments = pricedInstrumentFutures.stream().map(this::safeGetFuture)
                                                                                    .collect(Collectors.toList());
      return new Response(clientId, pricedInstruments, canTrade);
   }

   private PricedInstrument safeGetFuture(Future<PricedInstrument> future) {
      try {
         return future.get();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
