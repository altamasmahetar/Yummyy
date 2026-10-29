package com.example.pr2

import android.content.Context

data class CartItem(
    val name: String,
    val price: Double,
    var quantity: Int = 1
)

object CartManager {
    val items = mutableListOf<CartItem>()

    fun loadCart(context: Context) {
        items.clear()
        val dbHelper = DatabaseHelper(context)
        items.addAll(dbHelper.getCartItems())
    }

    fun addItem(context: Context, name: String, price: Double) {
        val dbHelper = DatabaseHelper(context)
        dbHelper.addToCart(name, price)
        loadCart(context)
    }

    fun updateQuantity(context: Context, name: String, delta: Int) {
        val dbHelper = DatabaseHelper(context)
        dbHelper.updateCartQuantity(name, delta)
        loadCart(context)
    }

    fun removeItem(context: Context, name: String) {
        val dbHelper = DatabaseHelper(context)
        dbHelper.removeFromCart(name)
        loadCart(context)
    }

    fun getTotalPrice(): Double {
        return items.sumOf { it.price * it.quantity }
    }

    fun clear(context: Context) {
        val dbHelper = DatabaseHelper(context)
        dbHelper.clearCart()
        items.clear()
    }
}