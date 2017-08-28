package com.example.marwa.patient.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.marwa.patient.R;
import com.example.marwa.patient.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    TextView userID;
    TextView userEmail;
    TextView userPass;
    EditText userName;
    EditText confirmation;
    EditText type;
    Button save;
    Button cancel;
    Button removeAfterTime;
    Button logout;
    static boolean calledAlready = false;
    User user;
    private FirebaseAuth auth;
    private DatabaseReference myDatabaseReference;
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userID = (TextView) findViewById(R.id.userid);
        userEmail = (TextView) findViewById(R.id.museremail);
        userPass = (TextView) findViewById(R.id.muserpass);
        userName = (EditText) findViewById(R.id.musername);
        confirmation = (EditText) findViewById(R.id.confirmation);
        type = (EditText) findViewById(R.id.type);

        save = (Button) findViewById(R.id.save);
        cancel = (Button) findViewById(R.id.cancel);
        removeAfterTime = (Button) findViewById(R.id.removetime);
        logout = (Button) findViewById(R.id.mlogout);

        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        userID.setText(pref.getString("userID", ""));
        userEmail.setText(pref.getString("userEmail", ""));
        userPass.setText(pref.getString("userPass", ""));

        if (!calledAlready) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            calledAlready = true;
        }

        myDatabaseReference = FirebaseDatabase.getInstance().getReference("Users");
        user = new User();


//----------------------------------- save -------------------------------------------------//
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user.setUserId(userID.getText().toString());
                user.setUserEmail(userEmail.getText().toString());
                user.setUserPass(userPass.getText().toString());
                user.setType(type.getText().toString());
                user.setConfirmationStatus(confirmation.getText().toString());
                user.setStartTime(String.valueOf(System.currentTimeMillis()));
                //add six hours
                user.setEndTime(String.valueOf(System.currentTimeMillis() + (6 * 60 * 60 * 1000)));
                myDatabaseReference.child(user.getUserId()).setValue(user);
            }
        });

//----------------------------------- cancel -------------------------------------------------//
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                user.setUserId(pref.getString("userID",""));
                Query applesQuery = myDatabaseReference.child(user.getUserId());
                applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                            appleSnapshot.getRef().removeValue();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("exception", "onCancelled", databaseError.toException());
                    }
                });
            }
        });
//----------------------------------- remove after time will be in service -------------------------------------------------//
        removeAfterTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDatabaseReference.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                        long time = System.currentTimeMillis();  //get time in millis
                        User u=dataSnapshot.getValue(User.class);
                        long end = Long.parseLong( u.getEndTime()); //get the end time from firebase database

                        //convert to int
                        int timenow = (int) time;
                        int endtime = (int) end;

                        //check if the endtime has been reached
                        if (endtime < timenow){
                            myDatabaseReference.removeValue();  //remove the entry
                        }
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {}

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            }
        });



//----------------------------------- logout -------------------------------------------------//

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth = FirebaseAuth.getInstance();
                auth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}
