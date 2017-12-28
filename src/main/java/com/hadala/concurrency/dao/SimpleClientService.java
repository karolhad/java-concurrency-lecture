package com.hadala.concurrency.dao;

import com.hadala.concurrency.model.Client;
import org.springframework.stereotype.Service;

import static com.hadala.concurrency.dao.Sleep.sleep;

@Service
public class SimpleClientService implements ClientService {
   @Override
   public Client getClient(int clientId) {
      sleep(1000);
      switch (clientId) {
         case 101:
            return new Client(clientId, "Joe", "Doe", "USD");
         case 102:
            return new Client(clientId,"John", "Englishman", "GBP");
         case 103:
            return new Client(clientId,"Jan", "Kowalski", "PLN");
         default:
            return null;
      }
   }
}
