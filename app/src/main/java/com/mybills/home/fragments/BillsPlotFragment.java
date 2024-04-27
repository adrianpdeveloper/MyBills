package com.mybills.home.fragments;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.PieLegendWidget;
import com.androidplot.pie.PieRenderer;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.androidplot.ui.HorizontalPositioning;
import com.androidplot.ui.VerticalPositioning;
import com.androidplot.ui.widget.LegendWidget;
import com.androidplot.util.PixelUtils;
import com.mybills.R;
import com.mybills.databinding.FragmentBillsPlotBinding;
import com.mybills.databinding.FragmentFutureBillListBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

public class BillsPlotFragment extends Fragment {
    public BillsPlotFragment() {
        // Required empty public constructor
    }

    FragmentBillsPlotBinding binding;
    FirestoreBills firestoreBills;

    PieChart pieChart;

    HomeActivity homeActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBillsPlotBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        pieChart = binding.billsPc;
        firestoreBills = new FirestoreBills();
        Log.e("USUARIO PLOT", homeActivity.getUserId());

        firestoreBills.getMonthAmount(homeActivity.getUserId(), new FirestoreBills.onBillsAmountLoaded() {
            @Override
            public void onBillsAmountLoaded(Map<String, Double> totalMap, Double total) {
                fillPlot(totalMap);
                String formattedAmount = NumberFormat.getCurrencyInstance().format(total);
                binding.totalTv.setText(formattedAmount);

                FragmentActivity activity = getActivity();
                if (activity != null) {
                SummaryFragment summaryFragment = (SummaryFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.homeActivityFrame);
                if (summaryFragment != null) {
                    summaryFragment.showPlot();
                }
            }}
        });



    }

    private void fillPlot(Map<String, Double> totalMap) {
        for(Map.Entry<String, Double> entry: totalMap.entrySet()){
            Log.e("TIPO TOTAL", entry.toString());

            if (entry.getValue()!=0) {
                switch (entry.getKey()) {
                    case "Cuentas y pagos":
                        addSegment(R.color.typeCuentas, entry.getKey(), entry.getValue());
                        binding.legendCuentas.setVisibility(View.VISIBLE);
                        break;
                    case "Alimentación":
                        addSegment(R.color.typeAlimentacion, entry.getKey(), entry.getValue());
                        binding.legendAlimentacion.setVisibility(View.VISIBLE);
                        break;
                    case "Vivienda":
                        addSegment(R.color.typeVivienda, entry.getKey(), entry.getValue());
                        binding.legendVivienda.setVisibility(View.VISIBLE);
                        break;
                    case "Transporte":
                        addSegment(R.color.typeTransporte, entry.getKey(), entry.getValue());
                        binding.legendTransporte.setVisibility(View.VISIBLE);
                        break;
                    case "Ropa":
                        addSegment(R.color.typeRopa, entry.getKey(), entry.getValue());
                        binding.legendRopa.setVisibility(View.VISIBLE);
                        break;
                    case "Salud e higiene":
                        addSegment(R.color.typeSalud, entry.getKey(), entry.getValue());
                        binding.legendSalud.setVisibility(View.VISIBLE);
                        break;
                    case "Ocio":
                        addSegment(R.color.typeOcio, entry.getKey(), entry.getValue());
                        binding.legendOcio.setVisibility(View.VISIBLE);
                        break;
                    case "Otros":
                        addSegment(R.color.typeOtros, entry.getKey(), entry.getValue());
                        binding.legendOtros.setVisibility(View.VISIBLE);
                        break;
                    default:
                        // Por si acaso, si no coincide con ningún caso, puedes usar un color predeterminado
                        addSegment(R.color.typeOtros, entry.getKey(), entry.getValue());
                        break;
                }
            }


            pieChart.getBorderPaint().setColor(Color.TRANSPARENT);
            pieChart.getBackgroundPaint().setColor(Color.TRANSPARENT);
            pieChart.invalidate();
        }
    }

    private void addSegment(int colorId, String entryKey, Double entryValue ){
        SegmentFormatter sf = new SegmentFormatter(binding.getRoot().getContext().getColor(colorId));
        sf.getLabelPaint().setTextSize(36); // Tamaño de texto deseado para mostrar el dinero

        sf.getLabelPaint().setColor(Color.BLACK); // Color de texto negro para mayor visibilidad

        sf.getRadialEdgePaint().setColor(Color.WHITE);
        sf.getRadialEdgePaint().setStrokeWidth(30);
        sf.getInnerEdgePaint().setColor(Color.TRANSPARENT);
        sf.getOuterEdgePaint().setColor(Color.TRANSPARENT);
        ;

        String formattedAmount = NumberFormat.getCurrencyInstance().format(entryValue);

        Segment segment = new Segment(formattedAmount, entryValue);

        pieChart.addSegment(segment, sf);
    }


}