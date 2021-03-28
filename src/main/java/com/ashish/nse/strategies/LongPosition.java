package com.ashish.nse.strategies;

import com.ashish.nse.data.Account;
import com.ashish.nse.data.DataCache;

public class LongPosition implements Position{

    private String stock;
    private long noOfShares;
    private double stopLoss;
    private double longWith;
    private DataCache cache = DataCache.getDataCache();

    private Account longAccount;

    public LongPosition(String stock, long noOfShares, double stopLoss ){
        this.stock = stock;
        this.noOfShares = noOfShares;
        this.stopLoss = stopLoss;
        cache.loadDataOf(stock,stock);
    }

    @Override
    public void addShortPosition(final Account accountToShort) {
        //  unused here
    }

    @Override
    public void addLongPosition(final Account accountToLong) {
        this.longAccount = accountToLong;
    }

    @Override
    public void shortWith(double shortNumber) {
        // unused here
    }

    @Override
    public void longWith(double longNumber) {
        this.longWith = longNumber;
    }

    @Override
    public void processPosition() {
        cache.getDataFor(stock).forEach(tickData -> {
            double open = tickData.getOpen();
            double longPrice = open - longWith;

            double longValue = longPrice * noOfShares;

            if (longValue > longAccount.getFunds()) {
                System.out.println("Margin insufficient to long, required = " + longValue + " but current having =" + longAccount.getFunds() + " on date " + tickData.getDate());
            }
            boolean longStatus = longPrice >= tickData.getLow();
            if (longStatus) {
                double sellikAtClose = 0.0;
                //apply stop loss for short sell
                if (longPrice - tickData.getLow() >= stopLoss) {
                    System.out.println("We have to take stop loss as its goes down from " + longPrice + " :-(" + " on " + tickData.getDate());
                    sellikAtClose = (longPrice - stopLoss) * noOfShares;
                } else {
                    sellikAtClose = tickData.getClose() * noOfShares;
                }
                double todaysGainOrLoss = sellikAtClose - longValue;
                if (todaysGainOrLoss < 0) {
                    System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                } else {
                    System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                }
                double accumulatedGainOrLoss = longAccount.getTotalGainsOrLoss();
                accumulatedGainOrLoss += todaysGainOrLoss;
                longAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);
                // System.out.println("Accumulated profit/loss " + account.getTotalGainsOrLoss() + " on " + tickData.getDate());
            } else {
                System.out.println("Long position could not taken on " + tickData.getDate());
            }
        });

        System.out.println("Accumulated profit/loss " + longAccount.getTotalGainsOrLoss());
    }

}
