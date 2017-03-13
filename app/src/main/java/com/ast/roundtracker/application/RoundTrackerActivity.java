package com.ast.roundtracker.application;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ast.roundtracker.R;
import com.ast.roundtracker.model.LedgerEntry;
import com.ast.roundtracker.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class RoundTrackerActivity extends AppCompatActivity {

    private static String LEDGER = "pt-ledger-1";

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private List<LedgerEntry> ledgerEntries;
    private List<User> users;
    private LinearLayout mainLayout;
    private LinearLayout creditList;
    private LinearLayout debtList;
    private LinearLayout ledgerLayout;
    private Spinner purchaserSelector;
    private Spinner recipientSelector;
    private Button addToLedgerButton;
    private ProgressBar progressBar;
    private DatabaseReference usersTable;
    private ChildEventListener usersEventListener;
    private DatabaseReference ledgerEntriesTable;
    private ChildEventListener ledgerEntriesEventListener;

    private LinearLayout creditScoreLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_round_tracker);

        users = new ArrayList<>();
        usersTable = database.getReference("ledgers/"+LEDGER+"/users");
        ledgerEntries = new ArrayList();
        ledgerEntriesTable = database.getReference("ledgers/"+LEDGER+"/ledger");

        showRoundTracker();

    }

    @Override
    protected void onResume() {
        super.onResume();
        addDbListeners();
        if(!users.isEmpty()) {
            calculateAllBalances();
            saveBalances();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDbListeners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_round_tracker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_round_tracker) {
            setContentView(R.layout.activity_round_tracker);
            showRoundTracker();
            try {
                if (progressBar != null) {
                    ((ViewGroup) progressBar.getParent())
                            .removeView(progressBar);
                }
            } catch(Exception ex) {
            }
            return true;
        } else if (id == R.id.action_credit_score) {
            setContentView(R.layout.activity_credit_score);
            showCreditScore();
            return true;
        } else if (id == R.id.action_ledger) {
            setContentView(R.layout.activity_ledger);
            showLedger();
            return true;
        } else if (id == R.id.action_disputes) {
            setContentView(R.layout.activity_disputes);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showRoundTracker() {
        findRoundTrackerScreenElements();
        setPurchaserSelector();
        setRecipientSelector();
        addListeners();
        displayUserData();
    }

    public void showCreditScore() {
        findCreditScoreScreenElements();
        displayCreditScoreData();
    }

    public void showLedger() {
        findLedgerScreenElements();
        displayLedgerData();
    }

    public void showDisputes() {
        findDisputeScreenElements();
        displayDisputeData();
    }

    private void findRoundTrackerScreenElements() {
        creditList = (LinearLayout) findViewById(R.id.credit_list);
        debtList = (LinearLayout) findViewById(R.id.debt_list);
        addToLedgerButton = (Button) findViewById(R.id.add_to_ledger_button);
        purchaserSelector = (Spinner) findViewById(R.id.purchaser_selector);
        recipientSelector = (Spinner) findViewById(R.id.recipient_selector);
        mainLayout = (LinearLayout) findViewById(R.id.main_layout);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void findCreditScoreScreenElements() {
        creditScoreLayout = (LinearLayout) findViewById(R.id.credit_score_layout);
    }

    private void findLedgerScreenElements() {
        ledgerLayout = (LinearLayout) findViewById(R.id.ledger_layout);
    }

    private  void findDisputeScreenElements() {
        ledgerLayout = (LinearLayout) findViewById(R.id.dispute_layout);
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

                        saveBalances();

                        displayUserData();

                        ledgerEntriesTable.updateChildren(childUpdates);

                    }
                }
                return false;
            }
        });
    }

    private void saveBalances() {
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
                    .setValue(users.get(i).getTotalReceived());
        }
    }

    private void resetBalances() {
        for (int i = users.size() - 1; i >= 0; i--) {
            setUserBalance(users.get(i).getUserId(), 0, 0, 0);
        }
    }

    private void calculateAllBalances() {
        resetBalances();
        for (int i = ledgerEntries.size() - 1; i >= 0; i--) {
            if(!ledgerEntries.get(i).isDispute() && !ledgerEntries.get(i).isDelete()) {
                updateUserBalance(ledgerEntries.get(i).getPurchaserUserId(), -ledgerEntries.get(i).getVolume());
                updateUserBalance(ledgerEntries.get(i).getRecipientUserId(), ledgerEntries.get(i).getVolume());
            }
        }
    }

    private void displayUserData() {

        creditList.removeAllViews();
        debtList.removeAllViews();

        boolean balancedLedger = true;

        int creditCount = 0;
        Collections.sort(users);
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getBalance() < 0) {
                addToCreditList(users.get(i).getUserName(), users.get(i).getBalance());
                creditCount++;
                balancedLedger = false;
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
                balancedLedger = false;
            }
            if (debitCount == 3) {
                break;
            }
        }

        if(balancedLedger) {
            try {
                TextView textView = new TextView(creditList.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textView.setText("The ledger is balanced");
                textView.setTextSize(19);
                params.setMargins(50, 0, 0, 15);
                textView.setTextColor(Color.BLACK);
                creditList.addView(textView, params);

                TextView textView1 = new TextView(debtList.getContext());
                LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                textView1.setText("The ledger is balanced");
                textView1.setTextSize(19);
                params1.setMargins(50, 0, 0, 15);
                textView1.setTextColor(Color.BLACK);
                debtList.addView(textView1, params1);
            } catch (Exception ex) {

            }
        }
    }

    private void displayCreditScoreData() {
        Collections.sort(users, User.userCreditScoreComparator);
        int position = 1;
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getTotalPurchased() > 0 || users.get(i).getTotalReceived() > 0) {
                addToCreditScoreLayout(position, users.get(i).getUserName(), users.get(i).getTotalPurchased(),
                        User.getCreditScore(users.get(i).getTotalPurchased(), users.get(i).getTotalReceived()));
                position++;
            }
        }
    }

    private void displayLedgerData() {
        Collections.sort(ledgerEntries);
        for (int i = 0; i < ledgerEntries.size(); i++) {
            if(!ledgerEntries.get(i).isDispute() && !ledgerEntries.get(i).isDelete()) {
                String dateTime = getDateTime(ledgerEntries.get(i).getTimestamp());
                String purchaserUserName = getUserName(ledgerEntries.get(i).getPurchaserUserId());
                String receiverUserName = getUserName(ledgerEntries.get(i).getRecipientUserId());
                addToLedgerLayout(purchaserUserName, receiverUserName,
                        ledgerEntries.get(i).getVolume(), dateTime);
            }
        }
    }

    private void displayDisputeData() {

    }

    private void setPurchaserSelector() {
        ArrayAdapter<User> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getSpinnerArray(true));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        purchaserSelector.setAdapter(adapter);
        purchaserSelector.setSelection(0);
    }

    private void setRecipientSelector() {
        ArrayAdapter<User> adapter
                = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, getSpinnerArray(false));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recipientSelector.setAdapter(adapter);
        recipientSelector.setSelection(0);
    }

    private List<User> getSpinnerArray(boolean reverseSort) {
        List<User> spinnerArray = new ArrayList<>();
        Collections.sort(users);
        if(!reverseSort) {
            for (int i = 0; i < users.size(); i++) {
                spinnerArray.add(users.get(i));
            }
        } else {
            for (int i = users.size()-1; i >= 0; i--) {
                spinnerArray.add(users.get(i));
            }
        }
        return spinnerArray;
    }

    // Needy
    private void addToCreditList(String user, int balance) {
        TextView textView = new TextView(creditList.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(user + " is owed " + (balance * -1));
        textView.setTextSize(19);
        params.setMargins(50, 0, 0, 15);
        int colourInt = Color.parseColor("#00A329");
        textView.setTextColor(colourInt);
        creditList.addView(textView, params);
    }

    // Greedy
    private void addToDebtList(String user, int balance) {
        TextView textView = new TextView(debtList.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView.setText(user + " owes " + balance);
        textView.setTextSize(19);
        params.setMargins(50, 0, 0, 15);
        int colourInt = Color.parseColor("#CC3E16");
        textView.setTextColor(colourInt);
        debtList.addView(textView, params);
    }

    private void addToCreditScoreLayout(int position, String userName, int totalPurchased, long score) {

        TextView textView0 = new TextView(creditScoreLayout.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView0.setText(position + ".  " + userName);
        textView0.setTextSize(20);
        params.setMargins(50, 8, 0, 8);
        textView0.setTextColor(Color.BLACK);
        creditScoreLayout.addView(textView0, params);

        TextView textView1 = new TextView(creditScoreLayout.getContext());
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.LEFT;
        textView1.setText("Total purchased:  " + totalPurchased);
        textView1.setTextSize(18);
        params1.setMargins(214, 8, 30, 8);
        textView1.setTextColor(Color.GRAY);
        creditScoreLayout.addView(textView1, params1);

        TextView textView2 = new TextView(creditScoreLayout.getContext());
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.LEFT;
        textView2.setText("Credit score:  " + score);
        textView2.setTextSize(18);
        params2.setMargins(214, 8, 30, 24); //280
        textView2.setTextColor(Color.GRAY);
        creditScoreLayout.addView(textView2, params2);
    }

    private void addToLedgerLayout(String purchaserUserName, String receiverUserName,
                                   int volume, String timestamp) {

        TextView textView0 = new TextView(ledgerLayout.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textView0.setText(purchaserUserName + " -> " +receiverUserName);
        textView0.setTextSize(18);
        params.setMargins(50, 8, 0, 8);
        textView0.setTextColor(Color.BLACK);
        ledgerLayout.addView(textView0, params);

        LinearLayout linearLayout = new LinearLayout(ledgerLayout.getContext());

        Button disputeButton = new Button(ledgerLayout.getContext());
        disputeButton.setText("DISPUTE");
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params1.gravity = Gravity.LEFT;
        TextView textView1 = new TextView(ledgerLayout.getContext());
        textView1.setText(timestamp);
        textView1.setTextSize(18);
        params1.setMargins(25, 8, 30, 24);
        textView1.setTextColor(Color.GRAY);
        linearLayout.addView(disputeButton, params1);
        linearLayout.addView(textView1, params1);

        ledgerLayout.addView(linearLayout, params1);
    }

    private void updateUserBalance(String userId, int balanceAdjust) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUserId().equals(userId)) {
                users.get(i).setBalance(users.get(i).getBalance() + balanceAdjust);
                if(balanceAdjust > 0) {
                    users.get(i).setTotalReceived(users.get(i).getTotalReceived() + 1);
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
                users.get(i).setTotalReceived(totalReceived);
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
                try {
                    displayUserData();
                    showCreditScore();
                    showLedger();
                    showDisputes();
                } catch (Exception ex) {
                }
            }
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                LedgerEntry ledgerEntryFromDb = dataSnapshot.getValue(LedgerEntry.class);
                for (int i = 0; i < ledgerEntries.size(); i++) {
                    if((ledgerEntries.get(i).getPurchaserUserId()
                            + ledgerEntries.get(i).getTimestamp()).equals(
                            ledgerEntryFromDb.getPurchaserUserId()
                                    + ledgerEntryFromDb.getTimestamp())) {
                        ledgerEntries.get(i).setDispute(ledgerEntryFromDb.isDispute());
                        ledgerEntries.get(i).setDeleteCount(ledgerEntryFromDb.getDeleteCount());
                        ledgerEntries.get(i).setKeepCount(ledgerEntryFromDb.getKeepCount());
                        ledgerEntries.get(i).setDelete(ledgerEntryFromDb.isDelete());
                        break;
                    }
                }
                try {
                    displayUserData();
                    showCreditScore();
                    showLedger();
                    showDisputes();
                } catch (Exception ex) {
                }
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

    private String getUserName(String userId) {
        for (int i = 0; i < users.size(); i++) {
            if(users.get(i).getUserId().equals(userId)) {
                return users.get(i).getUserName();
            }
        } return "";
    }

    private String getDateTime(Long timeInMilliseconds) {
        DateFormat dateTimeFormat = new SimpleDateFormat("dd MMM yy\nHH:mm");
        dateTimeFormat.setTimeZone(TimeZone.getDefault());
        String dateFormatted = dateTimeFormat.format(new Date(timeInMilliseconds));
        return dateFormatted;
    }

}
