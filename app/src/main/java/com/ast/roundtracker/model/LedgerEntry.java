package com.ast.roundtracker.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class LedgerEntry implements Comparable {

    private String key;
    private String purchaserUserId;
    private String recipientUserId;
    private String addedByUserId;
    private String addedByUserName;
    private String disputedByUserId;
    private String disputedByUserName;
    private Object originalTimestamp;
    private Object latestTimestamp;
    private boolean dispute;

    public LedgerEntry() {
    }

    public LedgerEntry(String key, String purchaserUserId, String recipientUserId,
                       Object originalTimestamp, Object latestTimestamp, boolean dispute,
                       String addedByUserId, String addedByUserName,
                       String disputedByUserId, String disputedByUserName) {
        this.key = key;
        this.purchaserUserId = purchaserUserId;
        this.recipientUserId = recipientUserId;
        this.originalTimestamp = originalTimestamp;
        this.latestTimestamp = latestTimestamp;
        this.dispute = dispute;
        this.addedByUserId = addedByUserId;
        this.addedByUserName = addedByUserName;
        this.disputedByUserId = disputedByUserId;
        this.disputedByUserName = disputedByUserName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPurchaserUserId() {
        return purchaserUserId;
    }

    public void setPurchaserUserId(String purchaserUserId) {
        this.purchaserUserId = purchaserUserId;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public long getOriginalTimestamp() {
        return (long) originalTimestamp;
    }

    public void setOriginalTimestamp(Object originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public long getLatestTimestamp() {
        return (long) latestTimestamp;
    }

    public void setLatestTimestamp(Object latestTimestamp) {
        this.latestTimestamp = latestTimestamp;
    }

    public boolean isDispute() {
        return dispute;
    }

    public void setDispute(boolean dispute) {
        this.dispute = dispute;
    }

    public String getAddedByUserId() {
        if(addedByUserId == null) {
            return "Unknown";
        } else {
            return addedByUserId;
        }
    }

    public void setAddedByUserId(String addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public String getAddedByUserName() {
        if(addedByUserName == null) {
            return "Unknown";
        } else {
            return addedByUserName;
        }
    }

    public void setAddedByUserName(String addedByUserName) {
        this.addedByUserName = addedByUserName;
    }

    public String getDisputedByUserId() {
        return disputedByUserId;
    }

    public void setDisputedByUserId(String disputedByUserId) {
        this.disputedByUserId = disputedByUserId;
    }

    public String getDisputedByUserName() {
        return disputedByUserName;
    }

    public void setDisputedByUserName(String disputedByUserName) {
        this.disputedByUserName = disputedByUserName;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("key", key);
        result.put("purchaserUserId", purchaserUserId);
        result.put("recipientUserId", recipientUserId);
        result.put("originalTimestamp", originalTimestamp);
        result.put("latestTimestamp", latestTimestamp);
        result.put("dispute", dispute);
        result.put("addedByUserId", addedByUserId);
        result.put("addedByUserName", addedByUserName);
        result.put("disputedByUserId", disputedByUserId);
        result.put("disputedByUserName", disputedByUserName);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if(((LedgerEntry) o).getLatestTimestamp() < this.getLatestTimestamp()) {
            return -1;
        } else if(((LedgerEntry) o).getLatestTimestamp() > this.getLatestTimestamp()) {
            return 1;
        } else {
            return 0;
        }
    }
}
