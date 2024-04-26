package com.example.sa

data class GyroscopeData (
    var valueX: Float = 0.0f,
    var valueY: Float = 0.0f,
    var valueZ: Float = 0.0f,
    var accuracy: Int = 0,
    var timestamp: Long = 0
) {

    private fun copy(): GyroscopeData {
        var valueX = this.valueX
        var valueY = this.valueY
        var valueZ = this.valueZ
        var accuracy = this.accuracy
        var timestamp = this.timestamp
        return GyroscopeData(valueX, valueY, valueZ, accuracy,timestamp)
    }
}