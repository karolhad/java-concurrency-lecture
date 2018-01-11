package com.hadala.concurrency.controller;

import com.hadala.concurrency.dao.ClientService;
import com.hadala.concurrency.dao.InstrumentsService;
import com.hadala.concurrency.dao.PriceService;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.PricedInstrument;
import com.hadala.concurrency.model.Response;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;

@RestController
public class InstrumentRxController {

   private final ClientService clientService;
   private final InstrumentsService instrumentsService;
   private final PriceService priceService;

   @Autowired
   public InstrumentRxController(ClientService clientService,
                                 InstrumentsService instrumentsService,
                                 PriceService priceService) {
      this.clientService = clientService;
      this.instrumentsService = instrumentsService;
      this.priceService = priceService;
   }

   @RequestMapping("/rx/client/{clientId}/instruments")
   public DeferredResult<Response> getFavouriteInstruments(@PathVariable("clientId") int clientId) {
      Observable<Boolean> canTradeObservable = Observable.fromCallable(() -> clientService.hasPermissionToCryptoCurrency(clientId))
                                                         .subscribeOn(Schedulers.io());

      Observable<Instrument> instrumentObservable = Observable.fromCallable(() -> instrumentsService.getFavouriteInstruments(clientId))
                                                                          .subscribeOn(Schedulers.io())
                                                                          .flatMapIterable(x -> x);

      Observable<List<PricedInstrument>> pricedInstrumentsObservable = instrumentObservable
            .flatMap(instrument -> Observable.fromCallable(
                  () -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode()))).subscribeOn(Schedulers.io()))
            .toList()
            .toObservable();


      final Observable<Response> result = Observable.zip(pricedInstrumentsObservable, canTradeObservable,
            (instruments, canTrade) -> new Response(clientId, instruments, canTrade));


      DeferredResult<Response>  deferredResult = new DeferredResult<>();
      result.subscribe(deferredResult::setResult, deferredResult::setErrorResult);
      return deferredResult;
   }


}
