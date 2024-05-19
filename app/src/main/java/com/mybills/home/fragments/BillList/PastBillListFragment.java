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

    @Override
    public void onResume() {
        super.onResume();
        setup();
        setBills();
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();
    }

    //Carga las bills para cada recycler
    public void setBills() {
        homeActivity.showProgressBar();
        //Carga los gastos de la semana actual
        firestoreBills.getWeekBills(homeActivity.getUserId(), bills -> {
            weekBillsAdapter(bills);
        });

        //Carga los gastos de este mes hasta la semana actual
        firestoreBills.getMonthExceptTodayWeek(homeActivity.getUserId(), bills -> {
            monthBillsAdapter(bills);
        });

        //Carga los gastos de antes del mes actual
        firestoreBills.getBeforeMonthBills(homeActivity.getUserId(), bills ->{
            beforeBillsAdapter(bills);
        });

    }

    //Adapter semana
    private void weekBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));
        binding.weekBillsRv.setAdapter(adapter);
        binding.weekBillsRv.setLayoutManager(layoutManager);

        //Si hay registros se  muestra el recycler
        if (!billArrayList.isEmpty()){
            binding.weekBillsRv.setVisibility(View.VISIBLE);
            binding.weekTv.setVisibility(View.VISIBLE);
            homeActivity.hideProgressBar();
        }
    }

    //Adapter mes
    private void monthBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, bill -> {
            homeActivity.showModifyBillAlert(bill);
        });

        binding.monthBillsRv.setAdapter(adapter);
        binding.monthBillsRv.setLayoutManager(layoutManager);

        //Si hay registros se  muestra el recycler
        if (!billArrayList.isEmpty()){
            binding.monthBillsRv.setVisibility(View.VISIBLE);
            binding.monthTv.setVisibility(View.VISIBLE);
            homeActivity.hideProgressBar();
        }
    }

    //Adapter anterior al mes actual
    private void beforeBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));
        binding.beforeBillsRv.setAdapter(adapter);
        binding.beforeBillsRv.setLayoutManager(layoutManager);

        //Si hay registros se  muestra el recycler
        if (!billArrayList.isEmpty()){
            binding.beforeBillsRv.setVisibility(View.VISIBLE);
            binding.beforeTv.setVisibility(View.VISIBLE);
            homeActivity.hideProgressBar();
        }

        checkRecyclers();
    }

    //Comprueba si los recycles son visibles, si no lo son se muestra un mensaje que indica que no hay registros
    private void checkRecyclers(){
        if (binding.beforeBillsRv.getVisibility()==View.VISIBLE || binding.monthBillsRv.getVisibility()==View.VISIBLE || binding.weekBillsRv.getVisibility()==View.VISIBLE){
            Log.i("VISIBLE","VISIBLE");
            homeActivity.hideNoRegistry();
        }else {
            Log.i("NO VISIBLE","NO VISIBLE");
            homeActivity.hideProgressBar();
            homeActivity.showNoRegistry();
        }
    }
    
}