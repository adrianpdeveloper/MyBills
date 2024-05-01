package com.mybills.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.mybills.R;
import com.mybills.auth.fragments.SignInFragment;
import com.mybills.auth.fragments.SignUpFragment;
import com.mybills.databinding.ActivityAuthBinding;
import com.mybills.home.HomeActivity;

public class AuthActivity extends AppCompatActivity {

    ActivityAuthBinding binding;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setup();
        inflateFragment();
    }

    private void setup() {
        mAuth = FirebaseAuth.getInstance();
        //Si ya hay sesión va a la home
        if (mAuth.getCurrentUser()!=null){
            goToHome();
        }
    }

    //Inicia sesión
    public void signIn(String email, String pass) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            goToHome();
                        }else {
                            Toast.makeText(AuthActivity.this, "E-mail o contraseña erroneos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Ir a la home
    private void goToHome() {
        startActivity(new Intent(AuthActivity.this, HomeActivity.class));
    }

    //Registrar cuenta
    public void signUp(String email, String pass){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        goToHome();
                    }else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException){
                            Toast.makeText(AuthActivity.this, "Ya existe una cuenta con este e-mail.", Toast.LENGTH_SHORT).show();
                        }else  {
                            Toast.makeText(AuthActivity.this, "Error de inicio de sesion.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //Infla el fragment de inicio de sesion.
    private void inflateFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.authActivityFrame.getId(), new SignInFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Infla el fragment de registro.
    public void goToRegister() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.authActivityFrame.getId(), new SignUpFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Infla el fragment de inicio de sesion.
    public void goToSignin() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(binding.authActivityFrame.getId(), new SignInFragment())
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    //Muestra los terminos y condiciones
    public void terminosCondiciones() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Términos y Condiciones de Uso.")
                .setMessage(getResources().getString(R.string.termsAndConditions))
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Acción al hacer clic en el botón "Aceptar"
                        dialog.dismiss(); // Cierra el diálogo
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SignUpFragment signUpFragment = (SignUpFragment) getSupportFragmentManager().findFragmentById(R.id.auth_activity_frame);
                        signUpFragment.uncheckCheckBox();
                        dialogInterface.dismiss(); // Cierra el diálogo
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }

    //Funcion para recibir e-mail de recuperar contraseña
    public void resetPassword() {
        final TextInputEditText email_et = new TextInputEditText(this);


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                .setTitle("Cambiar contraseña.")
                .setMessage(getResources().getString(R.string.resetPasswordMessage))
                .setView(email_et)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    // Acción al hacer clic en el botón "Aceptar"
                    if (!Patterns.EMAIL_ADDRESS.matcher(email_et.getText().toString()).matches() || email_et.getText().toString().isEmpty()){
                        Toast.makeText(AuthActivity.this, "Introduce un e-mail válido.", Toast.LENGTH_SHORT).show();
                    } else {
                        mAuth.sendPasswordResetEmail(email_et.getText().toString()).addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Toast.makeText(AuthActivity.this, "Se ha enviado un correo al e-mail proporcionado.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss(); // Cierra el diálogo
                            }else {
                                Toast.makeText(AuthActivity.this, "No existe una cuenta con ese e-mail.", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }

                })
                .setNegativeButton("Cancelar", (dialogInterface, i) -> {
                    dialogInterface.dismiss(); // Cierra el diálogo
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

    }




}