package com.example.sa

data class GyroscopeData (
    var gyroscopeX: Float = 0.0f,
    var gyroscopeY: Float = 0.0f,
    var gyroscopeZ: Float = 0.0f,
    var accuracy: Int = 0,
    var timestamp: Long = 0
) {

    private fun copy(): GyroscopeData {
        var valueX = this.gyroscopeX
        var valueY = this.gyroscopeY
        var valueZ = this.gyroscopeZ
        var accuracy = this.accuracy
        var timestamp = this.timestamp
        return GyroscopeData(valueX, valueY, valueZ, accuracy,timestamp)
    }
}