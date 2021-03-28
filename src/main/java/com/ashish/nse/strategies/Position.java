package com.ashish.nse.strategies;

import com.ashish.nse.data.Account;

public interface Position {

    public void processPosition();
    public void addShortPosition(Account accountToShort);
    public void addLongPosition(Account accountToLong);
    public void shortWith(double shortNumber);
    public void longWith(double longNumber);
}
