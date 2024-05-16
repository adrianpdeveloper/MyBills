package com.mybills.home.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.Toast;

import com.google.firebase.Timestamp;
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
import java.util.List;
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
        binding.calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                setBillsRecycler(year,month,day);
            }
        });
    }


    private Timestamp convertSelectionToTimestamp(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        long timestampInMillis = calendar.getTimeInMillis();

        Timestamp timestamp = new Timestamp(timestampInMillis / 1000, 0);
        return timestamp;
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();


        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = dateFormat.format(new Date());
        binding.dayTv.setText(dateFormated);
        setBills(DateFormater.getTodayTimestamp());
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

    public void setBills(Timestamp timestamp) {
        firestoreBills.getDayBills(homeActivity.getUserId(), timestamp, new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> billArrayList) {
                adapter(billArrayList);
                if (!billArrayList.isEmpty()){
                    binding.billsRv.setVisibility(View.VISIBLE);
                    binding.emptyTv.setVisibility(View.GONE);
                }else {
                    binding.billsRv.setVisibility(View.GONE);
                    binding.emptyTv.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    public void navToDay(String stringDate) throws ParseException {
        Date date;
        if (stringDate.equalsIgnoreCase("hoy")){
             date= new Date();
        }else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
             date = sdf.parse(stringDate);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        binding.calendarView.setDate(calendar.getTimeInMillis());

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH); // ¡Recuerda que los meses comienzan desde 0!
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        setBillsRecycler(year,month,day);

    }

    public void setBillsRecycler(int year, int month, int day){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month - 1, day); // El mes en Calendar empieza desde 0, por eso restamos 1 al mes
        Date date = calendar.getTime();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dateFormated = dateFormat.format(date);

        binding.dayTv.setText(dateFormated);
        binding.billsRv.setVisibility(View.GONE);

        binding.calendarView.setOnDateChangeListener(null);

        Timestamp timestamp= convertSelectionToTimestamp(year, month, day);
        setBills(timestamp);

        listeners();
    };


}