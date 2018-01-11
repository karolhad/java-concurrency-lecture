package com.hadala;

import com.google.common.collect.Iterables;
import com.hadala.concurrency.dao.*;
import com.hadala.concurrency.model.Instrument;
import com.hadala.concurrency.model.PricedInstrument;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.hadala.concurrency.Math.fibonacci;


public class Notes {

   private static final Logger LOG = Logger.getLogger(Notes.class);
   final ExecutorService executorService = Executors.newCachedThreadPool();
   ClientService clientService = new SimpleClientService();
   InstrumentsService instrumentsService = new SimpleInstrumentsService();
   PriceService priceService = new SimplePriceService();
   AccountService accountService = new AccountService();

   @Test
   public void syncCall1() {
      final boolean canTrade = clientService.hasPermissionToCryptoCurrency(102);
      LOG.info(canTrade);
   }

   @Test
   public void syncCall2() {
      final int clientId = 101;
      final boolean canTrade = clientService.hasPermissionToCryptoCurrency(clientId);

      final Collection<Instrument> favouriteInstruments = instrumentsService.getFavouriteInstruments(clientId);

      LOG.info("Instruments: " + favouriteInstruments + ", canTrade=" + canTrade);
   }

   @Test
   public void concurrentCallFuture() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final Future<Object> canTradeFuture = executorService.submit(() -> clientService.hasPermissionToCryptoCurrency(clientId));
      LOG.info("after future 1 created");
      final Future<Collection<Instrument>> instrumentsFuture = executorService
            .submit(() -> instrumentsService.getFavouriteInstruments(clientId));
      LOG.info("after future 2 created");


      final Collection<Instrument> instruments = instrumentsFuture.get();
      LOG.info("after instruments.get");

      final Object canTrade = canTradeFuture.get();
      LOG.info("after canTradeFuture.get");


