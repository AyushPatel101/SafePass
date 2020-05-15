package com.example.safepass;

class Account implements Comparable {
    String accountName;
    String username;
    String password;

    public Account(String accountName, String username, String password) {
        this.accountName = accountName;
        this.username = username;
        this.password = password;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String toString() {
        return "Account Name: " + accountName + "\nUsername: " + username + "\nPassword: " + password + "\n\n";
    }


    @Override
    public int compareTo(Object o) {
        Account temp = (Account) o;
        return this.getAccountName().compareTo(temp.getAccountName());
    }
}
