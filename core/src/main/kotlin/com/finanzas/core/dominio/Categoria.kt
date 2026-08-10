package com.finanzas.core.dominio

class Categoria(
    val id: String,
    val nombre: String
) {
    init {
        require(id.isNotBlank()) { "El id de la categoria no puede estar vacio" }
        require(nombre.isNotBlank()) { "El nombre de la categoria no puede estar vacio" }
    }

    override fun equals(otra: Any?): Boolean {
        return otra is Categoria && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Categoria(id=$id, nombre=$nombre)"
    }
}
