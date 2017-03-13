package com.ast.roundtracker.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class LedgerEntry implements Comparable {

    private String purchaserUserId;
    private String recipientUserId;
    private int volume;
    private Object timestamp;

    private boolean dispute;
    private int keepCount;
    private int deleteCount;
    private int totalCount;
    private boolean delete;

    public LedgerEntry() {

    }

    public LedgerEntry(String purchaserUserId, String recipientUserId, int volume,
                       Object timestamp, boolean dispute, int keepCount,
                       int deleteCount, int totalCount, boolean delete) {
        this.purchaserUserId = purchaserUserId;
        this.recipientUserId = recipientUserId;
        this.volume = volume;
        this.timestamp = timestamp;
        this.dispute = dispute;
        this.keepCount = keepCount;
        this.deleteCount = deleteCount;
        this.totalCount = totalCount;
        this.delete = delete;
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

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public long getTimestamp() {
        return (Long) timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDispute() {
        return dispute;
    }

    public void setDispute(boolean dispute) {
        this.dispute = dispute;
    }

    public int getKeepCount() {
        return keepCount;
    }

    public void setKeepCount(int keepCount) {
        this.keepCount = keepCount;
    }

    public int getDeleteCount() {
        return deleteCount;
    }

    public void setDeleteCount(int deleteCount) {
        this.deleteCount = deleteCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public boolean isDelete() {
        return delete;
    }

    public void setDelete(boolean delete) {
        this.delete = delete;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("purchaserUserId", purchaserUserId);
        result.put("recipientUserId", recipientUserId);
        result.put("timestamp", timestamp);
        result.put("volume", volume);
        result.put("dispute", dispute);
        result.put("keepCount", keepCount);
        result.put("deleteCount", deleteCount);
        result.put("totalCount", totalCount);
        result.put("delete", delete);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if(((LedgerEntry) o).getTimestamp() < this.getTimestamp()) {
            return -1;
        } else if(((LedgerEntry) o).getTimestamp() > this.getTimestamp()) {
            return 1;
        } else {
            return 0;
        }
    }
}
