package com.hadala.concurrency.dao;

import com.google.common.collect.ImmutableMap;
import com.hadala.concurrency.model.Price;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import static com.hadala.concurrency.dao.Sleep.sleep;

@Service
public class SimplePriceService implements PriceService {

   private final static Map<String, Double> PRICES = ImmutableMap.of(
         "GBP", 1.34352,
         "GLD", 1424.4,
         "BTC", 17112.0,
         "ETH", 658.0
   );

   @Override
   public Price getPrice(String instrumentCode) {
      sleep(500);
      final Double basePrice = PRICES.get(instrumentCode);
      return new Price(
            BigDecimal.valueOf(1.01 * basePrice).setScale(2, RoundingMode.FLOOR),
            BigDecimal.valueOf(0.99 * basePrice).setScale(2, RoundingMode.FLOOR)
      );
   }
}
