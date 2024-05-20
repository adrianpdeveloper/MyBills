package com.mybills.auth.fragments;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.mybills.R;
import com.mybills.auth.AuthActivity;
import com.mybills.databinding.FragmentSignUpBinding;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpFragment extends Fragment {

    private FragmentSignUpBinding binding;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Z])(?=.*\\d).+$";
    AuthActivity authActivity;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpBinding.inflate(inflater, container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setup();
        listeners();


    }
    private void setup() {
        authActivity = (AuthActivity) getActivity();
    }

    private void listeners() {
        //Comprueba que los terminos se han aceptado
        binding.terminosCb.setOnClickListener(view -> {
            if (binding.terminosCb.isChecked()) {
                authActivity.terminosCondiciones();
            }
        });

        //Comprueba el formato
        binding.emailEt.setOnFocusChangeListener((view, b) -> {
            if (!binding.emailEt.getText().toString().isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString()).matches() || binding.emailEt.getText().toString().isEmpty()) {
                    binding.emailIl.setError(getResources().getString(R.string.emailSetError));
                }else{
                    binding.emailIl.setError(null);
                    binding.emailIl.setErrorEnabled(false);
                }
            }
        });

        //Comprueba el formato
        binding.passwordEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.passwordEt.getText().toString().isEmpty()) {
                    binding.passwordIl.setError(null);
                    binding.passwordIl.setErrorEnabled(false);
                }else {
                    binding.passwordIl.setError(getResources().getString(R.string.passwordSetError));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Comprueba el formato
        binding.password2Et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!binding.password2Et.getText().toString().isEmpty() & binding.password2Et.getText().toString().equals(binding.passwordEt.getText().toString())) {
                    binding.password2Il.setError(null);
                    binding.password2Il.setErrorEnabled(false);
                }else if (!binding.password2Et.getText().toString().equals(binding.passwordEt.getText().toString())){
                    binding.password2Il.setError(getResources().getString(R.string.secondPasswordSetError));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Comprueba que se ha cumplimentado los datos
        binding.signUpBtn.setOnClickListener(view -> checkInputs());

        //Sign in de google
        binding.googleSignInBtn.setOnClickListener(view -> authActivity.googleSignIn());

        //Va a pantalla de SignIn
        binding.signInTv.setOnClickListener(view -> authActivity.goToSignin());

        //Si se pulsa boton atras, va a sign in
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                authActivity.goToSignin();

            }
        });
    }

    //Chekea que los campos estan correctamente rellenados.
    private void checkInputs() {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher passwordMatcher = pattern.matcher(binding.passwordEt.getText().toString());

        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString()).matches() || binding.emailEt.getText().toString().isEmpty()) {
            binding.emailIl.setError(getResources().getString(R.string.emailSetError));
        } else if (!passwordMatcher.matches()){
            binding.passwordIl.setError(getResources().getString(R.string.passSetErrorInfo));
        } else if (!binding.password2Et.getText().toString().equals(binding.passwordEt.getText().toString())){
            binding.password2Il.setError(getResources().getString(R.string.secondPasswordSetError));
        }  else if (!binding.terminosCb.isChecked()) {
            Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.termsAndConditionsSnack), Snackbar.LENGTH_LONG).show();
        } else {
            authActivity.signUp(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString());
        }
    }

    //Si no esta deacuerdo quital el check
    public void uncheckCheckBox() {
        if (binding.terminosCb != null) {
            binding.terminosCb.setChecked(false);
        }
    }



}