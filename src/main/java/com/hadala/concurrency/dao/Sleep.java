package com.hadala.concurrency.dao;

public class Sleep {

   public static void sleep(int duration) {
      try {
         Thread.sleep(duration);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
