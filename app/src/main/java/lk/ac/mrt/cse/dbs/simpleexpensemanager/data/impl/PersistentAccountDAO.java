package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;

import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.*;
import android.database.Cursor;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.SQLiteDB;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentAccountDAO implements AccountDAO {
    private final SQLiteDB myDB;
    public String myname = "PersistentAccount";

    private static final String TABLE_ACCOUNT = "account";

    private static final String ACCOUNT_NO = "accountno";
    private static final String ACCOUNT_BANKNAME = "bankname";
    private static final String ACCOUNT_HOLDERNAME = "accountHolderName";
    private static final String ACCOUNT_BALANCE = "balance";



    public PersistentAccountDAO(SQLiteDB db){
        this.myDB = db;
    }

    public String getMyname(){
        return this.myname;
    }

    @Override
    public List<String> getAccountNumbersList() {
        Cursor res = this.myDB.getData(TABLE_ACCOUNT,new String[] {"accountno"}, new String[][] {});
        List<String> accountNumbers = new ArrayList<String>();
        if(res.getCount() != 0) {
            while (res.moveToNext()) {
                accountNumbers.add(res.getString(0));
            }
        }
        res.close();
        return accountNumbers;
    }

    @Override
    public List<Account> getAccountsList() {
        Cursor res = this.myDB.getData(TABLE_ACCOUNT,new String[] {"*"}, new String[][] {});
        List<Account> accounts = new ArrayList<Account>();
        if(res.getCount() != 0) {
            while (res.moveToNext()) {
                String accountNo = res.getString(0);
                String bankName = res.getString(1);
                String accountHolderName = res.getString(2);
                double balance = res.getDouble(3);
                Account account = new Account(accountNo, bankName, accountHolderName, balance);
                accounts.add(account);
            }
        }
        res.close();
        return accounts;
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        String[] condition = {"accountNo", "=",accountNo};
        Cursor res = this.myDB.getData(TABLE_ACCOUNT,new String[] {"*"}, new String[][] {condition});
        if(res.getCount() == 0){
            throw new InvalidAccountException("Invalid Account Number");
        }
        String acNO = "";
        String bankName = "";
        String accountHolderName = "";
        double balance = 0;
        while(res.moveToNext()){
            acNO = res.getString(res.getColumnIndex(ACCOUNT_NO));
            bankName = res.getString(res.getColumnIndex(ACCOUNT_BANKNAME));
            accountHolderName = res.getString(res.getColumnIndex(ACCOUNT_HOLDERNAME));
            balance = res.getDouble(res.getColumnIndex(ACCOUNT_BALANCE));
        }

        res.close();
        Account account = new Account(acNO,bankName,accountHolderName,balance);
        return account;
    }

    @Override
    public void addAccount(Account account) {
        ContentValues accContent = new ContentValues();
        accContent.put(ACCOUNT_NO, account.getAccountNo());
        accContent.put(ACCOUNT_BANKNAME, account.getBankName());
        accContent.put(ACCOUNT_HOLDERNAME, account.getAccountHolderName());
        accContent.put(ACCOUNT_BALANCE, account.getBalance());
        this.myDB.insertData(TABLE_ACCOUNT, accContent);

    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        int result = this.myDB.deleteData("account","accountno",accountNo);
        if(result == 0){
            throw new InvalidAccountException("Account number is invalid");
        }


    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        double balance = 0;
        double total = 0;
        try{
            Account acc = getAccount(accountNo);
            balance = acc.getBalance();
            System.out.println("Current balnce "+String.valueOf(balance));
        }catch(Exception e){
            System.out.println("Invalid Account Number");
            throw new InvalidAccountException("Invalid Account Number");
        }

        if (expenseType == ExpenseType.EXPENSE){
            if(balance < amount){
                throw new InvalidAccountException("Insufficient Account Balance");
            }
            total = balance-amount;
        }else{
            total = amount +balance;
        }
        System.out.println("New balnce "+String.valueOf(total));
        String[] condition = {"accountno","=",accountNo};
        ContentValues accContent = new ContentValues();
        accContent.put(ACCOUNT_BALANCE, total);
        boolean result = this.myDB.updateData("account",accContent,condition);
        if(!result){
            throw new InvalidAccountException("Account number is invalid");
        }
    }


}
