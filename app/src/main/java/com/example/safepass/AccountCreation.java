package com.example.safepass;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AccountCreation extends AppCompatActivity {
    Button btnGeneratePassword, save, copyPass;
    EditText accountName, username, password;
    String[] alphaNumeric;
    String[] symbols;
    AccountAdapter accountAdapter;
    ArrayList<Account> mAccountList;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference myRef;
    FirebaseUser currentUser;
    DocumentReference mDocRef;
    FirebaseFirestore db;
    CollectionReference notebookRef;
    Map<String, Account> dataToSave;
    Account temp;

    public static String passwordGeneration(String passType, int min, int max, String[] alphaNumerics, String[] symbols) {
        boolean alphaNum = false;
        boolean special = false;
        String password = "";
        int randomLength = 0;
        int potStop = 0;
        if (passType.contains("1")) {
            alphaNum = true;
        }
        if (passType.contains("2")) {
            special = true;
        }
        if (alphaNum && special) {
            randomLength = alphaNumerics.length + symbols.length;
        } else if (alphaNum) {
            randomLength = alphaNumerics.length;
        } else {
            randomLength = symbols.length;
        }
        int test = (max - min) / ((max + min) / 4);
        for (int i = 0; i < max; i++) {
            if (i > min) {
                potStop = (int) (Math.random() * (max - min));
                if (potStop < test) {
                    break;
                }
            }
            int index = (int) (Math.random() * randomLength);
            if (index < alphaNumerics.length) {
                password += alphaNumerics[index];
            } else {
                password += symbols[index - alphaNumerics.length];
            }

        }
        return password;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_creation);
        db = FirebaseFirestore.getInstance();
        btnGeneratePassword = findViewById(R.id.button4);
        password = findViewById(R.id.editText4);
        copyPass= findViewById(R.id.button5);
        save = findViewById(R.id.button3);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUserId = currentUser.getUid();
        mDocRef = FirebaseFirestore.getInstance().document("users/" + currentUserId);
        accountName = findViewById(R.id.editText6);
        username = findViewById(R.id.editText5);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        notebookRef = db.collection("users");
        String alphas = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        alphaNumeric = alphas.split("");
        String symbol = "`!@#$%^&*()-_=+[];:',<.>/?" + "\"";
        symbols = symbol.split("");
        dataToSave = new HashMap<>();
        btnGeneratePassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                password.setText(passwordGeneration("12", 10, 20, alphaNumeric, symbols));
            }
        });

        copyPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Password", password.getText());
                clipboard.setPrimaryClip(clip);
            }


        });


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                String accountNameString = accountName.getText().toString();
                if (usernameString.isEmpty()) {
                    username.setError("Please enter a username");
                    username.requestFocus();
                } else
                    username.clearFocus();
                if (passwordString.isEmpty()) {
                    password.setError("Please enter a password");
                    password.requestFocus();
                } else
                    password.clearFocus();
                if (accountNameString.isEmpty()) {
                    accountName.setError("Please enter an account name");
                    accountName.requestFocus();
                } else
                    accountName.clearFocus();

                if (!(passwordString.isEmpty() && usernameString.isEmpty() && accountNameString.isEmpty())) {
                    temp = new Account(accountNameString, usernameString, AESEncryption(passwordString));
                    dataToSave.put(accountNameString, temp);
                    mDocRef.set(dataToSave, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("AccountCreation", "Document has been saved");
                            // notebookRef.add(temp);
                            Toast.makeText(AccountCreation.this, "Save successful", Toast.LENGTH_SHORT).show();
                            Intent toHome = new Intent(AccountCreation.this, HomeActivity.class);
                            startActivity(toHome);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("AccountCreation", "Document was not saved", e);
                        }
                    });

                    //TO DO LIST: DELETE HOME PAGE TEXT ONCE VAULT HAS ITEMS
                    //DO THIS
                }

            }
        });
    }
    public String AESEncryption(String password) {
        String secretKey = "fjerugheru" + accountName.getText() + "hguewihguiweh";;
        String encryptedString = encrypt(password, secretKey);
        return encryptedString;
    }

    public String encrypt(String strToEncrypt, String secret) {
        String salt = "58gevwno" + accountName.getText() + "29824envjv";
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

}
