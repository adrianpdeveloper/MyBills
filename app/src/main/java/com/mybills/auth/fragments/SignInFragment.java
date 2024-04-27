package com.mybills.auth.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.mybills.R;
import com.mybills.auth.AuthActivity;
import com.mybills.databinding.FragmentSignInBinding;

public class SignInFragment extends Fragment {


    private FragmentSignInBinding binding;

    AuthActivity authActivity;



    public SignInFragment() {
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
        binding = FragmentSignInBinding.inflate(inflater, container,false);
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

        binding.signInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInputs();
            }
        });


        binding.emailEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!binding.emailEt.getText().toString().isEmpty()) {
                    if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString()).matches() || binding.emailEt.getText().toString().equals("")) {
                        binding.emailIl.setError("Introduce un email valido");
                    }else{
                        binding.emailIl.setError(null);
                        binding.emailIl.setErrorEnabled(false);
                    }
                }
            }
        });
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
                    binding.passwordIl.setError("Introduce una contraseña válida");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authActivity.goToRegister();
            }
        });

        binding.resetPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        binding.resetPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                authActivity.resetPassword();
            }
        });
    }

    private void checkInputs() {
        if (binding.emailEt.getText().toString().isEmpty()){
            binding.emailIl.setError("Introduce un e-mail válido");

        }
        if (binding.passwordEt.getText().toString().isEmpty()){
            binding.passwordIl.setError("Introduce una contraseña válida");
        }
        if (binding.emailIl.getError()==null && binding.passwordIl.getError()==null){
            authActivity.signIn(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString());
        }else{
            Toast.makeText(getContext(), "Introduce los datos correctamente", Toast.LENGTH_SHORT).show();
        }
    }


}