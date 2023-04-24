package com.example.bledinamo.data

class MyBuffer<E> (private val maxSize : Int): ArrayList<E>() {

    /*
    Función que hace un override a la función add del ArrayList. Se asegura de que el
    ArrayList nunca supera el máximo valor de nuestro buffer, borrando el primer elemento.
    El resto de funciones de ArrayList está disponible.
    */
    override fun add(element : E) : Boolean{

        if(size >= maxSize)
            super.removeAt(0)
        return super.add(element)
    }
}