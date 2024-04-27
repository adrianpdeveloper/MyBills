package com.mybills.home.fragments;

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
import android.widget.FrameLayout;

import com.mybills.databinding.FragmentSummaryBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.adapters.BillAdapter;

import java.util.ArrayList;

public class SummaryFragment extends Fragment {

    HomeActivity homeActivity;
    FirestoreBills firestoreBills;
    private FragmentSummaryBinding binding;
    BillsPlotFragment billsPlotFragment;


    public SummaryFragment() {
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
        binding = FragmentSummaryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        setup();
        listeners();
        setBills();
        setPlot();
    }

    public void setPlot() {
        FrameLayout frameLayout= binding.plotFrame;
        billsPlotFragment = new BillsPlotFragment();
        getChildFragmentManager().beginTransaction()
                .replace(binding.plotFrame.getId(), billsPlotFragment)
                .commit();
    }
    public void showPlot() {
        binding.plotCardview.setVisibility(View.VISIBLE);
        binding.plotFrame.setVisibility(View.VISIBLE);
    }


    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList);

        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
    }

    public void setBills() {

        firestoreBills.getBillsLast5(homeActivity.getUserId(), bills -> {
            Log.e("5 ultimas", bills.size()+"");
            adapter(bills);
            binding.listCardview.setVisibility(View.VISIBLE);
            homeActivity.hideProgressBar();
        });
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();
    }
    private void listeners(){
        binding.addBillFab.setOnClickListener(view -> homeActivity.showAddBillAlert());
        binding.seeMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeActivity.goToBillList();
            }
        });
    }
}