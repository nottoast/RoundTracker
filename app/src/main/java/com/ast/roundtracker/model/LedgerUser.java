package com.ast.roundtracker.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class LedgerUser {

    private String ledgerId;

    public LedgerUser() {

    }

    public LedgerUser(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getLedgerId() {

        return ledgerId;
    }

    public void setLedgerId(String ledgerId) {
        this.ledgerId = ledgerId;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("ledgerId", ledgerId);
        return result;
    }
}
