package com.hadala.concurrency.controller;

import com.google.common.collect.Iterables;
import com.hadala.concurrency.dao.AccountService;
import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.PricedInstrument;
import com.hadala.concurrency.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class InstrumentCompletableFutureController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final AccountService accountService;
   private final PriceService priceService;
   private ExecutorService executorService;

   @Autowired
   public InstrumentCompletableFutureController(ClientService clientService,
                                                InstrumentsService instrumentsService,
                                                AccountService accountService,
                                                PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.accountService = accountService;
      this.priceService = priceService;
      this.executorService = Executors.newFixedThreadPool(40);
   }


   @RequestMapping("/comp-future/client/{clientId}/instruments")
   public Response getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {
      CompletableFuture<Integer> defaultAccountIdFuture = CompletableFuture.supplyAsync(
            () -> clientService.getDefaultAccountId(clientId), executorService);

      CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture.supplyAsync(
            () -> instrumentsService.getFavouriteInstruments(clientId), executorService);

      final CompletableFuture<BigDecimal> amountFuture = defaultAccountIdFuture.thenApplyAsync(accountService::getBalance, executorService);


      CompletableFuture<Response> clientInstrumentsCompletableFuture = instrumentsFuture.thenCombineAsync(
            amountFuture, (instruments, amount) -> new Response(clientId, instruments, amount), executorService);

      return clientInstrumentsCompletableFuture.get();
   }

   private CompletableFuture<List<PricedInstrument>> getPricedInstruments(Collection<Instrument> instruments) {

      final List<CompletableFuture<PricedInstrument>> listOfFutures = instruments.stream()
                                                                                 .map(this::getPricedInstrumentAsync)
                                                                                 .collect(Collectors.toList());

      return CompletableFuture
            .allOf(Iterables.toArray(listOfFutures, CompletableFuture.class))
            .thenApply(v -> listOfFutures
                  .stream()
                  .map(CompletableFuture::join)
                  .collect(Collectors.toList())
            );

   }

   private CompletableFuture<PricedInstrument> getPricedInstrumentAsync(Instrument instrument) {
      return CompletableFuture.supplyAsync(() -> priceService.getPrice(instrument.getCode()), executorService)
                              .thenApply(price -> new PricedInstrument(instrument, price));
   }
}
