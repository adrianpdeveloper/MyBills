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

        binding.emailIl.setError(null);
        binding.passwordIl.setError(null);
    }



    private void listeners() {

        binding.signInBtn.setOnClickListener(view -> checkInputs());

        //Comprueba el formato
        binding.emailEt.setOnFocusChangeListener((view, b) -> {
            if (!binding.emailEt.getText().toString().isEmpty()) {
                if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEt.getText().toString()).matches() || binding.emailEt.getText().toString().equals("")) {
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

        //Va a registro
        binding.signUpTv.setOnClickListener(view -> authActivity.goToRegister());

        //Reset de password
        binding.resetPasswordTv.setOnClickListener(view -> authActivity.resetPassword());

        //Si se pulsa boton hacia atras, minimiza la app
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

                authActivity.minimizeApp();
            }
        });

        binding.googleSignInBtn.setOnClickListener(view -> authActivity.googleSignIn());
    }

    //Checkea que los campos estan correctamente rellenados.
    private void checkInputs() {
        if (binding.emailEt.getText().toString().isEmpty()){
            binding.emailIl.setError(getResources().getString(R.string.emailSetError));
        }
        if (binding.passwordEt.getText().toString().isEmpty()){
            binding.passwordIl.setError(getResources().getString(R.string.passwordSetError));
        }
        if (binding.emailIl.getError()==null && binding.passwordIl.getError()==null){
            authActivity.emailSignIn(binding.emailEt.getText().toString(), binding.passwordEt.getText().toString());
        }else{
            Snackbar.make(getActivity().findViewById(android.R.id.content), getResources().getString(R.string.genericSetInputError), Snackbar.LENGTH_LONG).show();
        }
    }


}