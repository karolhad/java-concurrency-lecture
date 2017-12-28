package com.hadala.concurrency.model;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.math.BigDecimal;

@ToString
@EqualsAndHashCode
public class Client {
   private int id;
   private String firstName;
   private String lastName;
   private BigDecimal money;

   public Client(int id, String firstName, String lastName, BigDecimal money) {
      this.id = id;
      this.firstName = firstName;
      this.lastName = lastName;
      this.money = money;
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

   public BigDecimal getMoney() {
      return money;
   }
}
