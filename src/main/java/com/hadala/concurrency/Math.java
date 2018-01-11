package com.hadala.concurrency;

public class Math {
   public static long fibonacci(int n) {
      if (n <= 1) {
         return n;
      }
      long fibo = 1;
      long fiboPrev = 1;
      for (int i = 2; i < n; ++i) {
         long temp = fibo;
         fibo += fiboPrev;
         fiboPrev = temp;
      }
      return fibo;
   }
}
