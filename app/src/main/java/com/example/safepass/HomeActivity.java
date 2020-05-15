package com.example.safepass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class HomeActivity extends AppCompatActivity {
    Button btnLogout;
    Button btnAddAccount, btnViewAccount;
    FirebaseAuth mFirebaseAuth;
    FirebaseFirestore db;
    DocumentReference mDocRef;
    DocumentSnapshot mDocSnap;
    FirebaseUser currentUser;
    private AccountAdapter.OnItemClickListener listener;
    String currentUserId;
    ArrayList<Account> accountArrayList;
    private CollectionReference notebookRef;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        accountArrayList = new ArrayList<>();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        mDocRef = FirebaseFirestore.getInstance().document("users/" + currentUserId);
        btnLogout = findViewById(R.id.logout);
        btnAddAccount = findViewById(R.id.button2);

        onStart();
        notebookRef = db.collection("users");
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = null;
                mFirebaseAuth.signOut();
                Intent intToMain = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intToMain);
            }
        });
        btnAddAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addAcc = new Intent(HomeActivity.this, AccountCreation.class);
                startActivity(addAcc);
            }
        });



        mDocRef.addSnapshotListener( this, new EventListener<DocumentSnapshot>() {


            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Toast.makeText(HomeActivity.this, "Error while loading!", Toast.LENGTH_SHORT).show();
                    Log.d("HomeActivity", e.toString());
                    return;
                }

                if (documentSnapshot.exists()) {
                    Map<String, Object> accountList = documentSnapshot.getData();
                    Log.d("AccountList", accountList.toString());
                    mDocSnap= documentSnapshot;


                    String output = "";
                    //accountArrayList.clear();
                    for (Object account : accountList.values()) {
                        HashMap<String, String> hm = ((HashMap<String, String>) account);
                        Account account1 = new Account(hm.get("accountName"), hm.get("username"), hm.get("password"));
                        if (!accountArrayList.equals(account1))
                            accountArrayList.add(account1);
                    }


                    if (!accountArrayList.isEmpty()) {
                        Collections.sort(accountArrayList);


                    }

                }
                buildRecyclerView();

            }
        });


    }

    public void loadNotes() {
        notebookRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                String data = "";
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Account account = documentSnapshot.toObject(Account.class);
                    String accountName = account.getAccountName();
                    String username = account.getUsername();
                    String password = account.getPassword();
                    data += "AccountName: " + accountName + "\nUsername: " + username + "\n\nPassword: " + password + "\n\n\n";
                }

            }
        });
    }


    public void buildRecyclerView() {
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new AccountAdapter(accountArrayList, HomeActivity.this);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

    }

}




    /*
    @Override
    protected void onStart() {
        super.onStart();
        notebookRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    return;
                }

                String data = "";

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                   Account account = documentSnapshot.toObject(Account.class);
                    account.setDocumentId(documentSnapshot.getId());

                    String documentId = account.getDocumentId();
                    String accountName = account.getAccountName();
                    String username = account.getUsername();
                    String password= account.getPassword();

                    data += "ID: " + documentId
                            + "\nAccountName: " + accountName + "\nUsername: " + username + "Password: " + password+ "\n\n";
                }

                emptyVault.setText(data);
            }
        });
    }

     */


