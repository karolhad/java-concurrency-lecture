package com.hadala.concurrency.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Client {
   private int id;
   private String firstName;
   private String lastName;
   private String currency;

   public Client(int id, String firstName, String lastName, String currency) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.currency = currency;
   }

   public long getId() {
      return id;
   }

   public String getFirstName() {
      return firstName;
   }

   public String getLastName() {
      return lastName;
   }

   public String getCurrency() {
      return currency;
   }
}
