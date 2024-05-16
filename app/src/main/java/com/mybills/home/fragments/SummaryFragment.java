package com.mybills.home.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mybills.R;
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

    //Infla fragmento del gráfico
    public void setPlot() {
        billsPlotFragment = new BillsPlotFragment();
        getChildFragmentManager().beginTransaction()
                .replace(binding.plotFrame.getId(), billsPlotFragment)
                .commit();
    }

    //Muestra el grafico
    public void showPlot() {
        binding.plotCardview.setVisibility(View.VISIBLE);
        binding.plotFrame.setVisibility(View.VISIBLE);
    }


    //Adapter setup
    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList, new BillAdapter.OnBillClickListener() {
            @Override
            public void onBillClick(Bill bill) {
                homeActivity.showModifyBillAlert(bill);
            }
        });

        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);

    }

    //Carga los ultimos 5 registros
    public void setBills() {
        firestoreBills.getBillsLast5(homeActivity.getUserId(), bills -> {
            Log.e("5 ultimas", bills.size()+"");
            adapter(bills);
            if (!bills.isEmpty()){
                binding.listCardview.setVisibility(View.VISIBLE);
            }

            homeActivity.hideProgressBar();
            if (binding.listCardview.getVisibility()==View.GONE && binding.plotCardview.getVisibility()==View.GONE){
                homeActivity.showNoRegistry();
            }
        });
    }
    public void refreshPlot() {
        FragmentManager fragmentManager = getChildFragmentManager();
        BillsPlotFragment billsPlotFragment = (BillsPlotFragment) fragmentManager.findFragmentById(R.id.plotFrame);

        billsPlotFragment.setup();

    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.hideNoRegistry();

        homeActivity.showProgressBar();


    }
    private void listeners(){
        //Muestra la alert para crear gasto
        binding.addBillFab.setOnClickListener(view -> homeActivity.showAddBillAlert());
        //Ir a la lista de bills
        binding.seeMoreBtn.setOnClickListener(view -> homeActivity.goToBillList());

        binding.plotCardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                homeActivity.goToReport();
            }
        });

        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                homeActivity.minimizeApp();
            }
        });
    }

}