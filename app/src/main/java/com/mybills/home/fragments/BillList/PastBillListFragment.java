package com.mybills.home.fragments.BillList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mybills.databinding.FragmentPastBillListBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;
import com.mybills.utils.adapters.BillAdapter;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class PastBillListFragment extends Fragment {

    FragmentPastBillListBinding binding;
    HomeActivity homeActivity;
    FirestoreBills firestoreBills;


    public PastBillListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPastBillListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setup();
        setBills();

    }


    //Carga las bills para cada recycler
    public void setBills() {
        //Carga los gastos de esta semana
        firestoreBills.getWeekBills(homeActivity.getUserId(), bills -> {
            weekBillsAdapter(bills);
            homeActivity.hideProgressBar();
        });

        //Carga los gastos de este mes hasta la semana actual
        firestoreBills.getMonthExceptTodayWeek(homeActivity.getUserId(), bills -> {
            monthBillsAdapter(bills);
        });

        //Carga los gastos hasta el mes actual
        firestoreBills.getBeforeMonthBills(homeActivity.getUserId(), bills ->{
            beforeBillsAdapter(bills);
        });
    }

    //Adapter semana
    private void weekBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList, new BillAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                Log.e("Fecha bill", bill.getDate().toString());
                Log.e("Fecha timestamp", DateFormater.getMondayTimestamp().toString());
                homeActivity.showModifyBillAlert(bill);
                Log.e("Pulsado", "PULSADO");
            }
        });
        binding.weekBillsRv.setAdapter(adapter);
        binding.weekBillsRv.setLayoutManager(layoutManager);
        if (!billArrayList.isEmpty()){
            binding.weekBillsRv.setVisibility(View.VISIBLE);
            binding.weekTv.setVisibility(View.VISIBLE);
        }
    }

    //Adapter mes
    private void monthBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, new BillAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                homeActivity.showModifyBillAlert(bill);
                Log.e("Pulsado", "PULSADO");
            }
        });

        binding.monthBillsRv.setAdapter(adapter);
        binding.monthBillsRv.setLayoutManager(layoutManager);
        if (!billArrayList.isEmpty()){
            binding.monthBillsRv.setVisibility(View.VISIBLE);
            binding.monthTv.setVisibility(View.VISIBLE);
        }
    }

    //Adapter anterior al mes actual
    private void beforeBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, new BillAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                homeActivity.showModifyBillAlert(bill);
                Log.e("Pulsado", "PULSADO");
            }
        });
        binding.beforeBillsRv.setAdapter(adapter);
        binding.beforeBillsRv.setLayoutManager(layoutManager);

        if (!billArrayList.isEmpty()){
            binding.beforeBillsRv.setVisibility(View.VISIBLE);
            binding.beforeTv.setVisibility(View.VISIBLE);
        }
        if (binding.beforeBillsRv.getVisibility()==View.GONE && binding.monthBillsRv.getVisibility()==View.GONE && binding.weekBillsRv.getVisibility()==View.GONE){
            homeActivity.showNoRegistry();
        }
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();


        homeActivity.hideNoRegistry();

        homeActivity.showProgressBar();

    }
    @Override
    public void onResume() {
        super.onResume();
        setup();
        setBills();
    }
    
}