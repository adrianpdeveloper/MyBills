package com.mybills.home.fragments.Summary;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

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

    @Override
    public void onResume() {
        super.onResume();

        setup();
        listeners();
        setBills();
        setPlot();
    }

    public void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        //Esconde mensaje de no hay registros
        homeActivity.hideNoRegistry();

        //Muestra simbolo de carga
        homeActivity.showProgressBar();

        //Cambia el titulo de la toolbar
        homeActivity.toolbarTitle(getString(R.string.toolbarTitleHome));


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

    //Esconde el grafico
    public void hidePlot() {
        binding.plotCardview.setVisibility(View.GONE);
        binding.plotFrame.setVisibility(View.GONE);
    }


    //Adapter setup
    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        //Adapter con lista de gastos y onClick que lleva a modificar gastos
        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));

        //Setea el adaptador al recyclerView
        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);

    }

    //Carga los ultimos 5 registros
    public void setBills() {
        firestoreBills.getBillsLast5(homeActivity.getUserId(), bills -> {
            adapter(bills);
            //Si hay gastos
            if (!bills.isEmpty()){
                homeActivity.hideProgressBar();
                binding.listCardview.setVisibility(View.VISIBLE);
            }else {
                homeActivity.hideProgressBar();
                binding.listCardview.setVisibility(View.GONE);
            }

            //Si los gastos no son visibles se muestra mensaje de no hay registros
            if (binding.listCardview.getVisibility()==View.GONE){
                homeActivity.showNoRegistry();

                //Animacion en fab de añadir gasto si no hay registros
                binding.addBillFab.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.pulse_fab));
            }
        });
    }

    //Refresca los datos del Plot
    public void refreshPlot() {
        FragmentManager fragmentManager = getChildFragmentManager();
        BillsPlotFragment billsPlotFragment = (BillsPlotFragment) fragmentManager.findFragmentById(R.id.plotFrame);

        billsPlotFragment.setup();
    }

    private void listeners(){
        //Muestra la alert para crear gasto
        binding.addBillFab.setOnClickListener(view -> homeActivity.showAddBillAlert());

        //Ir a la lista de bills
        binding.seeMoreBtn.setOnClickListener(view -> homeActivity.goToBillList());

        //Ir a la pantalla de informes
        binding.plotCardview.setOnClickListener(view -> homeActivity.goToReport());

        //Si se pulsa boton de ir hacia atras se minimiza la app
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                homeActivity.minimizeApp();
            }
        });
    }

}