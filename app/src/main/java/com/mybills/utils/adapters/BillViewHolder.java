package com.mybills.utils.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mybills.R;
import com.mybills.databinding.BillListBinding;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;

import java.text.NumberFormat;

public class BillViewHolder extends RecyclerView.ViewHolder {

    private final BillListBinding binding;

    private final DateFormater dateFormater;



    public BillViewHolder(@NonNull BillListBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
        dateFormater = new DateFormater();
    }

    //Binding de los elementos de los items
    public void bind(Bill bill){
        String type = bill.getType();
        binding.descripcionTv.setText(bill.getDescription());
        binding.typeTv.setText(bill.getType());

        //Convierte a formato de dinero
        String formattedAmount = NumberFormat.getCurrencyInstance().format(bill.getAmount());

        binding.amountTv.setText(formattedAmount);
        binding.dateTv.setText(dateFormater.timestampToStringLong(bill.getDate()));

        //Color de los detalles del recycler view segun el tipo
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Cuentas))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeCuentas)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Alimentacion))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeAlimentacion)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Ocio))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeOcio)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Otros))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeOtros)));

        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Ropa))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeRopa)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Salud))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeSalud)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Transporte))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeTransporte)));
        }
        if (type.equals(binding.getRoot().getContext().getResources().getString(R.string.billTypeOptions_Vivienda))){
            binding.listBackground.setBackgroundColor((binding.getRoot().getContext().getColor(R.color.typeVivienda)));
        }

    }
}
