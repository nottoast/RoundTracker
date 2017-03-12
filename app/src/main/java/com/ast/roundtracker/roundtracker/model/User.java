package com.ast.roundtracker.roundtracker.model;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class User implements Comparable {

    private String userName;
    private String userId;
    private int balance;
    private int totalPurchased;
    private int totalRecieved;

    public User() {

    }

    public User(String userName, String userId, int balance, int totalPurchased, int totalRecieved) {
        this.userName = userName;
        this.userId = userId;
        this.balance = balance;
        this.totalPurchased = totalPurchased;
        this.totalRecieved = totalRecieved;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getTotalPurchased() {
        return totalPurchased;
    }

    public void setTotalPurchased(int totalPurchased) {
        this.totalPurchased = totalPurchased;
    }

    public int getTotalRecieved() {
        return totalRecieved;
    }

    public void setTotalRecieved(int totalRecieved) {
        this.totalRecieved = totalRecieved;
    }

    @Override
    public String toString() {
        return userName;
    }

    @Override
    public int compareTo(Object o) {
        if(((User) o).getBalance() < this.balance) {
            return 1;
        } else if(((User) o).getBalance() > this.balance) {
            return -1;
        } else {
            return 0;
        }
    }
}
