package com.example.sa

data class AccelerometerData (
    var accelerometerX: Float = 0.0f,
    var accelerometerY: Float = 0.0f,
    var accelerometerZ: Float = 0.0f,
    var accuracy: Int = 0,
    var timestamp: Long = 0
) {

    private fun copy(): AccelerometerData {
        var accelerometerX = this.accelerometerX
        var accelerometerY = this.accelerometerY
        var accelerometerZ = this.accelerometerZ
        var accuracy = this.accuracy
        var timestamp = this.timestamp
        return AccelerometerData(accelerometerX, accelerometerY, accelerometerZ, accuracy, timestamp)
    }
}