package com.stonka.shopapp.ui.shakeomat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShakeomatViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public ShakeomatViewModel() {
        mText = new MutableLiveData<>();
    }

    public LiveData<String> getText() {
        return mText;
    }
}