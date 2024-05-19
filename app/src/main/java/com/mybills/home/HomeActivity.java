package com.mybills.home;

import static com.mybills.utils.DateFormater.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.mybills.R;
import com.mybills.auth.AuthActivity;
import com.mybills.databinding.ActivityHomeBinding;
import com.mybills.home.fragments.BillList.TabBillListFragment;
import com.mybills.home.fragments.CalendarFragment;
import com.mybills.home.fragments.ReportFragment;
import com.mybills.home.fragments.Summary.SummaryFragment;
import com.mybills.model.Bill;
import com.mybills.utils.DateFormater;
import com.mybills.utils.MoneyInputFilter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mybills.firebase.FirestoreBills;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ActivityHomeBinding binding;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    View alertView;
    FirestoreBills firestoreBills;
    AuthActivity authActivity;
    ProgressBar progressBar;

    MoneyInputFilter decimalFilter = new MoneyInputFilter(2);
    private FirebaseAuth mAuth;

    Boolean amountReady;

    Boolean descriptionReady;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setup();
        inflateFragment();

    }

    private void setup() {
        mAuth = FirebaseAuth.getInstance();
        firestoreBills = new FirestoreBills();
        authActivity = new AuthActivity();

        //TOOLBAR
        MaterialToolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        //NAV VIEW
        drawerLayout = findViewById(R.id.drawerLayout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_open_drawer, R.string.navigation_close_drawer);
        drawerLayout.addDrawerListener(toggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        binding.navView.setNavigationItemSelectedListener(this);
        binding.navView.setItemIconTintList(null);
        MenuItem menuItem = binding.navView.getMenu().getItem(4);
        changeMenuItemColor(menuItem);
    }

    //Cambia el color de el item de la navview
    private void changeMenuItemColor(MenuItem menuItem) {
        SpannableString coloredMenuItemTitle = new SpannableString(menuItem.getTitle());
        coloredMenuItemTitle.setSpan(new ForegroundColorSpan(Color.RED), 0, coloredMenuItemTitle.length(), 0);
        menuItem.setTitle(coloredMenuItemTitle);

        // Obtenemos el icono original del MenuItem
        Drawable originalIcon = menuItem.getIcon();

        // Creamos un nuevo Drawable a partir del icono original pero con el color modificado
        Drawable coloredIcon = DrawableCompat.wrap(originalIcon);
        DrawableCompat.setTint(coloredIcon, Color.RED);

        // Establecemos el nuevo icono con el color modificado en el MenuItem
        menuItem.setIcon(coloredIcon);
    }

    //NavView listener
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId()==R.id.nav_first){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(binding.homeActivityFrame.getId(), new SummaryFragment())
                    .addToBackStack(null)
                    .commitAllowingStateLoss();
        }
        if (item.getItemId()==R.id.nav_second){
            hideNoRegistry();
            goToBillList();
        }
        if (item.getItemId()==R.id.nav_third){
            hideNoRegistry();
            goToCalendar();
        }
        if (item.getItemId()==R.id.nav_fourth){
            goToReport();
        }
        if (item.getItemId()==R.id.nav_first_2){
            goToAuth();

        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Cambia el titulo de la toolbar
    public void toolbarTitle(String title){
        binding.toolbar.setTitle(title);
    }
    //Ir a activity de registro
    private void goToAuth() {
        startActivity(new Intent(HomeActivity.this, AuthActivity.class).putExtra("signOut",true));
    }

    //Infla fragment de resumen
    private void inflateFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new SummaryFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Muestra el alert de añadir gasto
    public void showAddBillAlert() {
        // Inflar el XML de la alerta
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        alertView = inflater.inflate(R.layout.alert_add_bill, null);

        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this);
        builder.setView(alertView);

        // Crear el AlertDialog
        final AlertDialog alertDialog = builder.create();

        //Listeners del alert personalizado
        showAddBillAlertListeners(alertDialog, alertView, false, null);

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

    //Muestra el alert de modificar gasto
    public void showModifyBillAlert(Bill bill) {
        // Inflar el XML de la alerta
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        alertView = inflater.inflate(R.layout.alert_modify_bill, null);

        // Crear el constructor del AlertDialog
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this);
        builder.setView(alertView);

        //Listeners del alert personalizado
        final AlertDialog alertDialog = builder.create();
        showAddBillAlertListeners(alertDialog, alertView, true, bill);

        //Configurar el fondo del AlertDialog
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

    //Listener del alert de añadir y modificar gasto
    private void showAddBillAlertListeners(AlertDialog alertDialog, View alertView, Boolean modify, Bill newBill) {

        //Elements view
        TextInputEditText date_et = alertView.findViewById(R.id.date_et);
        TextInputLayout date_il = alertView.findViewById(R.id.date_il);
        TextInputEditText amount_et = alertView.findViewById(R.id.amount_et);
        TextInputLayout amount_il = alertView.findViewById(R.id.amount_il);
        TextInputEditText description_et = alertView.findViewById(R.id.descripcion_et);
        TextInputLayout description_il = alertView.findViewById(R.id.descripcion_il);
        MaterialAutoCompleteTextView type_et = alertView.findViewById(R.id.type_et);
        MaterialButton aceptar_btn = alertView.findViewById(R.id.aceptar_btn);
        MaterialButton cancelar_btn = alertView.findViewById(R.id.cancelar_btn);
        MaterialCardView header_cv = alertView.findViewById(R.id.header_cv);
        MaterialButton date_btn = alertView.findViewById(R.id.date_btn);



        //Comprueba si se puede aceptar el registro
        amountReady = false;
        descriptionReady = false;

        //Si se esta modificando un gasto setea los valores de los campos
        if (modify && newBill!=null){
            date_et.setText(DateFormater.timestampToStringShort(newBill.getDate()));
            amount_et.setText(newBill.getAmount().toString().replace("€","").replace(".",","));
            description_et.setText(newBill.getDescription());
            type_et.setText(newBill.getType());

            //Comprueba si se puede aceptar el registro
            amountReady = true;
            descriptionReady = true;

            //Cambia el color segun el tipo
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Cuentas))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeCuentas));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Alimentacion))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeAlimentacion));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Ocio))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeOcio));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Otros))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeOtros));

            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Ropa))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeRopa));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Salud))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeSalud));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Transporte))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeTransporte));
            }
            if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Vivienda))){
                header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeVivienda));
            }
        }


        //Formato de EditText importe
        amount_et.setFilters(new InputFilter[]{decimalFilter});

        //Listeners importe
        amount_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!amount_et.getText().toString().isEmpty()){
                    if (Double.parseDouble(amount_et.getText().toString().replace(",","."))<=0 ){
                        amount_il.setError("Importe no válido");
                        amountReady = false;
                    }else {
                        amount_il.setError(null);
                        amount_il.setErrorEnabled(false);
                        amountReady = true;
                    }
                }else {
                    amount_il.setError("Importe no válido");
                    amountReady = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amount_et.setOnFocusChangeListener((view, b) -> {
            if (amount_et.getText().toString().isEmpty()){
                amount_et.setText("0,00");
            }else if(amount_et.getText().toString().equalsIgnoreCase("0,00")){
                amount_et.setText("");
            }
        });

        //Listener descripcion
        description_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (description_et.getText().toString().isEmpty()) {
                    description_il.setError("Descripción no puede estar vacia.");
                    descriptionReady = false;
                } else {
                    description_il.setError(null);
                    description_il.setErrorEnabled(false);
                    descriptionReady = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Llama al date picker
        date_btn.setOnClickListener(view -> showDatePicker());


        //Boton aceptar listener
        aceptar_btn.setOnClickListener(view -> {
            //Comprueba si estan todos los campos
            if (descriptionReady && amountReady){
                Map<String, Object> bill = new HashMap<>();
                //Tipo
                bill.put("type", type_et.getText().toString());

                //Fecha
                if (date_et.getText().toString().equalsIgnoreCase("hoy")){
                    bill.put("date", getTodayTimestamp());
                }else {
                    bill.put("date", stringToTimestamp(date_et.getText().toString()));
                }

                //Importe
                bill.put("amount", Double.parseDouble(amount_et.getText().toString().replace(",",".")));

                //Descripcion
                if (!description_et.getText().toString().isEmpty()){
                    bill.put("description", description_et.getText().toString());
                }

                //User ID
                bill.put("userId", mAuth.getCurrentUser().getUid());

                //Comprueba si es una modificacion o un registro nuevo
                if (!modify){
                    //Si es un registro nuevo usa funcion put
                    firestoreBills.putBill(bill).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            Log.d("Resultado", "DocumentSnapshot added with ID: " + task.getResult().getId());
                        }
                    });
                }else if (modify && newBill!=null){
                    //Si es un registro ya existente usa funcion update
                    firestoreBills.updateBill(bill, newBill.getBillId());
                }


                alertDialog.dismiss();
                FragmentManager fragmentManager = getSupportFragmentManager();

                //Vuelve a cargar las listas
                try {
                    refreshRecyclers(fragmentManager.findFragmentById(R.id.homeActivityFrame), date_et.getText().toString());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
            if (!amountReady){
                amount_il.setError("Importe no válido");
            }
            if (!descriptionReady){
                description_il.setError("Descripción no puede estar vacia.");
            }
        });

        cancelar_btn.setOnClickListener(view -> alertDialog.dismiss());

        //Opciones de tipo de gasto
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.billTypeOptions));
        type_et.setAdapter(adapter);

        //Tipo de gasto listener
        type_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Cuentas))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeCuentas));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Alimentacion))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeAlimentacion));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Ocio))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeOcio));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Otros))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeOtros));

                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Ropa))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeRopa));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Salud))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeSalud));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Transporte))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeTransporte));
                }
                if (type_et.getText().toString().equals(getResources().getString(R.string.billTypeOptions_Vivienda))){
                    header_cv.setCardBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.typeVivienda));
                }
            }
        });

        //Si se modifica un registro, funcionalidad de delete
        if (modify && newBill!=null){
            MaterialButton delete_btn = alertView.findViewById(R.id.delete_btn);
            delete_btn.setOnClickListener(view -> {
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(alertView.getContext())
                        .setTitle("Borrar Gasto")
                        .setMessage(getResources().getString(R.string.delete_bill))
                        .setPositiveButton("Aceptar", (dialog, which) -> {
                            //Borrar gasto
                            firestoreBills.deleteBill(newBill.getBillId());
                            dialog.dismiss(); // Cierra el diálogo
                            alertDialog.dismiss();
                            FragmentManager fragmentManager = getSupportFragmentManager();
                            //Vuelve a cargar las listas
                            try {
                                refreshRecyclers(fragmentManager.findFragmentById(R.id.homeActivityFrame), date_et.getText().toString());
                            } catch (ParseException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .setNegativeButton("Cancelar", (dialogInterface, i) -> {
                            dialogInterface.dismiss(); // Cierra el diálogo
                        });

                android.app.AlertDialog alertDialog1 = alertDialogBuilder.create();
                alertDialog1.show();
            });
        }
    }

    //Actualiza los distintos elementos de cada fragmento
    private void refreshRecyclers(Fragment fragment, String date ) throws ParseException {
        if (fragment instanceof SummaryFragment){
            //Actualiza el Recycler
            ((SummaryFragment) fragment).setBills();
            //Actualiza el Plot
            ((SummaryFragment) fragment).refreshPlot();
        }
        if (fragment instanceof TabBillListFragment){
            //Actualiza el recycler segun la posicion del viewPager
            int position = ((TabBillListFragment) fragment).getViewPagerPosition();
            ((TabBillListFragment) fragment).setup();
            ((TabBillListFragment) fragment).setViewPagerPosition(position);

        }
        if (fragment instanceof CalendarFragment){
            //Actualiza el Recycler y el dia del calendario
            ((CalendarFragment) fragment).navToDay(date);
        }
        if (fragment instanceof ReportFragment){
            //Actualiza el Recycler y graph de la pantalla de informe
            ((ReportFragment) fragment).refresh();
        }

    }

    //Material Date Picker para seleccionar la fecha en la alert de crear y modificar gasto.
    private void showDatePicker() {
        MaterialDatePicker datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Selecciona una fecha")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            long selectedDateInMillis = (Long) selection;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");

            TextInputEditText date_et = alertView.findViewById(R.id.date_et);
            if (getFirstDayOfMonthTimestamp().toString().equalsIgnoreCase(sdf.format(new Date(selectedDateInMillis)))){
                date_et.setText("Hoy");
            }else {
                date_et.setText(sdf.format(new Date(selectedDateInMillis)));
            }
        });
        datePicker.show(getSupportFragmentManager(),"tag");
    }

    //Pide permisos para activar las notificaciones
    public void askNotificationPermission() {
        //Si la API<33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            ) {

            } else {//Si la API>33
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    //Request de permisos de notificacion
    private ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notifications permission granted", Toast.LENGTH_SHORT).show();
                } else {

                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this)
                            .setTitle("Permisos de notificaciones.")
                            .setMessage("MyBills requiere de los permisos de notificaciones para poder utilizar todas las funcionalidades.")
                            .setPositiveButton("Aceptar", (dialog, which) -> {
                                Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                                startActivity(settingsIntent);
                                dialog.dismiss();
                            })
                            .setNegativeButton("Cancelar", (dialogInterface, i) -> dialogInterface.dismiss());
                }
            });

    //Ir a lista de gastos
    public void goToBillList(){
        showProgressBar();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new TabBillListFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Ir al calendario de gastos
    public void goToCalendar(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new CalendarFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Ir a la pantalla de informes
    public void goToReport(){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new ReportFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Muestra simbolo de garga
    public void showProgressBar(){
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        progressBar.startAnimation(rotation);
    }

    //Esconde simbolo de garga
    public void hideProgressBar(){
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.GONE);
    }

    //Muestra mensaje de no hay registros
    public void showNoRegistry(){
        binding.noBillsTv.setVisibility(View.VISIBLE);
    }

    //Esconde mensaje de no hay registros
    public void hideNoRegistry(){
        binding.noBillsTv.setVisibility(View.GONE);
    }

    //Id del usuario
    public String getUserId(){
        return mAuth.getUid();
    }

    //Minimiza la app
    public void minimizeApp() {moveTaskToBack(true);}




}