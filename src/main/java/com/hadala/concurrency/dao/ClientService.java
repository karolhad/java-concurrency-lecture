package com.hadala.concurrency.dao;

import com.hadala.concurrency.model.Client;

public interface ClientService {
   Client getClient(int clientId);
}
