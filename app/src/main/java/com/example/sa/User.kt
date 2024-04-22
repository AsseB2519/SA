package com.example.sa

data class User (
    var first : String = "",
    var last : String = "",
    var born : Int = 0
)

{
    //private fun getFirst(): String { return this.first }

    //private fun getLast(): String { return this.last }

    //private fun getBorn(): Int { return this.born }

    //private fun setNewFirst(newFirst: String) { this.first = newFirst }

    //private fun setNewLast(newLast: String) { this.last = newLast }

    //private fun setNewBorn(newBorn: Int) { this.born = newBorn }

    private fun copy(): User {
                val first = this.first
                val last = this.last
                val born = this.born
                return User(first, last, born)
    }

    //fun copy(first: String = this.first, last: String = this.last, born: Int = this.born): User {
    //        return User(first, last, born)
    //}
}