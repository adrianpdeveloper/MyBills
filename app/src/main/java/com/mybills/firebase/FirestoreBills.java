package com.mybills.firebase;

import static com.mybills.utils.DateFormater.*;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mybills.model.Bill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;


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

    //Get Gastos semana actual
    public void getWeekBills(String userId, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereGreaterThan("date", getPreviousSundayTimestamp())
                .whereLessThan("date",getTodayTimestamp())
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    //Get Ultimos 5 registros añadidos
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
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    //Get Gastos con fecha superior a la actual
    public void getFutureBills(String userId, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereGreaterThan("date", getTodayTimestamp())
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void getDayBills(String userId, Timestamp dateSelected, final OnBillsLoadedListener listener){
        db.collection("bills")
                .whereGreaterThan("date", getDayBefore(dateSelected))
                .whereLessThan("date", getDayAfter(dateSelected))
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILLS TO STRING", bill.toString());
                            Log.e("Antes", getDayBefore(dateSelected).toString());
                            Log.e("Despues ", getDayAfter(dateSelected).toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    //Get Gastos de el mes hasta la semana actual
    public void  getMonthExceptTodayWeek(String userId, final OnBillsLoadedListener listener) {
        db.collection("bills")
                .whereGreaterThan("date",getLastDayOfPreviousMonthTimestamp())
                .whereLessThan("date", getPreviousSundayTimestamp())
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void getXMonthTypeBills(String userId, String month, String type, final OnBillsLoadedListener listener) {
        Log.e("DESPUES", getLastDayOfPreviousXMonthTimestamp(month)+"");
        Log.e("ANTES", getLastDayOfXMonthTimestamp(month)+"");
        db.collection("bills")
                .whereGreaterThan("date",getLastDayOfPreviousXMonthTimestamp(month))
                .whereLessThan("date", getLastDayOfXMonthTimestamp(month))
                .whereEqualTo("userId", userId)
                .whereEqualTo("type", type)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                        Log.e("BILL TO STRING", billArrayList.size()+"");
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    public void getXMonthBills(String userId, String month, final OnBillsLoadedListener listener) {
        db.collection("bills")
                .whereGreaterThan("date",getLastDayOfPreviousXMonthTimestamp(month))
                .whereLessThan("date", getLastDayOfXMonthTimestamp(month))
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                        Log.e("ARRAY LIST TO STRING", billArrayList.toString());
                        Log.e("BILL SIZE", billArrayList.size()+"");
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    //Get Gastos hasta el mes actual
    public void  getBeforeMonthBills(String userId, final OnBillsLoadedListener listener) {
        db.collection("bills")
                .whereLessThan("date", getFirstDayOfMonthTimestamp())
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Bill> billArrayList = new ArrayList<Bill>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Bill bill = document.toObject(Bill.class);
                            bill.setBillId(document.getId());
                            Log.e("BILL TO STRING", bill.toString());
                            billArrayList.add(bill);
                        }
                        listener.onBillsLoaded(billArrayList);
                    } else {
                        Log.w("Error", "Error getting documents.", task.getException());
                    }
                });
    }

    //Put Gasto
    public Task<DocumentReference> putBill( Map<String, Object> bill ){
        return db.collection("bills")
                .add(bill);
    }

    //Update Gasto
    public Task<Void> updateBill(Map<String, Object> bill, String billId ){
        return db.collection("bills")
                .document(billId)
                .update(bill);
    }

    //Delete Gasto
    public Task<Void> deleteBill(String billId ){
        return db.collection("bills")
                .document(billId)
                .delete();
    }

    //Get Importes por tipo e Importe total
    public void getMonthAmount(String userId, final onBillsAmountLoaded onBillsAmountLoaded) {
        db.collection("bills")
                .whereGreaterThan("date",getLastDayOfPreviousMonthTimestamp())
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

    //Get Importes por tipo e Importe total
    public void getXMonthAmount(String userId,String month, final onBillsAmountLoaded onBillsAmountLoaded) {
        db.collection("bills")
                .whereGreaterThan("date",getLastDayOfPreviousXMonthTimestamp(month))
                .whereLessThan("date",getLastDayOfXMonthTimestamp(month))
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
                        Log.e("BILLS TOTALES", totalAmount.toString());
                        onBillsAmountLoaded.onBillsAmountLoaded(totalAmount,total);
                    } else {
                        Log.w("Error", "Error al obtener documentos.", task.getException());
                    }
                });
    }


}
