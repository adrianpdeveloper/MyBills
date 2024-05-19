package com.mybills.home.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.Timestamp;
import com.mybills.R;
import com.mybills.databinding.FragmentCalendarBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;
import com.mybills.utils.adapters.BillAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CalendarFragment extends Fragment {

    FragmentCalendarBinding binding;
    HomeActivity homeActivity;
    FirestoreBills firestoreBills;

    public CalendarFragment() {
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
        binding = FragmentCalendarBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
        listeners();
    }

    private void listeners() {
        binding.addBillFab.setOnClickListener(view -> homeActivity.showAddBillAlert());

        //Listener del calendarView, cuando se seleciona un dia se refresca el recycler con los gastos
        binding.calendarView.setOnDateChangeListener((calendarView, year, month, day) -> setBillsRecycler(year,month,day));
    }



    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = dateFormat.format(new Date());
        binding.dayTv.setText(dateFormated);

        //Carga el recycler
        setBills(DateFormater.getTodayTimestamp());

        //Cambia el titulo de la toolbar
        homeActivity.toolbarTitle(getString(R.string.toolbarTitleCalendar));
    }

    //Adapter setup
    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));


        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
    }

    //Carga el recycler
    public void setBills(Timestamp timestamp) {
        //Devuelve los gastos del dia otorgado
        firestoreBills.getDayBills(homeActivity.getUserId(), timestamp, billArrayList -> {
            adapter(billArrayList);
            if (!billArrayList.isEmpty()){
                binding.billsRv.setVisibility(View.VISIBLE);
                binding.emptyTv.setVisibility(View.GONE);
            }else {
                binding.billsRv.setVisibility(View.GONE);
                binding.emptyTv.setVisibility(View.VISIBLE);
            }
        });

    }

    //Cambia el dia seleccionado del calendarView
    public void navToDay(String stringDate) throws ParseException {
        Date date;
        if (stringDate.equalsIgnoreCase("hoy")){
             date= new Date();
        }else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
             date = sdf.parse(stringDate);
        }

        Calendar calendar = Calendar.getInstance();
        if (date!=null){
            calendar.setTime(date);
        }
        binding.calendarView.setDate(calendar.getTimeInMillis());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        //
        setBillsRecycler(year,month,day);
    }

    //Carga el recycler con el dia seleccionado
    public void setBillsRecycler(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day);
        Date date = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = dateFormat.format(date);

        binding.dayTv.setText(dateFormated);
        binding.billsRv.setVisibility(View.GONE);

        binding.calendarView.setOnDateChangeListener(null);

        Timestamp timestamp= convertSelectionToTimestamp(year, month, day);

        //Carga el recycler
        setBills(timestamp);

        listeners();
    }

    //Convierte la fecha seleccionada en el CalendarView a Timestamp
    private Timestamp convertSelectionToTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        long timestampInMillis = calendar.getTimeInMillis();

        Timestamp timestampReturn = new Timestamp(timestampInMillis / 1000, 0);
        return timestampReturn;
    }


}