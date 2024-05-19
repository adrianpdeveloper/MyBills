package com.mybills.home.fragments;

import static androidx.core.content.ContextCompat.getSystemService;
import static com.mybills.utils.DateFormater.convertMonthYear;
import static com.mybills.utils.DateFormater.getCurrentMonthYear;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.button.MaterialButton;
import com.mybills.R;
import com.mybills.databinding.FragmentReportBinding;
import com.mybills.firebase.FirestoreBills;
import com.mybills.home.HomeActivity;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;
import com.mybills.utils.adapters.BillAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Currency;
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

        //Carga el grafico
        setChartBills(monthValue);

        //Carga el recycler
        setBills();
    }

    private void setup() {
        homeActivity = (HomeActivity) getActivity();
        firestoreBills = new FirestoreBills();

        homeActivity.showProgressBar();

        //Cambia el titulo de la toolbar
        homeActivity.toolbarTitle(getString(R.string.toolbarTitleReport));

        //Por defecto se muestra el tipo Cuentas y pagos
        billType = "Cuentas y pagos";

        //Por defecto se muestra el mes actual
        monthValue = getCurrentMonthYear();
        //Set text con el mes actual a formato texto
        String actualMonth = convertMonthYear(monthValue).substring(0, 1).toUpperCase() + convertMonthYear(monthValue).substring(1);
        binding.monthTv.setText(actualMonth);

        //Carga las opciones del AutoCompleteTextView de tipo de gasto
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.billTypeOptions));
        binding.typeEt.setAdapter(adapter);


        getAllArrayBills();

        //Formato para los importes
        euroFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
        euroFormat.setCurrency(Currency.getInstance("EUR"));

    }

    //Refresca el grafico y el recycler
    public void refresh() {
        setChartBills(monthValue);
        setBills();
    }

    //Setup del grafico
    private void setPlot(Map<String, Double> totalMap) {
        //Leyendas
        binding.legendCuentas.setVisibility(View.GONE);
        binding.legendVivienda.setVisibility(View.GONE);
        binding.legendAlimentacion.setVisibility(View.GONE);
        binding.legendTransporte.setVisibility(View.GONE);
        binding.legendSalud.setVisibility(View.GONE);
        binding.legendRopa.setVisibility(View.GONE);
        binding.legendOcio.setVisibility(View.GONE);
        binding.legendOtros.setVisibility(View.GONE);

        barChart = binding.horizontalBarChart;
        //Obtiene el eje Y izquierdo del gráfico
        YAxis yAxis = barChart.getAxisLeft();

        //Crea una lista de entradas de barra y asigna un color a cada entrada
        List<BarEntry> entries = new ArrayList<>();
        List<Integer> entryColors = new ArrayList<>();
        Map<String, Double> orderedMap = sortMapByValue(totalMap);

        int index = 0;
        float maxValor = 0.0f;
        for (Map.Entry<String, Double> entry : orderedMap.entrySet()) {
            if (entry.getValue().floatValue() > maxValor) {
                maxValor = entry.getValue().floatValue();
            }
            if (entry.getValue() != 0) {
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
        //Establece el rango máximo y mínimo del eje Y
        //Rango máximo siempre sera el valor mas alto del gráfico + 10% de este
        yAxis.setAxisMaximum(maxValor + maxValor * 10 / 100);
        yAxis.setAxisMinimum(0);

        BarDataSet dataSet = new BarDataSet(entries, "Values");

        dataSet.setColors(entryColors);
        dataSet.setValueTextSize(8);
        ValueFormatter euroValueFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                //Formatea el valor como euros y devuelve la cadena resultante
                return euroFormat.format(value);
            }
        };

        dataSet.setValueFormatter(euroValueFormatter);


        BarData barData = new BarData(dataSet);
        // Establece los datos en el gráfico
        barChart.setData(barData);

        barChart.getXAxis().setTextColor(getContext().getColor(R.color.typeVivienda));

        //Desahibilita las funciones iteractivas
        barChart.setTouchEnabled(false);
        barChart.setClickable(false);
        barChart.setDoubleTapToZoomEnabled(false);
        barChart.setDoubleTapToZoomEnabled(false);

        //Styling del grafico
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

        //Deshabilita el eje Y izquierdo
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setEnabled(false);

        //Deshabilita el eje Y derecho
        YAxis rightAxis = barChart.getAxisRight();
        rightAxis.setEnabled(false);


        //Configura el gráfico para que no muestre descripción
        Description description = new Description();
        description.setText("");
        barChart.setDescription(description);

        //Actualiza el gráfico
        barChart.invalidate();
    }


    //Recoge los datos del mes para proveerlos en el grafico
    private void setChartBills(String month) {

        firestoreBills.getXMonthAmount(homeActivity.getUserId(), month, (totalMap, total) -> {
            totalAmount = total;
            homeActivity.hideProgressBar();
            binding.noBillsTv.setVisibility(View.GONE);
            //Si no hay gastos
            if (total <= 0) {
                homeActivity.showNoRegistry();
                binding.barChartCard.setVisibility(View.GONE);
                binding.typeIl.setVisibility(View.GONE);
                binding.billsRv.setVisibility(View.GONE);
                binding.noBillsTv.setVisibility(View.GONE);
                binding.createReportFab.setEnabled(false);
            } else { //Si hay gastos
                homeActivity.hideNoRegistry();
                setPlot(totalMap);
                binding.barChartCard.setVisibility(View.VISIBLE);
                binding.typeIl.setVisibility(View.VISIBLE);
                binding.billsRv.setVisibility(View.VISIBLE);
                binding.createReportFab.setEnabled(true);
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

                //Actualiza el recycler
                setBills();
            }
        });

        //Funcionalidad para mostrar o esconder los fabs secundarios
        binding.mainFab.setOnClickListener(view -> {
            if (binding.createReportFab.getVisibility() == View.GONE) {
                showFabs();
            } else {
                hideFabs();
            }
        });

        //Fab secundario de generar PDF
        binding.createReportFab.setOnClickListener(view -> {
            try {
                generatePDFPermission();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        //Fab secundario de abrir descargas
        binding.openReportsFab.setOnClickListener(view -> openBillsLocation());
    }

    //Funcionalidad para mostrar los fabs secundarios
    private void showFabs() {
        //Animaciones para mostrar los fabs
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

        //Cambia el color
        binding.mainFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.white)));
        binding.mainFab.setColorFilter(getActivity().getColor(R.color.goldPrimary));
    }

    //Funcionalidad para esconder los fabs secundarios
    private void hideFabs() {
        //Animaciones para esconder los fabs
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

        //Cambia el color
        binding.mainFab.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.goldPrimary)));
        binding.mainFab.setColorFilter(getActivity().getColor(R.color.white));
    }


    //Carga el recycler
    public void setBills() {
        firestoreBills.getXMonthTypeBills(homeActivity.getUserId(), monthValue, billType, billArrayList -> {
            adapter(billArrayList);
            //Si no esta vacio
            if (!billArrayList.isEmpty()) {
                binding.billsRv.setVisibility(View.VISIBLE);
                binding.recyclerTotalTv.setVisibility(View.VISIBLE);
                binding.noBillsTv.setVisibility(View.GONE);
            } else { //Si esta vacio
                binding.billsRv.setVisibility(View.INVISIBLE);
                binding.recyclerTotalTv.setVisibility(View.GONE);
                if (totalAmount > 0) {
                    binding.noBillsTv.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    //Adapter para recycler
    private void adapter(ArrayList<Bill> billArrayList) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());

        BillAdapter adapter = new BillAdapter(billArrayList, bill -> homeActivity.showModifyBillAlert(bill));

        binding.billsRv.setAdapter(adapter);
        binding.billsRv.setLayoutManager(layoutManager);
        binding.recyclerTotalTv.setText(euroFormat.format(getBillArrayTotalAmount(billArrayList)));

    }

    //Muestra la alert personalizada para elegir un mes
    public void showMonthPickerAlert() {
        //Infla el XML de la alerta
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View alertView = inflater.inflate(R.layout.alert_month_picker, null);

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getActivity());
        builder.setView(alertView);

        final AlertDialog alertDialog = builder.create();

        showAlertListeners(alertDialog, alertView);

        //Configura el fondo del AlertDialog
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();

        //Convierte a píxeles
        int dialogWidthInPixels = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                325,
                getResources().getDisplayMetrics()
        );

        //Configura el ancho del AlertDialog basado en el tamaño de la pantalla
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(alertDialog.getWindow().getAttributes());
        layoutParams.width = dialogWidthInPixels;
        alertDialog.getWindow().setAttributes(layoutParams);
    }

    //Listeners de la alert para elegir un mes
    private void showAlertListeners(AlertDialog alertDialog, View alertView) {
        //Elements view
        NumberPicker month_np = alertView.findViewById(R.id.month_np);
        NumberPicker year_np = alertView.findViewById(R.id.year_np);
        MaterialButton aceptar_btn = alertView.findViewById(R.id.aceptar_btn);
        MaterialButton cancelar_btn = alertView.findViewById(R.id.cancelar_btn);

        String[] monthsArray = getResources().getStringArray(R.array.yearMonths);
        String currentMonth;

        //Guarda el valor del mes
        if (monthValue.substring(0, 2).contains("/")) {
            monthValue = "0" + monthValue;
            currentMonth = monthValue.substring(0, 2);
        } else {
            currentMonth = monthValue.substring(0, 2);
        }

        //Guarda el valor del año
        String currentYear = monthValue.substring(monthValue.length() - 4);

        //Setup de numberPicker del mes
        month_np.setMinValue(1);
        month_np.setMaxValue(12);
        month_np.setDisplayedValues(monthsArray);
        month_np.setValue(Integer.parseInt(currentMonth));

        //Setup de numberPicker del año
        year_np.setMinValue(2015);
        year_np.setMaxValue(2030);
        year_np.setValue(Integer.parseInt(currentYear));

        month_np.setOnValueChangedListener((numberPicker, i, i1) -> {
            if (month_np.getValue() < 10) {
                monthValue = "0" + month_np.getValue() + "/" + year_np.getValue();
            } else {
                monthValue = month_np.getValue() + "/" + year_np.getValue();
            }
        });

        year_np.setOnValueChangedListener((numberPicker, i, i1) -> monthValue = month_np.getValue() + "/" + year_np.getValue());

        aceptar_btn.setOnClickListener(view -> {
            alertDialog.dismiss();
            //Titulo del informe
            binding.monthTv.setText(convertMonthYear(monthValue).substring(0, 1).toUpperCase() + convertMonthYear(monthValue).substring(1));

            homeActivity.showProgressBar();

            //Actualiza el chart
            setChartBills(monthValue);

            //Tipo de gasto
            billType = binding.typeEt.getText().toString();

            //Actualiza el recycler
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
        Collections.sort(entryList, Map.Entry.comparingByValue());

        // Crea un nuevo LinkedHashMap para mantener el orden de inserción
        Map<K, V> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    //Abre la localizacion de la carpeta de descargas
    public void openBillsLocation() {
        String downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Construye la ruta de la carpeta específica dentro de Descargas
        String folderPath = downloadsPath;
        Uri uri = Uri.parse(folderPath);
        intent.setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR);
        startActivity(Intent.createChooser(intent, "Open folder"));
    }

    //Genera PDF del informe del mes
    public void generatePDF() throws IOException {

        //Crea listas para cada tipo de gasto
        ArrayList<Bill> cuentasArray = new ArrayList<>();
        ArrayList<Bill> viviendaArray = new ArrayList<>();
        ArrayList<Bill> alimentacionArray = new ArrayList<>();
        ArrayList<Bill> ropaArray = new ArrayList<>();
        ArrayList<Bill> ocioArray = new ArrayList<>();
        ArrayList<Bill> otrosArray = new ArrayList<>();
        ArrayList<Bill> saludArray = new ArrayList<>();
        ArrayList<Bill> transporteArray = new ArrayList<>();

        //Clasifica las facturas en las listas correspondientes
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

        //Crea nuevo documento PDF
        PdfDocument document = new PdfDocument();

        int pageNumber = 1;
        //Define el tamaño de página para el PDF
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(1080, 1920, pageNumber).create();

        //Inicia una nueva página
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        //Dibuja el titulo
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        canvas.drawText("Gastos de " + convertMonthYear(monthValue), 100, 200, paint);

        //Carga el logo
        Bitmap logoBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.color_logo_2);

        //Define la posición y el tamaño del logo
        int logoWidth = 150;
        int logoHeight = 150;
        int margin = 40;
        int x = margin;
        int y = margin;

        //Dibuja el logo en la esquina superior izquierda
        Rect dstRect = new Rect(pageInfo.getPageWidth() - x - logoWidth, y, pageInfo.getPageWidth() - x, y + logoHeight);
        canvas.drawBitmap(logoBitmap, null, dstRect, paint);


        //Dibuja el grafico
        Rect dstRect2 = new Rect(200, 300, 1000, 1000);
        canvas.drawBitmap(getBitmapFromView(binding.barChartCard), null, dstRect2, paint); //Recupera el grafico de la vista

        //Define las dimensiones y la posición de las tablas
        int startX = 125;
        int startY = 1000;
        int cellWidth = 200;
        int cellHeight = 50;

        //Estilo del titulo de la tabla
        Paint paintTitle = new Paint();
        paintTitle.setColor(Color.BLACK);
        paintTitle.setStyle(Paint.Style.FILL);
        paintTitle.setTextSize(28);

        //Estilo del importe total de la tabla
        Paint paintTotalAmount = new Paint();
        paintTotalAmount.setColor(Color.BLACK);
        paintTotalAmount.setStyle(Paint.Style.FILL);
        paintTotalAmount.setTextSize(32);

        //Dibuja las tablas por cada tipo de gasto
        for (ArrayList<Bill> billArray : Arrays.asList(cuentasArray, alimentacionArray, viviendaArray, transporteArray, saludArray, ropaArray, ocioArray, otrosArray)) {
            if (!billArray.isEmpty()) {
                if (startY > pageInfo.getPageHeight() - 400) {
                    //Finaliza la página actual y comienza una nueva página
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(pageInfo.getPageWidth(), pageInfo.getPageHeight(), pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    startY = 100; //Establece la posición Y al principio de la nueva página
                }

                //Titulo de la tabla = Tipo de gasto + total de los importes
                Double totalAmount = getBillArrayTotalAmount(billArray);
                canvas.drawText(billArray.get(0).getType() + ": " + euroFormat.format(totalAmount), startX, startY, paintTitle);
                startY = startY + 50;

                //Define los colores y la apariencia de la tabla
                Paint contentPaint = new Paint();
                Paint linesPaint = new Paint();

                //Define los colores y la apariencia de las lineas
                linesPaint.setColor(Color.BLACK);
                linesPaint.setStyle(Paint.Style.STROKE);
                linesPaint.setStrokeWidth(2);

                //Define los colores y la apariencia de los textos
                contentPaint.setColor(Color.BLACK);
                contentPaint.setStyle(Paint.Style.FILL);
                contentPaint.setTextSize(20);

                //Define los tamaños de las celdas
                int firstColumnWidth = cellWidth * 2; // Ancho de la primera columna (el doble de ancho)
                int otherColumnWidth = cellWidth; // Ancho de las otras columnas

                //Dibuja los nombres de las columnas
                canvas.drawText("Descripción", startX + 5, startY, contentPaint); // Primer columna
                canvas.drawText("Importe", startX + firstColumnWidth + 5, startY, contentPaint); // Segunda columna
                canvas.drawText("Fecha", startX + firstColumnWidth + otherColumnWidth + 5, startY, contentPaint); // Tercera columna
                startY = startY + 10;

                //Dibuja el contenido de la tabla
                for (int i = 0; i < billArray.size(); i++) {
                    if (startY > pageInfo.getPageHeight() - 300) {
                        // Finalizar la página actual y comenzar una nueva página
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(pageInfo.getPageWidth(), pageInfo.getPageHeight(), pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        startY = 100;
                    }
                    Bill bill = billArray.get(i);
                    //Dibuja la primera columna con el doble de ancho
                    canvas.drawText(bill.getDescription(), startX + 10, startY + cellHeight - 10, contentPaint);
                    //Dibuja las otras dos columnas
                    canvas.drawText(euroFormat.format(bill.getAmount()), startX + firstColumnWidth + 10, startY + cellHeight - 10, contentPaint);
                    canvas.drawText(DateFormater.timestampToStringShort(bill.getDate()) + "", startX + firstColumnWidth + otherColumnWidth + 10, startY + cellHeight - 10, contentPaint);

                    canvas.drawLine(startX, startY, startX + (firstColumnWidth + otherColumnWidth * 2), startY, linesPaint);

                    for (int j = 0; j < 4; j++) {
                        //Dibuja las líneas verticales para la primera y segunda columna
                        if (j < 2) {
                            canvas.drawLine(startX + firstColumnWidth * j, startY, startX + firstColumnWidth * j, startY + cellHeight, linesPaint);
                        }
                        //Dibuja la línea vertical para la tercera columna
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

        //Termina la página
        document.finishPage(page);

        //Carpeta de descargas
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        //Nombre del fichero
        String monthYear = monthValue.replace("/", "-");
        String fileName = monthYear + "-gastos.pdf";
        int fileNameCont = 0;
        while (new File(downloadsDir, fileName).exists()) {
            fileNameCont++;
            fileName = "MyBills-" + monthYear + "(" + fileNameCont + ").pdf";
        }

        //Guarda el documento PDF
        File pdfFile = new File(downloadsDir, fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(pdfFile);
        document.writeTo(fileOutputStream);
        document.close();
        fileOutputStream.close();

        //Muestra notificacion de documento descargado
        createAndShowNotification();

        //Ruta del archivo
        Uri fileUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".provider", pdfFile);

        //Alerta para abrir archivo
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Abrir Informe")
                .setMessage("¿Deseas abrir el informe?")
                .setPositiveButton("Abrir", (dialog, which) -> {
                    if (pdfFile.exists()) {
                        //Abre archvio
                        Intent openFileIntent = new Intent(Intent.ACTION_VIEW);
                        openFileIntent.setDataAndType(fileUri, "application/pdf");
                        openFileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        try {
                            startActivity(openFileIntent);
                        } catch (Exception e) {
                            Toast.makeText(getActivity(), "No se ha encontrado una aplicación para abrir el archivo.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), "Archivo no encontrado.", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss());

        android.app.AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


    }

    //Muestra notificacion de documento descargado
    private void createAndShowNotification() {
        homeActivity.askNotificationPermission();
        // Obtener el contexto de la aplicación
        Context context = getActivity();

        // Configurar la intención para cuando el usuario toque la notificación
        Intent intent = new Intent(context, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        //Builder de la notificacion
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.color_logo_2)
                .setContentTitle("Informe descargado.")
                .setContentText("Su informe esta listo en la carpeta de descargas.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //Obtiene el administrador de notificaciones
        NotificationManager notificationManager = getSystemService(context, NotificationManager.class);

        //Comprueba si el dispositivo está ejecutando Android Oreo (API nivel 26) o superior
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

    //Pide los permisos en caso de ser necesario
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

    //Convierte una vista en bitmap
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(1900, 1800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    //Devuelve todos los gastos del mes otorgado
    public void getAllArrayBills() {
        firestoreBills.getXMonthBills(homeActivity.getUserId(), monthValue, billArrayList -> {
            if (!billArrayList.isEmpty()) {
                allArrayBills = billArrayList;
            }
        });
    }

    //Devuelve el total de los importes de la lista de gastos
    public Double getBillArrayTotalAmount(ArrayList<Bill> billArrayList) {
        Double acum = 0.0;
        for (Bill bill : billArrayList) {
            acum += bill.getAmount();
        }
        return acum;
    }

}