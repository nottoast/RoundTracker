package com.ast.roundtracker.model;

public class Ledger implements Comparable {

    private String ledgerId;
    private String ledgerName;
    private Object originalTimestamp;
    private boolean active;

    public Ledger() {
    }

    public Ledger(String ledgerId, String ledgerName, Object originalTimestamp, boolean active) {
        this.ledgerId = ledgerId;
        this.ledgerName = ledgerName;
        this.originalTimestamp = originalTimestamp;
        this.active = active;
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
