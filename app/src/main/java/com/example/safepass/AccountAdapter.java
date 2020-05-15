package com.example.safepass;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {
    private final ArrayList<Account> mAccountList;
    private OnItemClickListener listener;
    private DocumentSnapshot documentSnapshot;
    Activity homeActivity;
    public AccountAdapter(ArrayList<Account> accountList, Activity HomeActivity) {
        mAccountList = accountList;
        homeActivity= HomeActivity;
    }

    @Override
    public AccountViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.account_item, parent, false);
        AccountViewHolder evh = new AccountViewHolder(v);
        return evh;
    }


    @Override
    public void onBindViewHolder(AccountViewHolder holder, int position) {
        final Account currentItem = mAccountList.get(position);
        holder.accountName.setText(currentItem.getAccountName());
        holder.accountName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intNew = new Intent (homeActivity, AccountAccess.class);
                intNew.putExtra("AccountName", currentItem.getAccountName());
                intNew.putExtra("Username", currentItem.getUsername());
                intNew.putExtra("Password", currentItem.getPassword());
                homeActivity.startActivity(intNew);
            }
        });

        holder.username.setText(currentItem.getUsername());
        //holder.password.setText(currentItem.getPassword());
    }

    @Override
    public int getItemCount() {
        return mAccountList.size();
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {
        TextView accountName, username;

        public AccountViewHolder(View itemView) {
            super(itemView);
            accountName = itemView.findViewById(R.id.editText4);
            username = itemView.findViewById(R.id.editText5);
            //password = itemView.findViewById(R.id.editText6);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(documentSnapshot, position);

                    }
                }
            });
        }
    }
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}

