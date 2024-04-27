package com.mybills.home;

import static com.mybills.utils.DateFormater.*;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
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


import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
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
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mybills.R;
import com.mybills.auth.AuthActivity;
import com.mybills.databinding.ActivityHomeBinding;
import com.mybills.home.fragments.BillList.TabBillListFragment;
import com.mybills.home.fragments.SummaryFragment;
import com.mybills.model.Bill;
import com.mybills.utils.MoneyInputFilter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.mybills.firebase.FirestoreBills;
public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    ActivityHomeBinding binding;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    View alertView;

    ArrayList<Bill> billArrayList = new ArrayList<>();
    String[] typeOptions;
    FirestoreBills firestoreBills;
    ProgressBar progressBar;

    MoneyInputFilter decimalFilter = new MoneyInputFilter(2);
    private FirebaseAuth mAuth;



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
        MenuItem menuItem = binding.navView.getMenu().getItem(3);
        changeMenuItemColor(menuItem);
    }
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
            Toast.makeText(this, "Segundo", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId()==R.id.nav_third){
            Toast.makeText(this, "Tercero", Toast.LENGTH_SHORT).show();
        }
        if (item.getItemId()==R.id.nav_first_2){
            mAuth.signOut();
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

    private void goToAuth() {
        startActivity(new Intent(HomeActivity.this, AuthActivity.class));
    }
    private void inflateFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new SummaryFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void showAddBillAlert() {
        // Inflar el XML de la alerta
        LayoutInflater inflater = LayoutInflater.from(HomeActivity.this);
        alertView = inflater.inflate(R.layout.alert_add_bill, null);

        // Crear el constructor del AlertDialog
        AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(HomeActivity.this);
        builder.setView(alertView);

        // Crear el AlertDialog
        final AlertDialog alertDialog = builder.create();

        showAddBillAlertListeners(alertDialog, alertView);

        // Configurar el fondo del AlertDialog
        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        // Mostrar el AlertDialog
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

    private void showAddBillAlertListeners(AlertDialog alertDialog, View alertView) {
        TextInputEditText date_et = alertView.findViewById(R.id.date_et);
        TextInputEditText amount_et = alertView.findViewById(R.id.amount_et);
        TextInputEditText description_et = alertView.findViewById(R.id.descripcion_et);
        TextInputLayout description_il = alertView.findViewById(R.id.descripcion_il);
        MaterialAutoCompleteTextView type_et = alertView.findViewById(R.id.type_et);
        MaterialButton aceptar_btn = alertView.findViewById(R.id.aceptar_btn);
        MaterialButton cancelar_btn = alertView.findViewById(R.id.cancelar_btn);
        MaterialCardView header_cv = alertView.findViewById(R.id.header_cv);

        SimpleDateFormat sdf=new SimpleDateFormat("dd/MM/yy");

        amount_et.setFilters(new InputFilter[]{decimalFilter});

        //Fecha
        date_et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });


        aceptar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

                firestoreBills.putBill(bill).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        Log.d("Resultado", "DocumentSnapshot added with ID: " + task.getResult().getId());
                    }
                })
                ;
                alertDialog.dismiss();
                FragmentManager fragmentManager = getSupportFragmentManager();

                Fragment fragment = fragmentManager.findFragmentById(R.id.homeActivityFrame);

                ((SummaryFragment) fragment).setBills();
            }
        });

        cancelar_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        //Opciones de tipo de gasto
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, getResources().getStringArray(R.array.billTypeOptions));
        type_et.setAdapter(adapter);

        type_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

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


        amount_et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (amount_et.getText().toString().isEmpty()){
                    amount_et.setText("0,00");
                }else if(amount_et.getText().toString().equalsIgnoreCase("0,00")){
                    amount_et.setText("");
                }
            }
        });

    }



    private void showDatePicker() {
        MaterialDatePicker datePicker =
                MaterialDatePicker.Builder.datePicker()
                        .setTitleText("Selecciona una fecha")
                        .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                        .build();

        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                long selectedDateInMillis = (Long) selection;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                TextInputEditText date_et = alertView.findViewById(R.id.date_et);
                if (getFirstDayOfMonthTimestamp().toString().equalsIgnoreCase(sdf.format(new Date(selectedDateInMillis)))){
                    date_et.setText("Hoy");
                }else {
                    date_et.setText(sdf.format(new Date(selectedDateInMillis)));
                }
            }
        });
        datePicker.show(getSupportFragmentManager(),"tag");
    }




    public void goToBillList(){
        showProgressBar();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.homeActivityFrame.getId(), new TabBillListFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    public void showProgressBar(){
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.VISIBLE);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation);
        progressBar.startAnimation(rotation);
    }

    public void hideProgressBar(){
        progressBar = binding.progressBar;
        progressBar.setVisibility(View.GONE);
    }

    public String getUserId(){
        return mAuth.getUid();
    }

}