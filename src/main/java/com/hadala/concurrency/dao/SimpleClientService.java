package com.hadala.concurrency.dao;

import org.springframework.stereotype.Service;

import static com.hadala.concurrency.dao.Sleep.sleep;

@Service
public class SimpleClientService implements ClientService {
   @Override
   public boolean hasPermissionToTrade(int clientId) {
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
}
