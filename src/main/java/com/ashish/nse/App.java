package com.ashish.nse;

import com.ashish.nse.data.Account;
import com.ashish.nse.strategies.*;

public class App {

    public static void main(String[] args) {
        System.out.println(.3*.3);
        if (args.length == 0) {
            System.out.println("Please provide data input to process position...");
        }
        String stock = "src/main/resource/TATASTEEL.NS.csv";
        double stopLoss = 2.0;
        long shares = 200;
        Position position = getPosition(stock, shares, stopLoss, Strategy.HEDGE);
        position.processPosition();
    }

    private static Position getPosition(String stock, long noOfShares, double stopLoss, Strategy strategy) {
        Position position = null;
        switch (strategy) {
            case HEDGE:
                position = new Hedging(stock, noOfShares, stopLoss);
                position.addLongPosition(new Account("Ashish", "007", 200000));
                position.addShortPosition(new Account("Vijay", "008", 200000));
                position.longWith(2.5);
                position.shortWith(2.5);
                break;
            case LONG:
                position = new LongPosition(stock, noOfShares, stopLoss);
                position.addLongPosition(new Account("Ashish", "007", 100000));
                position.longWith(2.0);
                break;
            case SHORT:
                position = new ShortPosition(stock, noOfShares, stopLoss);
                position.addShortPosition(new Account("Vijay", "008", 100000));
                position.shortWith(2.0);
                break;
            default:
                System.out.println("Incorrect position!");
        }
        return position;
    }
}
