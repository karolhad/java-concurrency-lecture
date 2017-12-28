package com.hadala.concurrency.dao;

import com.hadala.concurrency.model.Price;

public interface PriceService {
   Price getPrice(String instrumentCode);
}


