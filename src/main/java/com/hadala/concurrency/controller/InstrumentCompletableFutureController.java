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

import static java.util.stream.Collectors.toList;

@RestController
public class InstrumentCompletableFutureController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;
   private ExecutorService executorService;

   @Autowired
   public InstrumentCompletableFutureController(ClientService clientService,
                                                InstrumentsService instrumentsService,
                                                PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
      this.executorService = Executors.newCachedThreadPool();
   }

   static <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> com) {
      return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()]))
                              .thenApply(v -> com.stream()
                                                 .map(CompletableFuture::join)
                                                 .collect(toList())
                              );
   }

   @RequestMapping("/comp-future/client/{clientId}/instruments")
   public Response getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {
      CompletableFuture<Boolean> canTradeFuture = CompletableFuture.supplyAsync(
            () -> clientService.hasPermissionToTrade(clientId), executorService);

      CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture.supplyAsync(
            () -> instrumentsService.getFavouriteInstruments(clientId), executorService);

      CompletableFuture<List<PricedInstrument>> pricedInstrumentsFuture = instrumentsFuture.thenCompose(
            instruments -> {

               List<CompletableFuture<PricedInstrument>> pricedInstrumentFutures = instruments.stream().map(
                     instrument -> CompletableFuture.supplyAsync(() -> createPricedInstrument(instrument))).collect(toList());

               return allOf(pricedInstrumentFutures);
            });

      CompletableFuture<Response> clientInstrumentsCompletableFuture = pricedInstrumentsFuture.thenCombineAsync(
            canTradeFuture, (pricedInstruments, canTrade) -> new Response(clientId, pricedInstruments, canTrade));

      return clientInstrumentsCompletableFuture.get();
   }

   private PricedInstrument createPricedInstrument(Instrument instrument) {
      return new PricedInstrument(instrument, priceService.getPrice(instrument.getCode()));
   }
}
