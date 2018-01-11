package com.hadala.concurrency.dao;

import org.springframework.stereotype.Service;

import static com.hadala.concurrency.dao.Sleep.sleep;

@Service
public class SimpleClientService implements ClientService {
   @Override
   public boolean hasPermissionToCryptoCurrency(int clientId) {
      sleep(1000);
      switch (clientId) {
         case 101:
            return true;
         case 102:
            return false;
         case 103:
            return true;
         default:
            return false;
      }
   }

   @Override
   public int leverageLevel(int clientId) {
      sleep(1000);
      return clientId % 10;
   }

   @Override
   public int getDefaultAccountId(int clientId) {
      sleep(200);
      return clientId * 17;
   }
}
