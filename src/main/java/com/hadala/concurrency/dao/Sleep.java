package com.hadala.concurrency.dao;

class Sleep {

   static void sleep(int duration) {
      try {
         Thread.sleep(duration);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
