package com.ast.roundtracker.model;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;

@IgnoreExtraProperties
public class User implements Comparable {

    private String userName;
    private String userId;
    private int totalPurchased;
    private int totalReceived;

    public User() {
    }

    public User(String userName, String userId, int totalPurchased, int totalReceived) {
        this.userName = userName;
        this.userId = userId;
        this.totalPurchased = totalPurchased;
        this.totalReceived = totalReceived;
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

    public int getBalance() {
        return totalReceived - totalPurchased;
    }

    public int getTotalPurchased() {
        return totalPurchased;
    }

    public void setTotalPurchased(int totalPurchased) {
        this.totalPurchased = totalPurchased;
    }

    public int getTotalReceived() {
        return totalReceived;
    }

    public void setTotalReceived(int totalReceived) {
        this.totalReceived = totalReceived;
    }

    public static long getCreditScore(int totalPurchased, int totalReceived) {
        Double multiplier = 1.0;
        if(totalPurchased > 0 && totalReceived > 0) {
            multiplier = multiplier + ((totalPurchased - totalReceived) * 0.15);
        }
        return Math.round(((totalPurchased * 5) * multiplier)/2);
    }

    @Override
    public String toString() {
        return userName;
    }

    @Override
    public int compareTo(Object o) {
        if(((User) o).getBalance() < this.getBalance()) {
            return 1;
        } else if(((User) o).getBalance() > this.getBalance()) {
            return -1;
        } else {
            return 0;
        }
    }

    public static Comparator<User> userCreditScoreComparator = new Comparator<User>() {
        public int compare(User user1, User user2) {
            Long creditScore1 = getCreditScore(user1.getTotalPurchased(), user1.getTotalReceived());
            Long creditScore2 = getCreditScore(user2.getTotalPurchased(), user2.getTotalReceived());
            return creditScore2.compareTo(creditScore1);
        }
    };
}
