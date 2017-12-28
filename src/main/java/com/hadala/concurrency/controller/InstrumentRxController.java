package com.hadala.concurrency.controller;

import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.PricedInstrument;
import com.hadala.concurrency.model.Response;
import io.reactivex.Observable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class InstrumentRxController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;
   private ExecutorService executorService;

   @Autowired
   public InstrumentRxController(ClientService clientService,
                                 InstrumentsService instrumentsService,
                                 PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
      this.executorService = Executors.newCachedThreadPool();
   }

   @RequestMapping("/rx/client/{clientId}/instruments")
   public DeferredResult<Response> getFavouriteInstruments(@PathVariable("clientId") int clientId) throws ExecutionException, InterruptedException {
      long start = System.currentTimeMillis();

      Observable<Boolean> canTradeObservable = Observable.fromCallable(() -> clientService.hasPermissionToTrade(clientId));

      Observable<Collection<Instrument>> instrumentObservable = Observable.fromCallable(() -> instrumentsService.getFavouriteInstruments(clientId));


      final Observable<Instrument> instrumentObservable1 = instrumentObservable.flatMapIterable(x -> x);
      System.out.println("before flat map " + (System.currentTimeMillis() - start));

      Observable<PricedInstrument> map = instrumentObservable1.flatMap(instrument -> Observable
            .fromCallable(() -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode()))));


      System.out.println("after flat map " + (System.currentTimeMillis() - start));

      final Observable<Response> rObservable = Observable.combineLatest(map.toList().toObservable(), canTradeObservable, (instruments, canTrade) -> new Response(clientId, instruments, canTrade));

      System.out.println("after combineLatest " + (System.currentTimeMillis() - start));
      DeferredResult<Response> deffered = new DeferredResult<>();
      rObservable.subscribe(deffered::setResult, deffered::setErrorResult);
      return deffered;
   }


}
