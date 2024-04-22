package com.example.sa

data class User (
    var first : String = "",
    var last : String = "",
    var born : Int = 0
) {

    private fun copy(): User {
                val first = this.first
                val last = this.last
                val born = this.born
                return User(first, last, born)
    }
}