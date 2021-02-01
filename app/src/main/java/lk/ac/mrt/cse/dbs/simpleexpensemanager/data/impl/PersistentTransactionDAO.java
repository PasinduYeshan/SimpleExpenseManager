package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.ContentValues;
import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.control.PersistentExpenseManager;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.TransactionDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.db.SQLiteDB;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

public class PersistentTransactionDAO implements TransactionDAO {
    private SQLiteDB myDB;

    private static final String TABLE_TRANSACTION = "transactions";
    private static final String TRANSACTION_DATE = "date";
    private static final String TRANSACTION_ACCOUNTNO = "accountno";
    private static final String TRANSACTION_EXPENSETYPE = "expenseType";
    private static final String TRANSACTION_AMOUNT = "amount";

    public PersistentTransactionDAO(SQLiteDB db){
        this.myDB = db;
    }

    @Override
    public void logTransaction(Date date, String accountNo, ExpenseType expenseType, double amount) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String sDate = dateFormat.format(date);
        ContentValues transContent = new ContentValues();
        transContent.put(TRANSACTION_ACCOUNTNO, accountNo);
        transContent.put(TRANSACTION_DATE, sDate);
        transContent.put(TRANSACTION_EXPENSETYPE, getStringExpense(expenseType));
        transContent.put(TRANSACTION_AMOUNT, amount);
        this.myDB.insertData(TABLE_TRANSACTION, transContent);
    }

    @Override
    public List<Transaction> getAllTransactionLogs() {
        Cursor result = this.myDB.getData(TABLE_TRANSACTION,new String[] {"*"},new String[][] {});
        List<Transaction> transactions = new ArrayList<Transaction>();
        if(result.getCount() != 0) {

            while (result.moveToNext()) {
                String dateS = result.getString(2);
                String accountNo = result.getString(1);
                String expenseType = result.getString(3);
                double amount = result.getDouble(4);
                Date date = stringToDate(dateS);

                Transaction transaction = new Transaction(date,accountNo,getExpense(expenseType),amount );
                transactions.add(transaction);
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> getPaginatedTransactionLogs(int limit) {
        Cursor result = this.myDB.getDataWithLimit(TABLE_TRANSACTION,new String[] {"*"},new String[][] {},limit);
        List<Transaction> transactions = new ArrayList<Transaction>();
        if(result.getCount() != 0) {

            while (result.moveToNext()) {
                String dateS = result.getString(2);
                String accountNo = result.getString(1);
                String expenseType = result.getString(3);
                double amount = result.getDouble(4);
                Date date = stringToDate(dateS);
                Transaction transaction = new Transaction(date,accountNo,getExpense(expenseType),amount );
                transactions.add(transaction);
            }
        }

        return transactions;
    }

    public ExpenseType getExpense(String expense){
        if(expense == "Expense"){
            return ExpenseType.EXPENSE;
        }else{
            return ExpenseType.INCOME;
        }
    }

    public String getStringExpense(ExpenseType expense){
        if(expense == ExpenseType.EXPENSE){
            return "Expense";
        }else{
            return "Income";
        }
    }

    public Date stringToDate(String d){
        String strDate = new StringBuilder(d).reverse().toString();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Date date = new Date();
        try{
            date = dateFormat.parse(strDate);
        }catch(Exception e){
            System.out.println(e);
        }
        return date;
    }

    public void removeTransaction(String transactionNo) throws InvalidAccountException {
        int result = this.myDB.deleteData("transaction","transaction_no",transactionNo);
        if(result == 0){
            throw new InvalidAccountException("Transactoin number is invalid");
        }
    }

}
