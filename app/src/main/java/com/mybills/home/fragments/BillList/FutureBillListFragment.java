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

import com.mybills.R;
import com.mybills.databinding.FragmentFutureBillListBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.adapters.BillAdapter;

import java.util.ArrayList;

public class FutureBillListFragment extends Fragment {

    HomeActivity homeActivity;
    FirestoreBills firestoreBills;
    private FragmentFutureBillListBinding binding;

    private ArrayList<Bill> billArrayList;

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

    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList);

        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
    }

    public void setBills() {
        firestoreBills.getFutureBills(homeActivity.getUserId(),new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> bills) {
                adapter(bills);
                binding.billsRv.setVisibility(View.VISIBLE);
                homeActivity.hideProgressBar();
            }
        });

    }
    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();
    }
}