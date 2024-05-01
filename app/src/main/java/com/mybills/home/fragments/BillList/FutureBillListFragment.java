package com.mybills.home.fragments.BillList;

import android.app.FragmentManager;
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

import com.mybills.R;
import com.mybills.databinding.FragmentFutureBillListBinding;
import com.mybills.databinding.FragmentTabBillListBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.adapters.BillAdapter;

import java.util.ArrayList;

public class FutureBillListFragment extends Fragment {

    HomeActivity homeActivity;
    FirestoreBills firestoreBills;
    private FragmentFutureBillListBinding binding;


    public FutureBillListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentFutureBillListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
        setBills();

    }

    //Adapter setup
    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList, new BillAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                homeActivity.showModifyBillAlert(bill);
                Log.e("Pulsado", "PULSADO");
            }
        });


        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
    }

    public void setBills() {
        //Carga gastos futuros
        firestoreBills.getFutureBills(homeActivity.getUserId(), bills -> {
            adapter(bills);
            binding.billsRv.setVisibility(View.VISIBLE);
            homeActivity.hideProgressBar();
            if (bills.isEmpty()){
                homeActivity.showNoRegistry();
            }
        });

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