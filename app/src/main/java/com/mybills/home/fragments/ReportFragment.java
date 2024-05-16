package com.mybills.home.fragments;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.mybills.utils.DateFormater.convertMonthYear;
import static com.mybills.utils.DateFormater.getCurrentMonthYear;
import static com.mybills.utils.DateFormater.getFirstDayOfMonthTimestamp;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.androidplot.pie.PieChart;
import com.androidplot.xy.BarFormatter;
import com.androidplot.xy.BarRenderer;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart;
import com.github.mikephil.charting.renderer.YAxisRendererHorizontalBarChart;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mybills.R;
import com.mybills.databinding.FragmentReportBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;
import com.mybills.utils.adapters.BillAdapter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportFragment extends Fragment {

    private static final int REQUEST_CODE = 1232;
    private static final String CHANNEL_ID = "1234";
    private static final int NOTIFICATION_ID = 1;
    FragmentReportBinding binding;
    HomeActivity homeActivity;
    FirestoreBills firestoreBills;
    String monthValue;

    String billType;

    ArrayList<Bill> allArrayBills;

    HorizontalBarChart barChart;
    Double totalAmount;

    NumberFormat euroFormat;

    public ReportFragment() {
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
        binding = FragmentReportBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
        listeners();
        setChartBills(monthValue);
        setBills();
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();

        billType = "Cuentas y pagos";
        monthValue = getCurrentMonthYear();
        binding.monthTv.setText(convertMonthYear(monthValue).substring(0, 1).toUpperCase() + convertMonthYear(monthValue).substring(1));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.billTypeOptions));
        binding.typeEt.setAdapter(adapter);
        getAllArrayBills();

        euroFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        euroFormat.setCurrency(Currency.getInstance("EUR"));

    }
    public void refresh(){
        setChartBills(monthValue);
        setBills();
    }

    private void setPlot(Map<String,Double> totalMap) {
        binding.legendCuentas.setVisibility(View.GONE);
        binding.legendVivienda.setVisibility(View.GONE);
        binding.legendAlimentacion.setVisibility(View.GONE);
        binding.legendTransporte.setVisibility(View.GONE);
        binding.legendSalud.setVisibility(View.GONE);
        binding.legendRopa.setVisibility(View.GONE);
        binding.legendOcio.setVisibility(View.GONE);
        binding.legendOtros.setVisibility(View.GONE);

        barChart = binding.horizontalBarChart;
        // Obtiene el eje Y izquierdo del gráfico
        YAxis yAxis = barChart.getAxisLeft();

        // Crea una lista de entradas de barra y asigna un color a cada entrada
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> entryColors = new ArrayList<>();
        Map<String,Double> orderedMap = sortMapByValue(totalMap);
        int index = 0;
        float maxValor = 0.0f;
        for (Map.Entry<String, Double> entry : orderedMap.entrySet()) {
            if (entry.getValue().floatValue() > maxValor) {
                maxValor = entry.getValue().floatValue();
            }
            if (entry.getValue()!=0) {
                switch (entry.getKey()) {
                    case "Cuentas y pagos":
                        entryColors.add(getContext().getColor(R.color.typeCuentas));
                        binding.legendCuentas.setVisibility(View.VISIBLE);
                        break;
                    case "Alimentación":
                        entryColors.add(getContext().getColor(R.color.typeAlimentacion));
                        binding.legendAlimentacion.setVisibility(View.VISIBLE);
                        break;
                    case "Vivienda":
                        entryColors.add(getContext().getColor(R.color.typeVivienda));
                        binding.legendVivienda.setVisibility(View.VISIBLE);
                        break;
                    case "Transporte":
                        entryColors.add(getContext().getColor(R.color.typeTransporte));
                        binding.legendTransporte.setVisibility(View.VISIBLE);
                        break;
                    case "Ropa":
                        entryColors.add(getContext().getColor(R.color.typeRopa));
                        binding.legendRopa.setVisibility(View.VISIBLE);
                        break;
                    case "Salud e higiene":
                        entryColors.add(getContext().getColor(R.color.typeSalud));
                        binding.legendSalud.setVisibility(View.VISIBLE);
                        break;
                    case "Ocio":
                        entryColors.add(getContext().getColor(R.color.typeOcio));
                        binding.legendOcio.setVisibility(View.VISIBLE);
                        break;
                    case "Otros":
                        entryColors.add(getContext().getColor(R.color.typeOtros));
                        binding.legendOtros.setVisibility(View.VISIBLE);
                        break;
                    default:
                        entryColors.add(getContext().getColor(R.color.typeOtros));
                        break;
                }
                entries.add(new BarEntry(index++, entry.getValue().floatValue()));
            }

        }
        // Establece el rango máximo y mínimo del eje Y
        yAxis.setAxisMaximum(maxValor+maxValor*10/100);
        yAxis.setAxisMinimum(0);

        Log.e("ENTRIES", entries.toString());
        BarDataSet dataSet = new BarDataSet(entries, "Values");
        Log.e("dataSet", dataSet.toString());
        dataSet.setColors(entryColors);
        dataSet.setValueTextSize(8);
        ValueFormatter euroValueFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                // Crea un formateador de números con el estilo de moneda para euros


                // Formatea el valor como euros y devuelve la cadena resultante
                return euroFormat.format(value);
            }
        };

        dataSet.setValueFormatter(euroValueFormatter);


        BarData barData = new BarData(dataSet);
        // Establece los datos en el gráfico
        barChart.setData(barData);

        barChart.getXAxis().setTextColor(getContext().getColor(R.color.typeVivienda));

        barChart.setTouchEnabled(false);
        barChart.setClickable(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);

        barChart.setDrawBorders(false);
        barChart.setDrawGridBackground(false);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisLeft().setDrawAxisLine(false);

        barChart.getXAxis().setDrawGridLines(false);
        barChart.getXAxis().setDrawLabels(false);
        barChart.getXAxis().setDrawAxisLine(false);

        barChart.getAxisRight().setDrawGridLines(false);
        barChart.getAxisRight().setDrawLabels(false);
        barChart.getAxisRight().setDrawAxisLine(false);

        // Deshabilitar el eje Y izquierdo
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(false);

        // Deshabilitar el eje Y derecho
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);


        // Configurar el gráfico para que no muestre descripción
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        // Actualiza el gráfico
        barChart.invalidate();
    }



    private void setChartBills(String month) {

        firestoreBills.getXMonthAmount(homeActivity.getUserId(), month, (totalMap, total) -> {
            totalAmount=total;
            homeActivity.hideProgressBar();
            binding.noBillsTv.setVisibility(View.GONE);
            if(total<=0){
                homeActivity.showNoRegistry();
                binding.barChartCard.setVisibility(View.GONE);
                binding.typeIl.setVisibility(View.GONE);
                binding.billsRv.setVisibility(View.GONE);
                binding.noBillsTv.setVisibility(View.GONE);
            }else {
                homeActivity.hideNoRegistry();
                setPlot(totalMap);
                binding.barChartCard.setVisibility(View.VISIBLE);
                binding.typeIl.setVisibility(View.VISIBLE);
                binding.billsRv.setVisibility(View.VISIBLE);
            }


        });
    }

    private void listeners() {
        binding.monthPickerBtn.setOnClickListener(view -> showMonthPickerAlert());

        binding.typeEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                billType = binding.typeEt.getText().toString();
                setBills();
            }
        });

        binding.mainFab.setOnClickListener(view -> {
            if (binding.createReportFab.getVisibility()==View.GONE){
                showFabs();
            }else {
                hideFabs();
            }
        });

        binding.createReportFab.setOnClickListener(view -> {
            try {
                generatePDFPermission();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        binding.openReportsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBillsLocation();
            }
        });
    }

    private void showFabs() {

        binding.createReportFab.setVisibility(View.VISIBLE);
        binding.createReportFab.setTranslationY(binding.createReportFab.getHeight());
        binding.createReportFab.setAlpha(0.0f);
        binding.createReportFab.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(null);

        binding.openReportsFab.setVisibility(View.VISIBLE);
        binding.openReportsFab.setTranslationY(binding.openReportsFab.getHeight());
        binding.openReportsFab.setAlpha(0.0f);
        binding.openReportsFab.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(300)
                .setListener(null);

        binding.mainFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.white)));
        binding.mainFab.setColorFilter(getActivity().getColor(R.color.goldPrimary));
    }
    private void hideFabs() {
        binding.createReportFab.animate()
                .translationY(binding.createReportFab.getHeight())
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        binding.createReportFab.setVisibility(View.GONE);
                    }
                });

        binding.openReportsFab.animate()
                .translationY(binding.openReportsFab.getHeight())
                .alpha(0.0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        binding.openReportsFab.setVisibility(View.GONE);
                    }
                });


        binding.mainFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.goldPrimary)));
        binding.mainFab.setColorFilter(getActivity().getColor(R.color.white));
    }


    public void setBills() {
        firestoreBills.getXMonthTypeBills(homeActivity.getUserId(), monthValue, billType,billArrayList -> {
            adapter(billArrayList);
            if (!billArrayList.isEmpty()){
                binding.billsRv.setVisibility(View.VISIBLE);
                binding.recyclerTotalTv.setVisibility(View.VISIBLE);
                binding.noBillsTv.setVisibility(View.GONE);
            }else {
                binding.billsRv.setVisibility(View.INVISIBLE);
                binding.recyclerTotalTv.setVisibility(View.GONE);
                if (totalAmount>0){
                    binding.noBillsTv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));

        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
        binding.recyclerTotalTv.setText(euroFormat.format(getBillArrayTotalAmount(billArrayList)));

    }

    public void showMonthPickerAlert() {
        // Inflar el XML de la alerta
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.alert_month_picker, null);

        // Crear el constructor del AlertDialog
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setView(alertView);



        // Crear el AlertDialog
        final AlertDialog alertDialog = builder.create();

        showAlertListeners(alertDialog, alertView);

        // Configurar el fondo del AlertDialog
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();

        // Convertir a píxeles
        int dialogWidthInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                325,
                getResources().getDisplayMetrics()
        );

        // Configurar el ancho del AlertDialog basado en el tamaño de la pantalla
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = dialogWidthInPixels;
        alertDialog.getWindow().setAttributes(layoutParams);
    }

    private void showAlertListeners(AlertDialog alertDialog, View alertView) {
        //Elements view
        NumberPicker month_np = alertView.findViewById(R.id.month_np);
        NumberPicker year_np = alertView.findViewById(R.id.year_np);
        MaterialButton aceptar_btn = alertView.findViewById(R.id.aceptar_btn);
        MaterialButton cancelar_btn = alertView.findViewById(R.id.cancelar_btn);

        String[] monthsArray = getResources().getStringArray(R.array.yearMonths);
        String currentMonth;

        if (monthValue.substring(0,2).contains("/")){
            monthValue = "0" + monthValue;
            currentMonth = monthValue.substring(0,2);
        }else {
            currentMonth = monthValue.substring(0,2);
        }

        String currentYear = monthValue.substring(monthValue.length() - 4);

        month_np.setMinValue(1);
        month_np.setMaxValue(12);
        month_np.setDisplayedValues(monthsArray);
        month_np.setValue(Integer.parseInt(currentMonth));

        year_np.setMinValue(2015);
        year_np.setMaxValue(2030);
        year_np.setValue(Integer.parseInt(currentYear));

        month_np.setOnValueChangedListener((numberPicker, i, i1) -> {
            if (month_np.getValue()<10){
                monthValue = "0"+month_np.getValue()+"/"+year_np.getValue();
            }else {
                monthValue = month_np.getValue()+"/"+year_np.getValue();
            }

        });

        year_np.setOnValueChangedListener((numberPicker, i, i1) -> monthValue = month_np.getValue()+"/"+year_np.getValue());

        aceptar_btn.setOnClickListener(view -> {
            binding.monthTv.setText(convertMonthYear(monthValue).substring(0, 1).toUpperCase() + convertMonthYear(monthValue).substring(1));
            alertDialog.dismiss();
            homeActivity.showProgressBar();
            setChartBills(monthValue);
            billType = binding.typeEt.getText().toString();
            setBills();
            getAllArrayBills();
        });

        cancelar_btn.setOnClickListener(view -> alertDialog.dismiss());

    }

    // Función para ordenar un mapa por sus valores
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValue(Map<K, V> map) {
        // Convierte el mapa en una lista de entradas
        List<Map.Entry<K, V>> entryList = new ArrayList<>(map.entrySet());

        // Ordena la lista usando un comparador personalizado
        Collections.sort(entryList, new Comparator<Map.Entry<K, V>>() {
            @Override
            public int compare(Map.Entry<K, V> entry1, Map.Entry<K, V> entry2) {
                // Ordena de menor a mayor valor
                return entry1.getValue().compareTo(entry2.getValue());
            }
        });

        // Crea un nuevo LinkedHashMap para mantener el orden de inserción
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    public void openBillsLocation(){
        String downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Construye la ruta de la carpeta específica dentro de Descargas
        String folderPath = downloadsPath;
        Uri uri = Uri.parse(folderPath);
        intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR);
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    public void generatePDF() throws IOException {

        // Crear listas para cada tipo de gasto
        ArrayList<Bill> cuentasArray = new ArrayList<>();
        ArrayList<Bill> viviendaArray = new ArrayList<>();
        ArrayList<Bill> alimentacionArray = new ArrayList<>();
        ArrayList<Bill> ropaArray = new ArrayList<>();
        ArrayList<Bill> ocioArray = new ArrayList<>();
        ArrayList<Bill> otrosArray = new ArrayList<>();
        ArrayList<Bill> saludArray = new ArrayList<>();
        ArrayList<Bill> transporteArray = new ArrayList<>();

        // Clasificar las facturas en las listas correspondientes
        for (Bill bill : allArrayBills) {
            switch (bill.getType()) {
                case "Cuentas y pagos":
                    cuentasArray.add(bill);
                    break;
                case "Alimentación":
                    alimentacionArray.add(bill);
                    break;
                case "Vivienda":
                    viviendaArray.add(bill);
                    break;
                case "Transporte":
                    transporteArray.add(bill);
                    break;
                case "Ropa":
                    ropaArray.add(bill);
                    break;
                case "Salud e higiene":
                    saludArray.add(bill);
                    break;
                case "Ocio":
                    ocioArray.add(bill);
                    break;
                case "Otros":
                    otrosArray.add(bill);
                    break;
                default:
                    break;
            }
        }

        // Crear un nuevo documento PDF
        PdfDocument document = new PdfDocument();

        int pageNumber = 1;
        // Definir el tamaño de página para el PDF
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, pageNumber).create();

        // Iniciar una nueva página
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Dibujar el texto principal o contenido del PDF
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        canvas.drawText("Gastos de " + convertMonthYear(monthValue), 100, 200, paint);

        // Cargar el logo desde los recursos o la ruta de la imagen
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.color_logo_2);

        // Definir la posición y el tamaño del logo
        int logoWidth = 150; // Ancho del logo
        int logoHeight = 150; // Alto del logo
        int margin = 40; // Margen desde el borde
        int x = margin; // Posición X del logo (izquierda)
        int y = margin; // Posición Y del logo (arriba)

        // Dibujar el logo en la esquina superior izquierda
        Rect dstRect = new Rect(pageInfo.getPageWidth() - x - logoWidth, y, pageInfo.getPageWidth() - x, y + logoHeight);
        canvas.drawBitmap(logoBitmap, null, dstRect, paint);

        Rect dstRect2 = new Rect(200, 300, 1000, 1000);
        canvas.drawBitmap(getBitmapFromView(binding.barChartCard), null, dstRect2, paint);

        // Definir las dimensiones y la posición de las tablas
        int startX = 125; // Posición X de inicio de la tabla
        int startY = 1000; // Posición Y de inicio de la tabla
        int cellWidth = 200; // Ancho de las celdas de la tabla
        int cellHeight = 50; // Alto de las celdas de la tabla

        Paint paintTitle = new Paint();
        paintTitle.setColor(Color.BLACK);
        paintTitle.setStyle(Paint.Style.FILL);
        paintTitle.setTextSize(28); // Tamaño del título

        Paint paintTotalAmount = new Paint();
        paintTotalAmount.setColor(Color.BLACK);
        paintTotalAmount.setStyle(Paint.Style.FILL);
        paintTotalAmount.setTextSize(32); // Tamaño del título

        // Dibujar tablas para cada tipo de gasto
        Log.d("DEBUG", "Cuentas y pagos: " + cuentasArray.size());
        Log.d("DEBUG", "Alimentación: " + alimentacionArray.size());


        for (ArrayList<Bill> billArray : Arrays.asList(cuentasArray, alimentacionArray, viviendaArray, transporteArray, saludArray, ropaArray, ocioArray, otrosArray)) {
            if (!billArray.isEmpty()) {
                if (startY > pageInfo.getPageHeight() - 200) {
                    // Finalizar la página actual y comenzar una nueva página
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageInfo.getPageWidth(), pageInfo.getPageHeight(), pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    startY = 100; // Establecer la posición Y al principio de la nueva página
                }

                Double totalAmount = getBillArrayTotalAmount(billArray);
                canvas.drawText(billArray.get(0).getType()+": "+euroFormat.format(totalAmount), startX, startY, paintTitle);
                startY = startY + 50;

                Paint contentPaint = new Paint();
                Paint linesPaint = new Paint();

                // Definir los colores y la apariencia de la tabla
                linesPaint.setColor(Color.BLACK);
                linesPaint.setStyle(Paint.Style.STROKE);
                linesPaint.setStrokeWidth(2);

                contentPaint.setColor(Color.BLACK);
                contentPaint.setStyle(Paint.Style.FILL);
                contentPaint.setTextSize(20);

                // Definir los tamaños de las celdas
                int firstColumnWidth = cellWidth * 2; // Ancho de la primera columna (el doble de ancho)
                int otherColumnWidth = cellWidth; // Ancho de las otras columnas

                // Dibujar los nombres de las columnas
                canvas.drawText("Descripción", startX + 5, startY, contentPaint); // Primer columna
                canvas.drawText("Importe", startX + firstColumnWidth + 5, startY, contentPaint); // Segunda columna
                canvas.drawText("Fecha", startX + firstColumnWidth + otherColumnWidth + 5, startY, contentPaint); // Tercera columna
                startY = startY + 10;

                for (int i = 0; i <= billArray.size(); i++) {

                }


                // Dibujar el contenido de la tabla (datos de las facturas)
                for (int i = 0; i < billArray.size(); i++) {
                    if (startY > pageInfo.getPageHeight() - 200) {
                        // Finalizar la página actual y comenzar una nueva página
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(pageInfo.getPageWidth(), pageInfo.getPageHeight(), pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        startY = 100;
                    }
                    Bill bill = billArray.get(i);
                    // Dibujar la primera columna con el doble de ancho
                    canvas.drawText(bill.getDescription(), startX + 10, startY + cellHeight - 10, contentPaint);
                    // Dibujar las otras dos columnas
                    canvas.drawText(euroFormat.format(bill.getAmount()), startX + firstColumnWidth + 10, startY + cellHeight - 10, contentPaint);
                    canvas.drawText(DateFormater.timestampToStringShort(bill.getDate()) + "", startX + firstColumnWidth + otherColumnWidth + 10, startY + cellHeight - 10, contentPaint);

                    canvas.drawLine(startX, startY, startX + (firstColumnWidth + otherColumnWidth * 2), startY, linesPaint);

                    for (int j = 0; j < 4; j++) {
                        // Dibujar las líneas verticales para la primera y segunda columna
                        if (j < 2) {
                            canvas.drawLine(startX + firstColumnWidth * j, startY, startX + firstColumnWidth * j, startY + cellHeight, linesPaint);
                        }
                        // Dibujar la línea vertical para la tercera columna
                        else {
                            canvas.drawLine(startX + firstColumnWidth + otherColumnWidth * (j - 1), startY, startX + firstColumnWidth + otherColumnWidth * (j - 1), startY + cellHeight, linesPaint);
                        }
                    }
                    startY = startY + cellHeight;
                    canvas.drawLine(startX, startY, startX + (firstColumnWidth + otherColumnWidth * 2), startY, linesPaint);
                }
                startY += 100;
                }
            }

            // Terminar la página
            document.finishPage(page);

            // Guardar el documento PDF
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            String monthYear = monthValue.replace("/", "-");
            String fileName = monthYear + "-gastos.pdf";
            int fileNameCont = 0;

            while (new File(downloadsDir, fileName).exists()) {
                fileNameCont++;
                fileName = "MyBills-" + monthYear + "(" + fileNameCont + ").pdf";
            }
            File pdfFile = new File(downloadsDir, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
            document.writeTo(fileOutputStream);
            document.close();
            fileOutputStream.close();

        createAndShowNotification();

        Uri fileUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", pdfFile);

        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Abrir Informe")
                .setMessage("¿Deseas abrir el informe?")
                .setPositiveButton("Abrir", (dialog, which) -> {
                    if (pdfFile.exists()){
                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setDataAndType(fileUri, "application/pdf");
                        openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            startActivity(openFileIntent);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "No application found to open this file.", Toast.LENGTH_SHORT).show();
                        }
                    }else {
                        Toast.makeText(getActivity(), "File not found.", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss(); // Cierra el diálogo
                    }
                });

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    private void createAndShowNotification() {
        homeActivity.askNotificationPermission();
        // Obtener el contexto de la aplicación
        Context context = getActivity();

        // Configurar la intención para cuando el usuario toque la notificación
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        // Construir la notificación
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.color_logo_2)
                .setContentTitle("Informe descargado.")
                .setContentText("Su informe esta listo en la carpeta de descargas.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Obtener el administrador de notificaciones
        NotificationManager notificationManager = getSystemService(context, NotificationManager.class);

        // Comprobar si el dispositivo está ejecutando Android Oreo (API nivel 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "My Channel";
            String description = "Channel Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Registrar el canal en el administrador de notificaciones
            notificationManager.createNotificationChannel(channel);
        }

        // Mostrar la notificación
        notificationManager.notify(NOTIFICATION_ID, builder.build());

    }




    private void generatePDFPermission() throws IOException {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            // Dispositivo con Android 6.0 o superior, se requiere solicitud de permiso en tiempo de ejecución
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // El permiso no ha sido concedido, solicitarlo al usuario
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            } else {
                generatePDF();
            }
        } else {
            generatePDF();
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(1900, 1800,Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    public void getAllArrayBills() {
        firestoreBills.getXMonthBills(homeActivity.getUserId(), monthValue, new FirestoreBills.OnBillsLoadedListener() {
            @Override
            public void onBillsLoaded(ArrayList<Bill> billArrayList) {
                if (!billArrayList.isEmpty()){
                    Log.e("TODAS LAS BILLS DEL MES 1",billArrayList.toString());
                    allArrayBills = billArrayList;
                }
            }
        });
    }

    public Double getBillArrayTotalAmount(ArrayList<Bill> billArrayList){
        Double acum = 0.0;
        for (Bill bill:billArrayList){
            acum+=bill.getAmount();
        }
        return acum;
    }

    }