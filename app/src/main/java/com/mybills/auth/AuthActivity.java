package com.mybills.auth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.mybills.R;
import com.mybills.auth.fragments.SignInFragment;
import com.mybills.auth.fragments.SignUpFragment;
import com.mybills.databinding.ActivityAuthBinding;
import com.mybills.home.HomeActivity;

public class AuthActivity extends AppCompatActivity {

    ActivityAuthBinding binding;

    private FirebaseAuth mAuth;

    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private GoogleSignInOptions gso;

    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setup();
        inflateFragment();
    }

    private void setup() {
        googleSignInSetup();
        mAuth = FirebaseAuth.getInstance();

        //Si se llega a esta pantalla desde el boton de logout
        Intent intent = getIntent();
        if (intent!=null){
            boolean receivedBoolean = intent.getBooleanExtra("signOut", false);
            if (receivedBoolean){
                Log.d("LOGOUT","singOut");
                signOut();
            }
        }


        //Si ya hay una sesión va a la pantalla home.
        if (mAuth.getCurrentUser()!=null){
            goToHome();
        }


    }

    //Inicia sesión con email y contraseña
    public void emailSignIn(String email, String pass) {
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

    //Comprueba si el email esta verificado y va a la home activity
    private void goToHome() {
        if (mAuth.getCurrentUser().isEmailVerified()){
            startActivity(new Intent(AuthActivity.this, HomeActivity.class));
        } else {
            //Si el email no esta verificado
            //Envia email de verificación
            mAuth.getCurrentUser().sendEmailVerification();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this)
                    .setTitle("Debes verificar tu cuenta.")
                    .setMessage("Es necesario verificar la cuenta de correo. Se ha enviado un correo a tu e-mail.")
                    .setNegativeButton("Volver a enviar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAuth.getCurrentUser().sendEmailVerification();
                        }
                    })
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss(); // Cierra el diálogo
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            //Si se pulsa en enviar nuevo email se deshabilita el boton
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                }
            });

            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    Snackbar.make(findViewById(android.R.id.content), "Si ya has verificado el e-mail, puedes volver a iniciar sesión.", Snackbar.LENGTH_LONG).show();
                }
            });
        }


    }

    //Registrar cuenta con email y contraseña
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
                        //Si no esta deacurdo quita el check
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

    //Minimiza la aplicación
    public void minimizeApp() {moveTaskToBack(true);}

    //Ejecuta el signIn de google
    public void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    //Logout tanto de google como por cuenta de firebase auth.
    public void signOut(){
            mAuth.signOut();
            mGoogleSignInClient.signOut();
    }

    //Setup de sign in de google
    public void googleSignInSetup(){
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleSignInResult(task);
                    } else {
                        Log.w("GOOGLE SIGN IN", "Google sign in failed");
                    }
                });

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d("GOOGLE SIGN IN", "firebaseAuthWithGoogle:" + account.getId());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w("GOOGLE SIGN IN", "Google sign in failed", e);
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("GOOGLE SIGN IN", "signInWithCredential:success");
                            Log.d("GOOGLE SIGN IN", "USER:" + mAuth.getCurrentUser().getEmail());
                            goToHome();

                        } else {
                            Log.w("GOOGLE SIGN IN", "signInWithCredential:failure", task.getException());

                        }
                    }
                });
    }



}