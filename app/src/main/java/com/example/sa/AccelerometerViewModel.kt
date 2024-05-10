package com.example.sa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccelerometerViewModel: ViewModel() {
    
    val currentAccelerometerData: MutableLiveData<AccelerometerData> by lazy {
        MutableLiveData<AccelerometerData>()
    }
}
