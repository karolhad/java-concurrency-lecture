package com.hadala.concurrency.dao;

import com.hadala.concurrency.model.Instrument;

import java.util.Collection;

public interface InstrumentsService {

   Collection<Instrument> getFavouriteInstruments(int clientId);
}
