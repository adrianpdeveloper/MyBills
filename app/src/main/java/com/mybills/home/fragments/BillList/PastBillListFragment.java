package com.mybills.home.fragments.BillList;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mybills.databinding.FragmentPastBillListBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.adapters.BillAdapter;

import java.util.ArrayList;


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


    public void setBills() {
        firestoreBills.getBillsLast5(homeActivity.getUserId(), new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> bills) {
                weekBillsAdapter(bills);
                homeActivity.hideProgressBar();
            }
        });

        firestoreBills.getMonthExceptTodayWeek(homeActivity.getUserId(), new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> bills) {
                monthBillsAdapter(bills);
            }
        });
        firestoreBills.getBeforeMonthBills(homeActivity.getUserId(), new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> bills) {
                beforeBillsAdapter(bills);
            }
        });
    }
    private void weekBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList);

        binding.weekBillsRv.setAdapter(adapter);
        binding.weekBillsRv.setLayoutManager(layoutManager);
        binding.weekBillsRv.setVisibility(View.VISIBLE);
        binding.weekTv.setVisibility(View.VISIBLE);
    }

    private void monthBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList);

        binding.monthBillsRv.setAdapter(adapter);
        binding.monthBillsRv.setLayoutManager(layoutManager);
        binding.monthBillsRv.setVisibility(View.VISIBLE);
        binding.monthTv.setVisibility(View.VISIBLE);
    }

    private void beforeBillsAdapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList);

        binding.beforeBillsRv.setAdapter(adapter);
        binding.beforeBillsRv.setLayoutManager(layoutManager);
        binding.beforeBillsRv.setVisibility(View.VISIBLE);
        binding.beforeTv.setVisibility(View.VISIBLE);
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();
    }
}