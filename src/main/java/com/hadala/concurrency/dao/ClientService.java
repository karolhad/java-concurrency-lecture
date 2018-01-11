package com.hadala.concurrency.dao;

public interface ClientService {
   boolean hasPermissionToCryptoCurrency(int clientId);
   int leverageLevel(int clientId);
   int getDefaultAccountId(int clientId);

}
