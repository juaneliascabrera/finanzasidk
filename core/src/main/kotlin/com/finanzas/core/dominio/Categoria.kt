package com.finanzas.core.dominio

class Categoria(
    val id: String,
    val nombre: String
) {
    override fun equals(otra: Any?): Boolean {
        return otra is Categoria && id == otra.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String {
        return "Categoria(id=$id, nombre=$nombre)"
    }
}
