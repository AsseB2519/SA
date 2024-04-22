package com.example.sa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GyroscopeViewModel: ViewModel() {
    // Create a LiveData object with a AccelerometerData object
    val currentGyroscopeData: MutableLiveData<GyroscopeData> by lazy {
        MutableLiveData<GyroscopeData>()
    }
}