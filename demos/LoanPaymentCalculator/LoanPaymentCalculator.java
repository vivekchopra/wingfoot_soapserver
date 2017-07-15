/*
 * Copyright (c) Wingfoot Software Inc. All Rights Reserved.
 * Please see http://www.wingfoot.com for license details.
 */

package com.wingfoot.samples;

import java.lang.Math;

/**
 * Loan Payment Calculator Web Service
 */
public class LoanPaymentCalculator {

    /**
     * Compute monthly payments on the loan
     * @param loanAmount Amount of the loan
     * @param interest   Interest on the loan
     * @param term       Term for the loan in months
     *
     * @return The monthly loan payment
     */
    public static double compute (double loanAmount,
                                  double interest,
                                  double term) throws Exception {

        if (term <= 0 || interest <= 0 || loanAmount <= 0) {
             throw new Exception ("Invalid values for the amount, interest or term.");
        }

        double interestRate   = interest/100;
        double periods        = 12; /* number of payment periods in a year */
           
        double monthlyPayment = 
               (loanAmount * (interestRate)) / 
               (periods*(1- Math.pow ((1+(interestRate/periods)), -term)));

        long roundedValue = Math.round (monthlyPayment);

        return roundedValue;
    }

    /**
     * Test code for Web Service
     */

    public static void main (String args[]) 
    throws Exception {

       if (args.length != 3) {
          System.err.println ("usage: java com.wingfoot.samples.LoanPaymentCalculator <amount> <interest> <term>");
          System.exit (1);
       }

       double loanAmount = 0.0;
       double interest   = 0.0;
       double term       = 0.0;
       try {
          loanAmount = (new Double (args[0])).doubleValue ();
          interest   = (new Double (args[1])).doubleValue ();
          term       = (new Double (args[2])).doubleValue ();
       } catch (NumberFormatException e) {
          System.err.println ("One of the input values is not a number");
          System.err.println ("usage: java com.wingfoot.parvus.samples.LoanPaymentCalculator <amount> <interest> <term>");
          System.exit (1);
       }

       double paymentAmount = LoanPaymentCalculator.compute (loanAmount, interest, term);

       System.out.println ("The monthly payment on an amount of " + loanAmount
                           + " an an interest rate of " + interest
                           + "% for " + term + " months is " 
                           + paymentAmount);
    }
}
