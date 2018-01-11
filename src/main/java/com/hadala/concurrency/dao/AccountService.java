package com.hadala.concurrency.dao;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

   public BigDecimal getBalance(int accountId) {
      Sleep.sleep(300);
      return BigDecimal.valueOf(accountId * 7 / 13.0d);
   }

}
