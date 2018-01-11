package com.hadala.concurrency.controller;

import com.hadala.concurrency.dao.AccountService;
import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class InstrumentCompletableFutureAsyncController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;
   private final AccountService accountService;
   private ExecutorService executorService;

   @Autowired
   public InstrumentCompletableFutureAsyncController(ClientService clientService,
                                                     InstrumentsService instrumentsService,
                                                     PriceService priceService, AccountService accountService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
      this.accountService = accountService;
      this.executorService = Executors.newFixedThreadPool(40);
   }

   @RequestMapping("/comp-future-async/client/{clientId}/instruments")
   public CompletableFuture<Response> getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {
      CompletableFuture<Integer> defaultAccountIdFuture = CompletableFuture.supplyAsync(
            () -> clientService.getDefaultAccountId(clientId), executorService);

      CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture.supplyAsync(
            () -> instrumentsService.getFavouriteInstruments(clientId), executorService);

      final CompletableFuture<BigDecimal> amountFuture = defaultAccountIdFuture.thenApplyAsync(accountService::getBalance, executorService);

      return instrumentsFuture.thenCombineAsync(
            amountFuture, (instruments, amount) -> new Response(clientId, instruments, amount), executorService);
   }

}
