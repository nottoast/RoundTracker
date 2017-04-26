package com.ast.roundtracker.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class User implements Comparable {

    private String userName;
    private String userId;
    private String linkedUserId;
    private int totalPurchased;
    private int totalReceived;

    public User() {
    }

    public User(String userName, String userId, int totalPurchased, int totalReceived, String linkedUserId) {
        this.userName = userName;
        this.userId = userId;
        this.totalPurchased = totalPurchased;
        this.totalReceived = totalReceived;
        this.linkedUserId = linkedUserId;
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

    public String getLinkedUserId() {
        return linkedUserId;
    }

    public void setLinkedUserId(String linkedUserId) {
        this.linkedUserId = linkedUserId;
    }

    public static Integer getBalance(int totalPurchased, int totalReceived) {
        return totalReceived - totalPurchased;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("userName", userName);
        result.put("linkedUserId", linkedUserId);
        result.put("totalPurchased", totalPurchased);
        result.put("totalReceived", totalReceived);
        return result;
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
            Integer creditScore1 = user1.getTotalPurchased() - getBalance(user1.getTotalPurchased(), user1.getTotalReceived());
            Integer creditScore2 = user2.getTotalPurchased() - getBalance(user2.getTotalPurchased(), user2.getTotalReceived());
            return creditScore2.compareTo(creditScore1);
        }
    };
}
