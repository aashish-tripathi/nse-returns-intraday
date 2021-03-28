package com.ashish.nse.data;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private String accountName;
    private String accountNumber;
    private double funds;
    private double totalGainsOrLoss;
    private long margin;
    private List<String> segments;

    public Account(String accountName, String accountNumber, double funds) {
        this.accountName = accountName;
        this.accountNumber = accountNumber;
        this.funds = funds;
        this.segments = new ArrayList<>();
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public double getFunds() {
        return funds;
    }

    public void setFunds(double funds) {
        this.funds = funds;
    }

    public long getMargin() {
        return margin;
    }

    public void setMargin(long margin) {
        this.margin = margin;
    }

    public List<String> getSegments() {
        return segments;
    }

    public void setSegments(List<String> segments) {
        this.segments = segments;
    }

    public synchronized double getTotalGainsOrLoss() {
        return totalGainsOrLoss;
    }

    public synchronized void setTotalGainsOrLoss(double totalGainsOrLoss) {
        this.totalGainsOrLoss = totalGainsOrLoss;
    }
}
