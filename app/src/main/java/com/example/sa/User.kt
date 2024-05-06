package com.example.sa

data class User (
    var nome : String = "",
    var apelido : String = "",
    var idade : String = "",
    var peso : String = ""
) {

    private fun copy(): User {
        val nome = this.nome
        val apelido = this.apelido
        val idade = this.idade
        val peso = this.peso
        return User(nome, apelido, idade,peso)
    }
}