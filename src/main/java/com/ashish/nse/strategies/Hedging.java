package com.ashish.nse.strategies;

import com.ashish.nse.data.Account;
import com.ashish.nse.data.DataCache;
import com.ashish.nse.data.TickData;

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
            double longValue = longPrice * noOfShares;

            double shortPrice = open + shortWith;
            double shortValue = shortPrice * noOfShares;

            if (shortValue > shortAccount.getFunds()) {
                System.out.println("Margin insufficient to short, required = " + shortValue + " but current having =" + shortAccount.getFunds()+" on date " + tickData.getDate());
                return;
            }
            if (shortValue > longAccount.getFunds()) {
                System.out.println("Margin insufficient to long, required = " + shortValue + " but current having =" + longAccount.getFunds()+" on date " + tickData.getDate());
                return;
            }
            boolean shortStatus = shortPrice <= tickData.getHigh();
            boolean longStatus = longPrice >= tickData.getLow();
            if (shortStatus && longStatus){
                luckyDay(tickData, longPrice, shortPrice, longValue, shortValue);
            }else if(shortStatus && !longStatus){
                onlyShort(tickData, shortPrice, shortValue);
            }else if(!shortStatus && longStatus){
                onlyLong(tickData, longPrice, longValue);
            }
/*
            System.out.println("(Long) Accumulated profit/loss in "+longAccount.getAccountName()+" "+ longAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());
            System.out.println("(Short)Accumulated profit/loss in  "+shortAccount.getAccountName()+" "+shortAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());
*/
        });
        System.out.println("Total Profit in "+shortAccount.getAccountName()+" is " +shortAccount.getTotalGainsOrLoss()+ " and account value "+(shortAccount.getFunds()+shortAccount.getTotalGainsOrLoss()));
        System.out.println("Total Profit in "+longAccount.getAccountName()+" is " +longAccount.getTotalGainsOrLoss()+ " and account value "+(longAccount.getFunds()+longAccount.getTotalGainsOrLoss()));
        System.out.println("Invested "+(shortAccount.getFunds()+longAccount.getFunds()));
        System.out.println("Returns "+(shortAccount.getTotalGainsOrLoss()+longAccount.getTotalGainsOrLoss()));
        System.out.println("Returns "+(shortAccount.getTotalGainsOrLoss()+longAccount.getTotalGainsOrLoss())/(shortAccount.getFunds()+longAccount.getFunds())*100+" %");
    }

    private void onlyLong(TickData tickData, double longPrice, double longValue) {
        //System.out.println("short position not performed, only long applicable");
        double sellOut = 0.0;
        //apply stop loss for short sell
        if (longPrice - tickData.getLow() >= stopLoss) {
            //System.out.println("We have to take stop loss as its goes down from " + longPrice + " :-(" + " on " + tickData.getDate());
            sellOut = (longPrice - stopLoss) * noOfShares;
        } else {
            sellOut = tickData.getClose() * noOfShares;
        }
        double todaysGainOrLoss = sellOut - longValue;
        if (todaysGainOrLoss < 0) {
            //System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        } else {
            //System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        }
        double accumulatedGainOrLoss = longAccount.getTotalGainsOrLoss();
        accumulatedGainOrLoss += todaysGainOrLoss;
        longAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);
    }

    private void onlyShort(com.ashish.nse.data.TickData tickData, double shortPrice, double shortValue) {
        //System.out.println("long position not performed, only short applicable");
        double buyBack=0.0;
        //apply stop loss for short sell
        if(tickData.getHigh() - shortPrice >= stopLoss){
           // System.out.println("We have to take stop loss as its goes up from "+shortPrice+" :-("+" on "+tickData.getDate());
            buyBack = (shortPrice + stopLoss) * noOfShares;
        }else{
            buyBack = tickData.getClose() * noOfShares;
        }
        double todaysGainOrLoss = shortValue - buyBack;
        if(todaysGainOrLoss<0) {
            //System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        }else{
           // System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        }
        double accumulatedGainOrLoss = shortAccount.getTotalGainsOrLoss();
        accumulatedGainOrLoss+=todaysGainOrLoss;
        shortAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);
    }

    private void luckyDay(com.ashish.nse.data.TickData tickData, double longPrice, double shortPrice, double longValue, double shortValue) {

        double profit = shortValue - longValue;
        double previousGainOrLossOnShortAccount = shortAccount.getTotalGainsOrLoss();
        previousGainOrLossOnShortAccount+=profit;
        shortAccount.setTotalGainsOrLoss(previousGainOrLossOnShortAccount);

        double previousGainOrLossOnLongAccount = longAccount.getTotalGainsOrLoss();
        previousGainOrLossOnLongAccount += 0;
        longAccount.setTotalGainsOrLoss(previousGainOrLossOnLongAccount);

        /*double buyAtClose=0.0;
        //apply stop loss for short sell
        if(tickData.getHigh() - shortPrice >= stopLoss){
            //System.out.println("We have to take stop loss as its goes up from "+shortPrice+" :-("+" on "+tickData.getDate());
            buyAtClose = (shortPrice + stopLoss) * noOfShares;
        }else{
            buyAtClose = tickData.getClose() * noOfShares;
        }
        double todaysGainOrLoss = shortValue - buyAtClose;
        if(todaysGainOrLoss<0) {
          // System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        }else{
           // System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysGainOrLoss);
        }
        double accumulatedGainOrLoss = shortAccount.getTotalGainsOrLoss();
        accumulatedGainOrLoss+=todaysGainOrLoss;
        shortAccount.setTotalGainsOrLoss(accumulatedGainOrLoss);
        // System.out.println("Accumulated profit/loss in short account "+shortAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());

        double sellikAtClose = 0.0;
        //apply stop loss for short sell
        if (longPrice - tickData.getLow() >= stopLoss) {
            //System.out.println("We have to take stop loss as its goes down from " + longPrice + " :-(" + " on " + tickData.getDate());
            sellikAtClose = (longPrice - stopLoss) * noOfShares;
        } else {
            sellikAtClose = tickData.getClose() * noOfShares;
        }
        double todaysLongGainOrLoss = sellikAtClose - longValue;
        if (todaysGainOrLoss < 0) {
           // System.err.println("We have loss on " + tickData.getDate() + " of Rs " + todaysLongGainOrLoss);
        } else {
           // System.out.println("We have profit on " + tickData.getDate() + " of Rs " + todaysLongGainOrLoss);
        }
        double accumulatedLongGainOrLoss = longAccount.getTotalGainsOrLoss();
        accumulatedLongGainOrLoss += todaysLongGainOrLoss;
        longAccount.setTotalGainsOrLoss(accumulatedLongGainOrLoss);*/
    }

}
