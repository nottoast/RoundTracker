package com.ast.roundtracker.application;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ast.roundtracker.R;
import com.ast.roundtracker.model.Ledger;
import com.ast.roundtracker.model.LedgerUser;
import com.ast.roundtracker.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LedgerListActivity extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private List<String> ledgerUsers;
    private DatabaseReference userLedgersTable;
    private ChildEventListener userLedgersEventListener;

    private List<Ledger> ledgers;
    private DatabaseReference ledgersTable;
    //private ChildEventListener ledgersEventListener;

    private LinearLayout ledgersLayout;
    private Button joinLedger;
    private Button createLedger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences preferences = getSharedPreferences("round_tracker_prefs", 0);
        String currentLedgerStatus = preferences.getString("current_ledger_status", "closed");
        if(currentLedgerStatus.equals("open")) {
            Intent intent = new Intent(getApplicationContext(), LedgerActivity.class);
            startActivity(intent);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledgers);

        //TODO: Sort out userId with authentication
        ledgerUsers = new ArrayList<>();
        userLedgersTable = database.getReference("users"+"/u01");

        ledgers = new ArrayList<>();
        ledgersTable = database.getReference("ledgers");

        addDbListeners();
        showLedgers();

    }

    @Override
    protected void onResume() {

        SharedPreferences preferences = getSharedPreferences("round_tracker_prefs", 0);
        String currentLedgerStatus = preferences.getString("current_ledger_status", "closed");
        if(currentLedgerStatus.equals("open")) {
            Intent intent = new Intent(getApplicationContext(), LedgerActivity.class);
            startActivity(intent);
        }

        super.onResume();

        addDbListeners();
        setContentView(R.layout.activity_ledgers);
        showLedgers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDbListeners();
    }

    public void addListeners() {

        joinLedger.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                }
                return false;
            }
        });

        createLedger.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    String ledgerId = UUID.randomUUID().toString();
                    String inviteCode = LedgerUtils.getInviteCode();
                    Object timestamp = ServerValue.TIMESTAMP;

                    //TODO: Fix the userid with authentication
                    // Create LedgerUser
                    Map<String, Object> childUpdates0 = new HashMap<>();
                    childUpdates0.put(ledgerId, ledgerId);
                    userLedgersTable.updateChildren(childUpdates0);

                    //TODO: Fix the userid with authentication
                    // Create Ledger
                    Ledger ledger = new Ledger();
                    ledger.setLedgerId(ledgerId);
                    ledger.setLedgerName("myLedger");
                    ledger.setActive(true);
                    ledger.setOriginalTimestamp(timestamp);
                    ledger.setInviteCode(inviteCode);
                    ledger.setAdminId("u01");
                    Map<String, Object> ledgerValues = ledger.toMap();
                    Map<String, Object> childUpdates1 = new HashMap<>();
                    childUpdates1.put(ledgerId, ledgerValues);
                    ledgersTable.updateChildren(childUpdates1);

                    //TODO: Fix the userid with authentication
                    // Create user within Ledger
                    DatabaseReference usersTable = database.getReference("ledgers/" + ledgerId + "/users");
                    User user = new User();
                    user.setUserId("u01");
                    user.setUserName("unknown");
                    user.setTotalPurchased(0);
                    user.setTotalReceived(0);
                    Map<String, Object> userValues = user.toMap();
                    Map<String, Object> childUpdates2 = new HashMap<>();
                    childUpdates2.put("u01", userValues);
                    usersTable.updateChildren(childUpdates2);

                }
                return false;
            }
        });
    }

    private void showLedgers() {
        findLedgersScreenElements();
        addListeners();
        displayLedgersData();
    }

    private void findLedgersScreenElements() {
        ledgersLayout = (LinearLayout) findViewById(R.id.ledgers_layout);
        createLedger = (Button) findViewById(R.id.create_ledger_button);
        joinLedger = (Button) findViewById(R.id.join_ledger_button);
    }

    private void addDbListeners() {

        ledgerUsers = new ArrayList<>();

        userLedgersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                String ledgerUserFromDb = dataSnapshot.getValue(String.class);

                /*
                try {
                    if (progressBar != null) {
                        ((ViewGroup) progressBar.getParent())
                                .removeView(progressBar);
                    }
                } catch (Exception ex) {
                }
                */

                boolean addUserLedger = true;
                for (int i = 0; i < ledgerUsers.size(); i++) {
                    if (ledgerUsers.get(i).equals(ledgerUserFromDb)) {
                        addUserLedger = false;
                        break;
                    }
                }
                if (addUserLedger) {
                    ledgerUsers.add(ledgerUserFromDb);
                }

                populateLedgersList(ledgerUserFromDb);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                LedgerUser ledgerUserFromDb = dataSnapshot.getValue(LedgerUser.class);

                for (int i = 0; i < ledgerUsers.size(); i++) {
                    if (ledgerUsers.get(i).equals(ledgerUserFromDb)) {
                        //ledgers.get(i).setTotalPurchased(userFromDb.getTotalPurchased());

                        break;
                    }
                }

                populateLedgersList(ledgerUserFromDb.getLedgerId());
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

        userLedgersTable.addChildEventListener(userLedgersEventListener);
    }

    private void populateLedgersList(final String ledgerId) {
        Query query = database.getReference("ledgers").child(ledgerId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Ledger ledgerFromDb = dataSnapshot.getValue(Ledger.class);

                    Boolean addLedger = true;
                    for (int i = 0; i < ledgers.size(); i++) {
                        if (ledgers.get(i).getLedgerId().equals(ledgerId)) {
                            ledgers.get(i).setActive(ledgerFromDb.isActive());
                            ledgers.get(i).setLedgerName(ledgerFromDb.getLedgerName());
                            addLedger = false;
                            break;
                        }
                    }
                    if (addLedger) {
                        ledgers.add(ledgerFromDb);
                    }

                    displayLedgersData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void displayLedgersData() {

        ledgersLayout.removeAllViews();

        Collections.sort(ledgers);

        for (int i = 0; i < ledgers.size(); i++) {

            final int index = i;

            TextView textView = new TextView(ledgersLayout.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            textView.setText(ledgers.get(i).getLedgerName());
            textView.setTextSize(22);
            params.setMargins(25, 15, 0, 15);
            //int colourInt = Color.parseColor("#CC3E16");
            //textView.setTextColor(colourInt);
            ledgersLayout.addView(textView, params);


            LinearLayout linearLayout = new LinearLayout(ledgersLayout.getContext());
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params1.gravity = Gravity.LEFT;

            final Button addUsersButton = new Button(ledgersLayout.getContext());
            addUsersButton.setText("Add users");
            addUsersButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {

                    }
                    return false;
                }
            });
            linearLayout.addView(addUsersButton, params1);

            final Button shareButton = new Button(ledgersLayout.getContext());
            shareButton.setText("Invite");
            shareButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(LedgerListActivity.this);
                        builder.setTitle("Ledger invite code:  "
                                + ledgers.get(index).getInviteCode().substring(0,4)
                                + " " + ledgers.get(index).getInviteCode().substring(4,8));
                        builder.setMessage("Users wishing to join this ledger will need to enter this code");
                        builder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();

                    }
                    return false;
                }
            });
            linearLayout.addView(shareButton, params1);

            final Button useButton = new Button(ledgersLayout.getContext());
            useButton.setText("Use");
            useButton.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {

                        SharedPreferences settings = getSharedPreferences("round_tracker_prefs", 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.clear();
                        editor.putString("current_ledger", ledgers.get(index).getLedgerId());
                        editor.putString("current_ledger_name", ledgers.get(index).getLedgerName());
                        editor.putString("current_ledger_status", "open");
                        editor.commit();

                        Intent intent = new Intent(getApplicationContext(), LedgerActivity.class);
                        startActivity(intent);
                    }
                    return false;
                }
            });
            linearLayout.addView(useButton, params1);

            ledgersLayout.addView(linearLayout, params1);

        }
    }

    private void removeDbListeners() {
        userLedgersTable.removeEventListener(userLedgersEventListener);
    }

}
