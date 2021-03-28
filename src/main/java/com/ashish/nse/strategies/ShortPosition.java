package com.ashish.nse.strategies;

import com.ashish.nse.data.Account;
import com.ashish.nse.data.DataCache;

public class ShortPosition implements Position{

    private long noOfShares;
    private double stopLoss;
    private double shortWith;
    private String stock;
    private DataCache cache = DataCache.getDataCache();

    private Account shortAccount;

    public ShortPosition(String stock, long noOfShares, double stopLoss ){
        this.stock = stock;
        this.noOfShares = noOfShares;
        this.stopLoss = stopLoss;
        cache.loadDataOf(stock,stock);
    }

    @Override
    public void processPosition() {
        cache.getDataFor(stock).forEach( tickData -> {
            double open = tickData.getOpen();
            double shortPrice = open + shortWith;

            double shortValue = shortPrice * noOfShares;

            if (shortValue > shortAccount.getFunds()) {
                System.out.println("Margin insufficient to short, required = " + shortValue + " but current having =" + shortAccount.getFunds()+" on date " + tickData.getDate());
            }
            boolean shortStatus = shortPrice <= tickData.getHigh();
            if (shortStatus){
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
                System.out.println("Accumulated profit/loss "+ shortAccount.getTotalGainsOrLoss()+" on "+tickData.getDate());
            }else{
                System.out.println("Shorting not performed on "+tickData.getDate());
            }
        });

    }

    @Override
    public void addShortPosition(Account accountToShort) {
        this.shortAccount = accountToShort;
    }

    @Override
    public void addLongPosition(Account accountToLong) {
        // unused here
    }

    @Override
    public void shortWith(double shortNumber) {
        this.shortWith = shortNumber;
    }

    @Override
    public void longWith(double longNumber) {
       // unused here
    }
}
