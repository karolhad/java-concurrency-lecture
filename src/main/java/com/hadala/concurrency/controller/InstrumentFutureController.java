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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@RestController
public class InstrumentFutureController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;
   private ExecutorService executorService;

   @Autowired
   public InstrumentFutureController(ClientService clientService,
                                     InstrumentsService instrumentsService,
                                     PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
   }

   @RequestMapping("/future/client/{clientId}/instruments")
   public Collection<PricedInstrument> getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {

      executorService = Executors.newCachedThreadPool();

      final Future<Client> clientFuture = executorService.submit(() -> clientService.getClient(clientId));

      final Future<Collection<Instrument>> instrumentsFuture = executorService
            .submit(() -> instrumentsService.getFavouriteInstruments(clientId));


      final Collection<Instrument> instruments = instrumentsFuture.get();
      final Client client = clientFuture.get();


      final List<Future<PricedInstrument>> pricedInstrumentFutures = instruments
            .stream()
            .map(
                  instrument -> executorService.submit(() -> new PricedInstrument(instrument, priceService
                        .getPrice(instrument.getCode(), client.getCurrency()))))
            .collect(Collectors.toList());


      return pricedInstrumentFutures.stream().map(this::safeGetFuture).collect(Collectors.toList());

   }

   private  PricedInstrument safeGetFuture(Future<PricedInstrument> future) {
      try {
         return future.get();
      } catch (Exception e) {
         e.printStackTrace();
         return null;
      }
   }
}
