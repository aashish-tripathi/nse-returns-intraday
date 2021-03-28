package com.ashish.nse.strategies;

import com.ashish.nse.data.Account;
import com.ashish.nse.data.DataCache;

public class Hedging implements Position{

    private final String stock;
    private long noOfShares;
    private double stopLoss;
    private double shortWith;
    private double longWith;
    private DataCache cache = DataCache.getDataCache();

    private Account shortAccount;
    private Account longAccount;

    public Hedging(String stock, long noOfShares, double stopLoss ){
        this.stock = stock;
        this.noOfShares = noOfShares;
        this.stopLoss = stopLoss;
        cache.loadDataOf(stock,stock);
    }

    @Override
    public void addShortPosition(final Account accountToShort) {
        this.shortAccount = accountToShort;
    }

    @Override
    public void addLongPosition(final Account accountToLong) {
        this.longAccount = accountToLong;
    }

    @Override
    public void shortWith(double shortNumber) {
        this.shortWith = shortNumber;
    }

    @Override
    public void longWith(double longNumber) {
        this.longWith = longNumber;
    }

    @Override
    public void processPosition() {
        cache.getDataFor(stock).forEach( tickData -> {
            double open = tickData.getOpen();

            double longPrice = open - longWith;
            double shortPrice = open + shortWith;

            double longValue = longPrice * noOfShares;
            double shortValue = shortPrice * noOfShares;

            if (shortValue > shortAccount.getFunds()) {
                System.out.println("Margin insufficient to short, required = " + shortValue + " but current having =" + shortAccount.getFunds()+" on date " + tickData.getDate());
            }
            if (shortValue > longAccount.getFunds()) {
                System.out.println("Margin insufficient to long, required = " + shortValue + " but current having =" + longAccount.getFunds()+" on date " + tickData.getDate());
            }
            boolean shortStatus = shortPrice <= tickData.getHigh();
            boolean longStatus = longPrice >= tickData.getLow();
            if (shortStatus && longStatus){
                double buyAtClose=0.0;
                //apply stop loss for short sell
                if(tickData.getHigh() - shortPrice >= stopLoss){
                    System.out.println("We have to take stop loss as its goes up from "+shortPrice+" :-("+" on "+tickData.getDate());
                    buyAtClose = (shortPrice + stopLoss) * noOfShares;
                }else{
                    buyAtClose = tickData.getClose() * noOfShares;
                }
                double todaysGainOrLoss = shortValue - buyAtClose;
                if(todaysGainOrLoss<0) {
                    System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                }else{
                    System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                }
                double accumulatedGainOrLoss = shortAccount.getTotalGainsOrLoss();
                accumulatedGainOrLoss+=todaysGainOrLoss;
                shortAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);
                // System.out.println("Accumulated profit/loss in short account "+shortAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());

                double sellikAtClose = 0.0;
                //apply stop loss for short sell
                if (longPrice - tickData.getLow() >= stopLoss) {
                    System.out.println("We have to take stop loss as its goes down from " + longPrice + " :-(" + " on " + tickData.getDate());
                    sellikAtClose = (longPrice - stopLoss) * noOfShares;
                } else {
                    sellikAtClose = tickData.getClose() * noOfShares;
                }
                double todaysLongGainOrLoss = sellikAtClose - longValue;
                if (todaysGainOrLoss < 0) {
                    System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysLongGainOrLoss);
                } else {
                    System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysLongGainOrLoss);
                }
                double accumulatedLongGainOrLoss = longAccount.getTotalGainsOrLoss();
                accumulatedLongGainOrLoss += todaysGainOrLoss;
                longAccount.setTotalGainsOrLoss(accumulatedLongGainOrLoss);
                // System.out.println("Accumulated profit/loss in long account "+longAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());

            }else if(shortStatus && !longStatus){
                System.out.println("long position not performed, only short applicable");
                double buyAtClose=0.0;
                //apply stop loss for short sell
                if(tickData.getHigh() - shortPrice >= stopLoss){
                    System.out.println("We have to take stop loss as its goes up from "+shortPrice+" :-("+" on "+tickData.getDate());
                    buyAtClose = (shortPrice + stopLoss) * noOfShares;
                }else{
                    buyAtClose = tickData.getClose() * noOfShares;
                }
                double todaysGainOrLoss = shortValue - buyAtClose;
                if(todaysGainOrLoss<0) {
                    System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                }else{
                    System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
                }
                double accumulatedGainOrLoss = shortAccount.getTotalGainsOrLoss();
                accumulatedGainOrLoss+=todaysGainOrLoss;
                shortAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);

            }else if(!shortStatus && longStatus){
                System.out.println("short position not performed, only long applicable");
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
            }
        });

        System.out.println("At end of the year total profit/loss in short account  "+shortAccount.getTotalGainsOrLoss());
        System.out.println("At end of the year total profit/loss in long account  "+longAccount.getTotalGainsOrLoss());
    }

}
