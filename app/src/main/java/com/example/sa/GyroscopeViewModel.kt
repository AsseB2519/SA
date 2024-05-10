package com.example.sa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GyroscopeViewModel: ViewModel() {
    
    val currentGyroscopeData: MutableLiveData<GyroscopeData> by lazy {
        MutableLiveData<GyroscopeData>()
    }
}
