package com.example.pr2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val foodContainer = view.findViewById<LinearLayout>(R.id.foodContainer)

        CartManager.loadCart(requireContext())

        val dbHelper = DatabaseHelper(requireContext())
        val allFoods = dbHelper.getAllFoods()

        // Display first few popular items from SQLite DB
        for (i in 0 until minOf(6, allFoods.size)) {
            val food = allFoods[i]
            val foodName = food[0]
            val priceVal = food[1].toDoubleOrNull() ?: 0.0
            val priceStr = "₹${priceVal.toInt()}"
            val emoji = food[2]

            addFoodCard(foodContainer, foodName, priceStr, priceVal, emoji)
        }

        return view
    }

    private fun addFoodCard(
        container: LinearLayout,
        foodName: String,
        price: String,
        priceValue: Double,
        emoji: String
    ) {

        val card = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_food, container, false)

        val name = card.findViewById<TextView>(R.id.foodName)
        val foodPrice = card.findViewById<TextView>(R.id.foodPrice)
        val btnAdd = card.findViewById<Button>(R.id.btnAdd)

        name.text = "$emoji $foodName"
        foodPrice.text = price

        btnAdd?.setOnClickListener {
            CartManager.addItem(requireContext(), foodName, priceValue)
            Toast.makeText(requireContext(), "$foodName added to Cart!", Toast.LENGTH_SHORT).show()
        }

        container.addView(card)
    }
}