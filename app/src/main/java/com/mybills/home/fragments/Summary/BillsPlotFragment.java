package com.mybills.home.fragments.Summary;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.pie.PieChart;
import com.androidplot.pie.Segment;
import com.androidplot.pie.SegmentFormatter;
import com.mybills.R;
import com.mybills.databinding.FragmentBillsPlotBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;

import java.text.NumberFormat;
import java.util.Map;

public class BillsPlotFragment extends Fragment {
    public BillsPlotFragment() {}
    FragmentBillsPlotBinding binding;
    FirestoreBills firestoreBills;

    PieChart pieChart;

    HomeActivity homeActivity;

    double sevenPercent;

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

    public void setup() {
        homeActivity = (HomeActivity) getActivity();
        pieChart = binding.billsPc;
        firestoreBills = new FirestoreBills();

        //Devuelve el total de gastos del mes y un map con los gastos por tipo
        firestoreBills.getMonthAmount(homeActivity.getUserId(), (totalMap, total) -> {
            fillPlot(totalMap, total);

            //Formatea el String de importe
            String formattedAmount = NumberFormat.getCurrencyInstance().format(total);
            binding.totalTv.setText(formattedAmount);

            FragmentActivity activity = getActivity();
            if (activity != null) {
            SummaryFragment summaryFragment = (SummaryFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.homeActivityFrame);
            if (summaryFragment != null) {
                if (total>0){
                    //Muestra el plot
                    summaryFragment.showPlot();
                }
            }
        }});



    }

    //Carga los datos del grafico
    private void fillPlot(Map<String, Double> totalMap, Double total) {
        //Calcula el 7% del total
        sevenPercent = total * 0.07;

        //Vacia el plot
        pieChart.clear();

        //Rellena el plot por cada tipo
        for(Map.Entry<String, Double> entry: totalMap.entrySet()){
            if (entry.getValue()!=0) {
                switch (entry.getKey()) {
                    case "Cuentas y pagos":
                            addSegment(R.color.typeCuentas, entry.getValue());
                            binding.legendCuentas.setVisibility(View.VISIBLE);
                        break;
                    case "Alimentación":
                            addSegment(R.color.typeAlimentacion, entry.getValue());
                            binding.legendAlimentacion.setVisibility(View.VISIBLE);
                        break;
                    case "Vivienda":
                            addSegment(R.color.typeVivienda, entry.getValue());
                            binding.legendVivienda.setVisibility(View.VISIBLE);
                        break;
                    case "Transporte":
                            addSegment(R.color.typeTransporte, entry.getValue());
                            binding.legendTransporte.setVisibility(View.VISIBLE);
                        break;
                    case "Ropa":
                            addSegment(R.color.typeRopa, entry.getValue());
                            binding.legendRopa.setVisibility(View.VISIBLE);
                        break;
                    case "Salud e higiene":
                            addSegment(R.color.typeSalud, entry.getValue());
                            binding.legendSalud.setVisibility(View.VISIBLE);
                        break;
                    case "Ocio":
                            addSegment(R.color.typeOcio, entry.getValue());
                            binding.legendOcio.setVisibility(View.VISIBLE);
                        break;
                    case "Otros":
                            addSegment(R.color.typeOtros, entry.getValue());
                            binding.legendOtros.setVisibility(View.VISIBLE);
                        break;
                    default:
                        // Por si acaso, si no coincide con ningún caso, puedes usar un color predeterminado
                        addSegment(R.color.typeOtros, entry.getValue());
                        break;
                }
            }

            //Styling del grafico
            pieChart.getBorderPaint().setColor(Color.TRANSPARENT);
            pieChart.getBackgroundPaint().setColor(Color.TRANSPARENT);
            pieChart.invalidate();
        }
    }

    //Añade segmento al grafico
    private void addSegment(int colorId, Double entryValue ){
        SegmentFormatter sf = new SegmentFormatter(binding.getRoot().getContext().getColor(colorId));

        //Tamaño de texto de los importes
        sf.getLabelPaint().setTextSize(36);

        //Color de texto negro para mayor visibilidad
        sf.getLabelPaint().setColor(Color.BLACK);

        //Bordes del grafico
        sf.getRadialEdgePaint().setColor(Color.WHITE);
        sf.getRadialEdgePaint().setStrokeWidth(30);
        sf.getInnerEdgePaint().setColor(Color.TRANSPARENT);
        sf.getOuterEdgePaint().setColor(Color.TRANSPARENT);

        //Formatea los importes
        String formattedAmount = NumberFormat.getCurrencyInstance().format(entryValue);

        Segment segment;

        //Si el segmento es menor al 7% del total ocupara el 7% visualmente
        if (entryValue< sevenPercent){
            segment = new Segment(formattedAmount, sevenPercent);
        }else {
             segment = new Segment(formattedAmount, entryValue);
        }

        //Añade el segmento
        pieChart.addSegment(segment, sf);
    }


}