      LOG.info("Instruments: " + instruments + ", canTrade=" + canTrade);

   }

   @Test
   public void syncCall3() {
      final int clientId = 101;
      final boolean canTrade = clientService.hasPermissionToCryptoCurrency(clientId);

      final Collection<Instrument> favouriteInstruments = instrumentsService.getFavouriteInstruments(clientId);

      final List<PricedInstrument> pricedInstruments = favouriteInstruments
            .stream()
            .map(instrument -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode())))
            .collect(Collectors.toList());


      LOG.info("Instruments: " + pricedInstruments + ", canTrade=" + canTrade);
   }

   @Test
   public void concurrentCallFuture3() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final Future<Object> canTradeFuture = executorService.submit(() -> clientService.hasPermissionToCryptoCurrency(clientId));
      final Future<Collection<Instrument>> instrumentsFuture = executorService
            .submit(() -> instrumentsService.getFavouriteInstruments(clientId));

      final Collection<Instrument> instruments = instrumentsFuture.get();

      ForkJoinPool pool = new ForkJoinPool(4);

      final List<PricedInstrument> pricedInstruments = pool.submit(() ->
            instruments
                  .parallelStream()
                  .map(instrument -> new PricedInstrument(instrument, priceService.getPrice(instrument.getCode())))
                  .collect(Collectors.toList())
      ).get();

      final Object canTrade = canTradeFuture.get();

      LOG.info("Instruments: " + pricedInstruments + ", canTrade=" + canTrade);
   }

   @Test
   public void concurrentGetBalanceFuture() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final Future<Collection<Instrument>> instrumentsFuture = executorService
            .submit(() -> instrumentsService.getFavouriteInstruments(clientId));
      final Future<Integer> defaultAccountIdFuture = executorService.submit(() -> clientService.getDefaultAccountId(clientId));

      final Integer accountId = defaultAccountIdFuture.get();

      final Future<BigDecimal> balanceFuture = executorService.submit(() -> accountService.getBalance(accountId));
      final Collection<Instrument> instruments = instrumentsFuture.get();

      LOG.info("Instruments: " + instruments + ", balance=" + balanceFuture.get());
   }


   @Test
   public void concurrentGetBalanceCallableFuture() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final CompletableFuture<Integer> defaultAccountIdFuture = CompletableFuture
            .supplyAsync(() -> clientService.getDefaultAccountId(clientId), executorService);
      final CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture
            .supplyAsync(() -> instrumentsService.getFavouriteInstruments(clientId), executorService);


      final CompletableFuture<BigDecimal> balanceFuture = defaultAccountIdFuture.thenApplyAsync(
            accountService::getBalance, executorService);


      LOG.info("Instruments: " + instrumentsFuture.get() + ", balance=" + balanceFuture.get());
   }

   @Test
   public void concurrentCallableFuturePricedInstruments() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final CompletableFuture<Object> canTradeFuture = CompletableFuture
            .supplyAsync(() -> clientService.hasPermissionToCryptoCurrency(clientId), executorService);
      final CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture
            .supplyAsync(() -> instrumentsService.getFavouriteInstruments(clientId), executorService);

      ForkJoinPool pool = new ForkJoinPool(4);

      final CompletableFuture<List<PricedInstrument>> pricedInstruments = instrumentsFuture.thenApplyAsync(
            instruments -> {
               List<PricedInstrument> pricedInstruments1 = null;
               try {
                  pricedInstruments1 = pool.submit(() ->
                        instruments.parallelStream()
                                   .map(instrument -> new PricedInstrument(instrument, priceService
                                         .getPrice(instrument.getCode())))
                                   .collect(Collectors.toList())

                  ).get();
               } catch (InterruptedException e) {
                  e.printStackTrace();
               } catch (ExecutionException e) {
                  e.printStackTrace();
               }
               return pricedInstruments1;
            }
      );


      final Object canTrade = canTradeFuture.get();

      LOG.info("Instruments: " + pricedInstruments.get() + ", canTrade=" + canTrade);
   }


   @Test
   public void concurrentCallableFuturePricedInstrumentsFixed() throws ExecutionException, InterruptedException {
      final int clientId = 101;
      final CompletableFuture<Object> canTradeCryptoFuture = CompletableFuture
            .supplyAsync(() -> clientService.hasPermissionToCryptoCurrency(clientId), executorService);
      final CompletableFuture<Collection<Instrument>> instrumentsFuture = CompletableFuture
            .supplyAsync(() -> instrumentsService.getFavouriteInstruments(clientId), executorService);

      final CompletableFuture<List<PricedInstrument>> pricedInstruments = instrumentsFuture
            .thenCompose(this::getPricedInstruments);

      final Object canTrade = canTradeCryptoFuture.get();

      LOG.info("Instruments: " + pricedInstruments.get() + ", canTrade=" + canTrade);
   }

   private CompletableFuture<List<PricedInstrument>> getPricedInstruments(Collection<Instrument> instruments) {

      final List<CompletableFuture<PricedInstrument>> listOfFutures = instruments.stream()
                                                                                 .map(this::getPricedInstrumentAsync)
                                                                                 .collect(Collectors.toList());


      final CompletableFuture<List<PricedInstrument>> pricedInstrumentsFuture = CompletableFuture
            .allOf(Iterables.toArray(listOfFutures, CompletableFuture.class))
            .thenApply(v -> listOfFutures
                  .stream()
                  .map(CompletableFuture::join)
                  .collect(Collectors.toList())
            );

      return pricedInstrumentsFuture;

   }

   private CompletableFuture<PricedInstrument> getPricedInstrumentAsync(Instrument instrument) {
      return CompletableFuture.supplyAsync(() -> priceService.getPrice(instrument.getCode()))
                              .thenApply(price -> new PricedInstrument(instrument, price));
   }


   @Test
   public void concurrentGetBalanceRxJava() {
      final int clientId = 101;

      final Observable<Integer> defaultAccountIdObservable = Observable
            .fromCallable(
                  () -> clientService.getDefaultAccountId(clientId))
            .subscribeOn(Schedulers.io());

      final Observable<Collection<Instrument>> instrumentsObservable = Observable
            .fromCallable(() -> instrumentsService.getFavouriteInstruments(clientId))
            .subscribeOn(Schedulers.io());

      final Observable<BigDecimal> balanceObservable =
            defaultAccountIdObservable.flatMap(accountId ->
                  Observable.fromCallable(() -> accountService.getBalance(accountId))
                            .subscribeOn(Schedulers.io())
            );

      final Observable<String> resultObservable = Observable.zip(instrumentsObservable, balanceObservable,
            (instruments, balance) -> "Instruments: " + instrumentsObservable + ", balance=" + balance
      );

      resultObservable.subscribe(LOG::info);

      resultObservable.blockingFirst();
   }


   @Test
   public void rxJavaSample1() {
      final Observable<Integer> range = Observable.range(1, 100);

      final Observable<Integer> range2 = range.map(x -> x * x);

      final Observable<String> letters = Observable.just("A", "B", "C");


      final Observable<String> result = range2.zipWith(letters, (number, letter) -> number + "->" + letter);


      result.subscribe(System.out::println);

      Sleep.sleep(2000);
   }

   @Test
   public void rxJavaSample2() {
      final Observable<Object> numbersObservable = Observable.create((emitter) -> {
         int n = 0;

         while (!emitter.isDisposed()) {
            emitter.onNext(fibonacci(n++));
         }
      });


      numbersObservable.take(20).subscribe(System.out::println);

      Sleep.sleep(2000);
   }

   @Test
   public void rxJavaSample3() {
      final Observable<Long> numbersObservable = Observable.create((emitter) -> {
         long n = 0;

         while (!emitter.isDisposed()) {
            emitter.onNext(n++);
         }
      });


      numbersObservable.observeOn(Schedulers.computation())
                       .filter(this::analyze)
                       .subscribe(System.out::println);

      Sleep.sleep(50_000);
   }

   @Test
   public void rxJavaSample4() {
      final Flowable<Long> numbersObservable = Flowable.create((emitter) -> {
         long n = 0;

         while (!emitter.isCancelled()) {
            emitter.onNext(n++);
         }
      }, BackpressureStrategy.DROP);

      numbersObservable.observeOn(Schedulers.computation())
                       .filter(this::analyze)
                       .subscribe(System.out::println);

      Sleep.sleep(50_000);
   }

   private boolean analyze(long number) {
      Sleep.sleep(100);
      return number % 2 == 0;
   }


//
//   @Test
//   public void rxJavaSample2() {
//      Observable.create((emitter) -> {
//
//         while(emitter.isDisposed()) {
//
//         }
//
//      });
//
//   }


}
