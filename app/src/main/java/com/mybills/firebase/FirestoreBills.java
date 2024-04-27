package com.mybills.firebase;

import static com.mybills.utils.DateFormater.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mybills.model.Bill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class FirestoreBills {
    FirebaseFirestore db;

    public interface OnBillsLoadedListener {
        void onBillsLoaded(ArrayList<Bill> billArrayList);
    }

    public interface onBillsAmountLoaded {
        void onBillsAmountLoaded(Map<String, Double> totalMap, Double total);

    }



    public FirestoreBills() {
        this.db = FirebaseFirestore.getInstance();
    }

    public void getWeekBills(String userId, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereGreaterThan("date", getMondayTimestamp())
                .whereLessThan("date",getTodayTimestamp())
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void getBillsLast5(String userId, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereEqualTo("userId", userId)
                .limit(5) // Limita a los primeros 5 documentos
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void getFutureBills(String userId, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereGreaterThan("date", getTodayTimestamp())
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void  getMonthExceptTodayWeek(String userId, final OnBillsLoadedListener listener) {
        db.collection("bills")
                .whereGreaterThan("date",getFirstDayOfMonthTimestamp())
                .whereLessThan("date", getMondayTimestamp())
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void  getBeforeMonthBills(String userId, final OnBillsLoadedListener listener) {
        db.collection("bills")
                .whereLessThan("date", getFirstDayOfMonthTimestamp())
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public Task<DocumentReference> putBill( Map<String, Object> bill ){
        return db.collection("bills")
                .add(bill);
    }

    public void getMonthAmount(String userId, final onBillsAmountLoaded onBillsAmountLoaded) {
        db.collection("bills")
                .whereGreaterThan("date",getFirstDayOfMonthTimestamp())
                .whereLessThan("date",getLastDayOfMonthTimestamp())
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Map<String, Double> totalAmount = new HashMap<>();
                        Double total = 0.0;
                        totalAmount.put("Cuentas y pagos", 0.0);
                        totalAmount.put("Alimentación", 0.0);
                        totalAmount.put("Vivienda", 0.0);
                        totalAmount.put("Transporte", 0.0);
                        totalAmount.put("Ropa", 0.0);
                        totalAmount.put("Salud e higiene", 0.0);
                        totalAmount.put("Ocio", 0.0);
                        totalAmount.put("Otros", 0.0);
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            total = total + document.getDouble("amount");

                            switch (document.getString("type")) {
                                case "Cuentas y pagos":
                                    totalAmount.put("Cuentas y pagos", totalAmount.get("Cuentas y pagos") + document.getDouble("amount"));
                                    break;
                                case "Alimentación":
                                    totalAmount.put("Alimentación", totalAmount.get("Alimentación") + document.getDouble("amount"));
                                    break;
                                case "Vivienda":
                                    totalAmount.put("Vivienda", totalAmount.get("Vivienda") + document.getDouble("amount"));
                                    break;
                                case "Transporte":
                                    totalAmount.put("Transporte", totalAmount.get("Transporte") + document.getDouble("amount"));
                                    break;
                                case "Ropa":
                                    totalAmount.put("Ropa", totalAmount.get("Ropa") + document.getDouble("amount"));
                                    break;
                                case "Salud e higiene":
                                    totalAmount.put("Salud e higiene", totalAmount.get("Salud e higiene") + document.getDouble("amount"));
                                    break;
                                case "Ocio":
                                    totalAmount.put("Ocio", totalAmount.get("Ocio") + document.getDouble("amount"));
                                    break;
                                case "Otros":
                                    totalAmount.put("Otros", totalAmount.get("Otros") + document.getDouble("amount"));
                                    break;
                                default:
                                    break;
                            }

                        }
                        onBillsAmountLoaded.onBillsAmountLoaded(totalAmount,total);
                    } else {
                        Log.w("Error", "Error al obtener documentos.", task.getException());
                    }
                });
    }


}
