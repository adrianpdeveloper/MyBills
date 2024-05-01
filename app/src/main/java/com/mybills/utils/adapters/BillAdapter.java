package com.mybills.utils.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mybills.databinding.BillListBinding;
import com.mybills.model.Bill;

import java.util.ArrayList;
import java.util.List;

public class BillAdapter extends RecyclerView.Adapter<BillViewHolder> {

    private final ArrayList<Bill> items;

    private BillListBinding binding;

    private OnBillClickListener listener;

    public interface OnBillClickListener {
        void onBillClick(Bill bill); // Replace "Bill" with your actual data type
    }



    public BillAdapter(ArrayList<Bill> billArrayList, OnBillClickListener onBillClickListener) {
        this.items = billArrayList;
        this.listener = onBillClickListener;
    }

    public BillAdapter(ArrayList<Bill> billArrayList) {
        this.items = billArrayList;
    }


    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = BillListBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new BillViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        holder.bind(items.get(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener!=null){
                    listener.onBillClick(items.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (items != null){
            if (items.size()==0){
                return 0;
            }else {
                return items.size();
            }
        }
        return 0;
    }
}
