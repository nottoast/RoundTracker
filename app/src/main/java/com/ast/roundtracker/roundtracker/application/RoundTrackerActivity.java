package com.ast.roundtracker.roundtracker.application;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ast.roundtracker.roundtracker.R;
import com.ast.roundtracker.roundtracker.model.LedgerEntry;
import com.ast.roundtracker.roundtracker.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoundTrackerActivity extends AppCompatActivity {

    private static String LEDGER = "pt-ledger-1";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private List<LedgerEntry> ledgerEntries;
    private List<User> users;
    private LinearLayout creditList;
    private LinearLayout debtList;
    private Spinner purchaserSelector;
    private Spinner recipientSelector;
    private Button addToLedgerButton;
    private ProgressBar progressBar;
    private DatabaseReference usersTable;
    private ChildEventListener usersEventListener;
    private DatabaseReference ledgerEntriesTable;
    private ChildEventListener ledgerEntriesEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_tracker);

        users = new ArrayList<>();
        usersTable = database.getReference("ledgers/"+LEDGER+"/users");
        ledgerEntries = new ArrayList();
        ledgerEntriesTable = database.getReference("ledgers/"+LEDGER+"/ledger");

        findScreenElements();
        setPurchaserSelector();
        setRecipientSelector();
        addListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDbListeners();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDbListeners();
    }

    public void addListeners() {

        addToLedgerButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String purchaserId = ((User) purchaserSelector.getSelectedItem()).getUserId();
                    String recipientId = ((User) recipientSelector.getSelectedItem()).getUserId();
                    if(!purchaserId.equals(recipientId)) {

                        calculateAllBalances();

                        Object timestamp = ServerValue.TIMESTAMP;
                        LedgerEntry ledgerEntry = new LedgerEntry(purchaserId, recipientId,
                                1, timestamp, false, 0, 0, users.size(), false);
                        String key = ledgerEntriesTable.push().getKey();
                        Map<String, Object> ledgerEntryValues = ledgerEntry.toMap();
                        Map<String, Object> childUpdates = new HashMap<>();
                        childUpdates.put(key, ledgerEntryValues);

                        updateUserBalance(ledgerEntry.getPurchaserUserId(), -ledgerEntry.getVolume());
                        updateUserBalance(ledgerEntry.getRecipientUserId(), ledgerEntry.getVolume());

                        for (int i = 0; i < users.size(); i++) {
                            String userId = users.get(i).getUserId().substring(1,2);
                            usersTable.child(userId)
                                    .child("balance")
                                    .setValue(users.get(i).getBalance());
                            usersTable.child(userId)
                                    .child("totalPurchased")
                                    .setValue(users.get(i).getTotalPurchased());
                            usersTable.child(userId)
                                    .child("totalReceived")
                                    .setValue(users.get(i).getTotalRecieved());
                        }

                        displayUserData();

                        ledgerEntriesTable.updateChildren(childUpdates);

                    }
                }
                return false;
            }
        });
    }

    private void resetBalances() {
        for (int i = users.size() - 1; i >= 0; i--) {
            setUserBalance(users.get(i).getUserId(), 0, 0, 0);
        }
    }

    private void calculateAllBalances() {
        resetBalances();
        for (int i = ledgerEntries.size() - 1; i >= 0; i--) {
            updateUserBalance(ledgerEntries.get(i).getPurchaserUserId(), -ledgerEntries.get(i).getVolume());
            updateUserBalance(ledgerEntries.get(i).getRecipientUserId(), ledgerEntries.get(i).getVolume());
        }
    }

    private void findScreenElements() {
        creditList = (LinearLayout) findViewById(R.id.credit_list);
        debtList = (LinearLayout) findViewById(R.id.debt_list);
        addToLedgerButton = (Button) findViewById(R.id.add_to_ledger_button);
        purchaserSelector = (Spinner) findViewById(R.id.purchaser_selector);
        recipientSelector = (Spinner) findViewById(R.id.recipient_selector);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void displayUserData() {

        creditList.removeAllViews();
        debtList.removeAllViews();

        int creditCount = 0;
        Collections.sort(users);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getBalance() < 0) {
                addToCreditList(users.get(i).getUserName(), users.get(i).getBalance());
                creditCount++;
            }
            if (creditCount == 3) {
                break;
            }
        }
        int debitCount = 0;
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i).getBalance() > 0) {
                addToDebtList(users.get(i).getUserName(), users.get(i).getBalance());
                debitCount++;
            }
            if (debitCount == 3) {
                break;
            }
        }
    }

    private void setPurchaserSelector() {
        ArrayAdapter<User> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getSpinnerArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        purchaserSelector.setAdapter(adapter);
        purchaserSelector.setSelection(0);
    }

    private void setRecipientSelector() {
        ArrayAdapter<User> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getSpinnerArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipientSelector.setAdapter(adapter);
        recipientSelector.setSelection(1);
    }

    private List<User> getSpinnerArray() {
        List<User> spinnerArray = new ArrayList<>();
        Collections.sort(users);
        for (int i = 0; i < users.size(); i++) {
            spinnerArray.add(users.get(i));
        }
        return spinnerArray;
    }

    // Needy
    private void addToCreditList(String user, int balance) {
        TextView textView = new TextView(creditList.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(user + " is owed " + (balance * -1));
        textView.setTextSize(17);
        params.setMargins(30, 0, 0, 15);
        textView.setTextColor(Color.BLACK);
        creditList.addView(textView, params);
    }

    // Greedy
    private void addToDebtList(String user, int balance) {
        TextView textView = new TextView(debtList.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(user + " owes " + balance);
        textView.setTextSize(17);
        params.setMargins(30, 0, 0, 15);
        textView.setTextColor(Color.BLACK);
        debtList.addView(textView, params);
    }

    private void updateUserBalance(String userId, int balanceAdjust) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                users.get(i).setBalance(users.get(i).getBalance() + balanceAdjust);
                if(balanceAdjust > 0) {
                    users.get(i).setTotalRecieved(users.get(i).getTotalRecieved() + 1);
                } else if(balanceAdjust < 0) {
                    users.get(i).setTotalPurchased(users.get(i).getTotalPurchased() + 1);
                }
                break;
            }
        }
    }

    private void setUserBalance(String userId, int balance, int totalPurchased, int totalReceived) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                users.get(i).setBalance(balance);
                users.get(i).setTotalPurchased(totalPurchased);
                users.get(i).setTotalRecieved(totalReceived);
                break;
            }
        }
    }

    private void addDbListeners() {

        usersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                User userFromDb = dataSnapshot.getValue(User.class);
                try {
                    if (progressBar != null) {
                        ((ViewGroup) progressBar.getParent())
                                .removeView(progressBar);
                    }
                } catch(Exception ex) {
                }
                users.add(userFromDb);
                setPurchaserSelector();
                setRecipientSelector();
                displayUserData();
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                User userFromDb = dataSnapshot.getValue(User.class);
                for (int i = 0; i < users.size(); i++) {
                    if(users.get(i).getUserId().equals(userFromDb.getUserId())) {
                        users.get(i).setBalance(userFromDb.getBalance());
                        break;
                    }
                }
                displayUserData();
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        usersTable.addChildEventListener(usersEventListener);

        ledgerEntriesEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                LedgerEntry ledgerEntryFromDb = dataSnapshot.getValue(LedgerEntry.class);
                try {
                    if (progressBar != null) {
                        ((ViewGroup) progressBar.getParent())
                                .removeView(progressBar);
                    }
                } catch(Exception ex) {
                }
                ledgerEntries.add(ledgerEntryFromDb);
                // refresh hall of fame if needed
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                LedgerEntry ledgerEntryFromDb = dataSnapshot.getValue(LedgerEntry.class);
                for (int i = 0; i < ledgerEntries.size(); i++) {
                    if((ledgerEntries.get(i).getPurchaserUserId()
                            + ledgerEntries.get(i).getTimestamp()).equals(
                            ledgerEntryFromDb.getPurchaserUserId()
                                    + ledgerEntryFromDb.getTimestamp())) {
                        ledgerEntries.get(i).setDiscard(ledgerEntryFromDb.isDiscard());
                        break;
                    }
                }
                // refresh hall of fame if needed
            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ledgerEntriesTable.addChildEventListener(ledgerEntriesEventListener);
    }

    private void removeDbListeners() {
        usersTable.removeEventListener(usersEventListener);
        ledgerEntriesTable.removeEventListener(ledgerEntriesEventListener);
    }

}
