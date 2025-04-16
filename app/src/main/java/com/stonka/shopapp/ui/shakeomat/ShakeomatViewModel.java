package com.stonka.shopapp.ui.shakeomat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel dla fragmentu Shakeomat, przechowujący dane, które mogą być obserwowane przez widok (np. tekst powiązany z nagrodą).
 */
public class ShakeomatViewModel extends ViewModel {

    private final MutableLiveData<String> mText; // Zmienna przechowująca tekst do wyświetlenia w UI

    /**
     * Konstruktor, który inicjalizuje MutableLiveData.
     */
    public ShakeomatViewModel() {
        mText = new MutableLiveData<>();
    }

    /**
     * Zwraca dane typu LiveData, które można obserwować w celu aktualizacji UI.
     * Wartością jest tekst, który może zostać zmieniony przez fragment.
     *
     * @return Obiekt LiveData zawierający tekst
     */
    public LiveData<String> getText() {
        return mText; // Zwracamy obiekt LiveData, który zapewnia reaktywne aktualizacje UI
    }
}
