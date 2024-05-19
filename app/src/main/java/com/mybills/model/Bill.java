package com.mybills.model;

import com.google.firebase.Timestamp;

public class Bill {

    //ID gasto
    String billId;

    //Fecha
    Timestamp date;

    //Importe
    Double amount;

    //Tipo de gasto
    String type;

    //Descripción
    String description;


    //GETTERS AND SETTERS
    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public Bill() {
    }

    public Bill(String billId, Timestamp date, Double amount, String type, String description) {
        this.billId = billId;
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    public Bill(Timestamp date, Double amount, String type, String description) {
        this.date = date;
        this.amount = amount;
        this.type = type;
        this.description = description;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "date='" + date + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", billId='" + billId + '\'' +
                '}';
    }


}
