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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class AccountAccess extends AppCompatActivity {
    EditText accountName, username, password;
    Button copyPaste, save, btnGeneratePassword, delete;
    String[] alphaNumeric;
    String[] symbols;
    Account temp;
    String accountNameS, usernameS, passwordS;
    DocumentReference mDocRef;
    ArrayList<Account> accountArrayList;
    FirebaseUser currentUser;
    String currentUserId;
    Map<String, Account> dataToSave;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_access);
        password = findViewById(R.id.editText10);
        username = findViewById(R.id.editText11);
        save = findViewById(R.id.button3);
        btnGeneratePassword = findViewById(R.id.button4);
        accountName = findViewById(R.id.editText12);
        accountName.setEnabled(false);
        accountArrayList = new ArrayList<>();
        copyPaste = findViewById(R.id.button5);
        delete = findViewById(R.id.button7);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();
        mDocRef = FirebaseFirestore.getInstance().document("users/" + currentUserId);
        dataToSave = new HashMap<>();
        String alphas = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        alphaNumeric = alphas.split("");
        String symbol = "`!@#$%^&*()-_=+[];:',<.>/?" + "\"";
        symbols = symbol.split("");
        accountNameS = Objects.requireNonNull(getIntent().getExtras().getString("AccountName"));
        usernameS = Objects.requireNonNull(getIntent().getExtras().getString("Username"));
        passwordS = Objects.requireNonNull(getIntent().getExtras().getString("Password"));
        passwordS= AESDecryption(passwordS);
        password.setText(passwordS);
        username.setText(usernameS);
        accountName.setText(accountNameS);

        copyPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Password", password.getText());
                clipboard.setPrimaryClip(clip);
            }


        });
        delete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                delete();
            }
        });
        btnGeneratePassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                password.setText(passwordGeneration("12", 10, 20, alphaNumeric, symbols));
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

                if (!passwordString.isEmpty() && !usernameString.isEmpty() && !accountNameString.isEmpty()) {
                    temp = new Account(accountNameString, usernameString, AESEncryption(passwordString));
                    dataToSave = new HashMap<>();
                    dataToSave.put(accountNameString, temp);
                    mDocRef.set(dataToSave, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("AccountCreation", "Document has been saved");
                            // notebookRef.add(temp);
                            Toast.makeText(AccountAccess.this, "Save successful", Toast.LENGTH_SHORT).show();

                            Intent toHome = new Intent(AccountAccess.this, HomeActivity.class);
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

    public void delete() {
        mDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                Map<String, Object> map = task.getResult().getData();
                map.remove(accountNameS);

                mDocRef.set(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("Delete Method", "Successfully Deleted");
                                Toast.makeText(AccountAccess.this, "Successfully deleted", Toast.LENGTH_LONG);
                                Intent intent = new Intent(AccountAccess.this, HomeActivity.class);
                                startActivity(intent);
                            }
                        });
            }
        });


    }
    public String AESEncryption(String password) {
        String secretKey = "fjerugheru" +accountNameS + "hguewihguiweh";
        String encryptedString = encrypt(password, secretKey);
        return encryptedString;
    }

    public String AESDecryption(String password) {
        String secretKey = "fjerugheru" +accountNameS + "hguewihguiweh";;
        String decryptedString = decrypt(password, secretKey);
        return decryptedString;
    }

    public String encrypt(String strToEncrypt, String secret) {
        String salt = "58gevwno" + accountNameS + "29824envjv";
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

    public String decrypt(String strToDecrypt, String secret) {
        String salt = "58gevwno" + accountNameS + "29824envjv";
        try {
            byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            IvParameterSpec ivspec = new IvParameterSpec(iv);

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(secret.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivspec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
