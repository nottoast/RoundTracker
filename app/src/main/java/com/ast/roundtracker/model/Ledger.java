package com.ast.roundtracker.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class Ledger implements Comparable {

    private String ledgerId;
    private String ledgerName;
    private Object originalTimestamp;
    private boolean active;
    private String adminId;
    private String inviteCode;

    public Ledger() {
    }

    public Ledger(String ledgerId, String ledgerName, Object originalTimestamp,
                  boolean active, String adminId, String inviteCode) {
        this.ledgerId = ledgerId;
        this.ledgerName = ledgerName;
        this.originalTimestamp = originalTimestamp;
        this.active = active;
        this.adminId = adminId;
        this.inviteCode = inviteCode;
    }

    public String getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getLedgerName() {
        return ledgerName;
    }

    public void setLedgerName(String ledgerName) {
        this.ledgerName = ledgerName;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getOriginalTimestamp() {
        return (long) originalTimestamp;
    }

    public void setOriginalTimestamp(Object originalTimestamp) {
        this.originalTimestamp = originalTimestamp;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ledgerId", ledgerId);
        result.put("ledgerName", ledgerName);
        result.put("originalTimestamp", originalTimestamp);
        result.put("active", active);
        result.put("adminId", adminId);
        result.put("inviteCode", inviteCode);
        return result;
    }

    @Override
    public int compareTo(Object o) {
        if(((Ledger) o).getOriginalTimestamp() < this.getOriginalTimestamp()) {
            return 1;
        } else if(((Ledger) o).getOriginalTimestamp() > this.getOriginalTimestamp()) {
            return -1;
        } else {
            return 0;
        }
    }

}
