package com.example.sheba_mental_health_project.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.example.sheba_mental_health_project.repository.AuthRepository;

public class MainActivityViewModel extends ViewModel {

    private final AuthRepository mAuthRepository;

//    TODO: add MutableLiveData

    private final String TAG = "MainActivityViewModel";

    public MainActivityViewModel(final Context context) {
        mAuthRepository = AuthRepository.getInstance(context);
    }

    public void logout() {
        mAuthRepository.logOut();
    }
}