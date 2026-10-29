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

class CategoriesFragment : Fragment() {

    private lateinit var tvSelectedCategory: TextView
    private lateinit var categoryProductsContainer: LinearLayout
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        dbHelper = DatabaseHelper(requireContext())
        CartManager.loadCart(requireContext())

        tvSelectedCategory = view.findViewById(R.id.tvSelectedCategory)
        categoryProductsContainer = view.findViewById(R.id.categoryProductsContainer)

        val btnPizza = view.findViewById<Button>(R.id.btnCatPizza)
        val btnBurger = view.findViewById<Button>(R.id.btnCatBurger)
        val btnChinese = view.findViewById<Button>(R.id.btnCatChinese)
        val btnDesserts = view.findViewById<Button>(R.id.btnCatDesserts)
        val btnBeverages = view.findViewById<Button>(R.id.btnCatBeverages)

        btnPizza.setOnClickListener { loadCategoryFromDb("Pizza", "Pizza Items") }
        btnBurger.setOnClickListener { loadCategoryFromDb("Burgers", "Burger Items") }
        btnChinese.setOnClickListener { loadCategoryFromDb("Chinese", "Chinese Items") }
        btnDesserts.setOnClickListener { loadCategoryFromDb("Desserts", "Dessert Items") }
        btnBeverages.setOnClickListener { loadCategoryFromDb("Beverages", "Beverage Items") }

        // Default category
        loadCategoryFromDb("Pizza", "Pizza Items")

        return view
    }

    private fun loadCategoryFromDb(categoryDbName: String, displayTitle: String) {
        tvSelectedCategory.text = displayTitle
        categoryProductsContainer.removeAllViews()

        val products = dbHelper.getFoodsByCategory(categoryDbName)

        for (product in products) {
            val nameStr = product[0]
            val priceVal = product[1].toDoubleOrNull() ?: 0.0
            val priceStr = "₹${priceVal.toInt()}"
            val emojiStr = product[2]

            val card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_food, categoryProductsContainer, false)

            val nameView = card.findViewById<TextView>(R.id.foodName)
            val foodPriceView = card.findViewById<TextView>(R.id.foodPrice)
            val btnAdd = card.findViewById<Button>(R.id.btnAdd)

            nameView.text = "$emojiStr $nameStr"
            foodPriceView.text = priceStr

            btnAdd?.setOnClickListener {
                CartManager.addItem(requireContext(), nameStr, priceVal)
                Toast.makeText(requireContext(), "$nameStr added to Cart!", Toast.LENGTH_SHORT).show()
            }

            categoryProductsContainer.addView(card)
        }
    }
}