package com.example.sa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccelerometerViewModel: ViewModel() {
    // Create a LiveData object with a AccelerometerData object
    val currentAccelerometerData: MutableLiveData<Accelerometerdata> by lazy {
        MutableLiveData<Accelerometerdata>()
    }
}