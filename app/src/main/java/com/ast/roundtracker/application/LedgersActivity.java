package com.ast.roundtracker.application;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.ast.roundtracker.R;
import com.ast.roundtracker.model.Ledger;
import com.ast.roundtracker.model.User;
import com.ast.roundtracker.model.UserLedger;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LedgersActivity extends AppCompatActivity {

    private final FirebaseDatabase database = FirebaseDatabase.getInstance();

    private List<UserLedger> userLedgers;
    private DatabaseReference userLedgersTable;
    private ChildEventListener userLedgersEventListener;

    private List<Ledger> ledgers;
    private DatabaseReference ledgersTable;
    private ChildEventListener ledgersEventListener;

    private LinearLayout ledgersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledgers);

        userLedgers = new ArrayList<>();
        userLedgersTable = database.getReference("users");

        ledgers = new ArrayList<>();
        ledgersTable = database.getReference("ledgers");

        /*
        Query query = database.getReference("ledgers").child("issue").orderByChild("id").equalTo(0);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        */

        addDbListeners();
        showLedgers();

    }

    @Override
    protected void onResume() {
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

    private void showLedgers() {
        findLedgersScreenElements();
        //addListeners();
        displayLedgersData();
    }

    private void findLedgersScreenElements() {
        ledgersLayout = (LinearLayout) findViewById(R.id.ledgers_layout);
    }

    private void addDbListeners() {

        userLedgers = new ArrayList<>();

        userLedgersEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                UserLedger userLedgerFromDb = dataSnapshot.getValue(UserLedger.class);

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
                for (int i = 0; i < userLedgers.size(); i++) {
                    if (userLedgers.get(i).getLedgerId().equals(userLedgerFromDb.getLedgerId())) {
                        addUserLedger = false;
                        break;
                    }
                }
                if (addUserLedger) {
                    userLedgers.add(userLedgerFromDb);
                }

                populateLedgersList(userLedgerFromDb.getLedgerId());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

                UserLedger userLedgerFromDb = dataSnapshot.getValue(UserLedger.class);

                for (int i = 0; i < userLedgers.size(); i++) {
                    if (userLedgers.get(i).getLedgerId().equals(userLedgerFromDb.getLedgerId())) {
                        //ledgers.get(i).setTotalPurchased(userFromDb.getTotalPurchased());

                        break;
                    }
                }

                populateLedgersList(userLedgerFromDb.getLedgerId());
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
                    for (DataSnapshot ledger : dataSnapshot.getChildren()) {

                        Ledger ledgerFromDb = ledger.getValue(Ledger.class);

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
            TextView textView = new TextView(ledgersLayout.getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            textView.setText(ledgers.get(i).getLedgerName());
            textView.setTextSize(22);
            params.setMargins(25, 15, 0, 15);
            //int colourInt = Color.parseColor("#CC3E16");
            //textView.setTextColor(colourInt);
            ledgersLayout.addView(textView, params);
        }
    }

    private void removeDbListeners() {
        ledgersTable.removeEventListener(ledgersEventListener);
    }

}
