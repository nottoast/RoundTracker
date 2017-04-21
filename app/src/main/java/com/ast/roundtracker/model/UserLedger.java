package com.ast.roundtracker.model;

public class UserLedger {

    private String ledgerId;

    public UserLedger() {

    }

    public UserLedger(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getLedgerId() {

        return ledgerId;
    }

    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }
}
