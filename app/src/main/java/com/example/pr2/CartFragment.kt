package com.example.pr2

import android.app.AlertDialog
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CartFragment : Fragment() {

    private lateinit var emptyCartLayout: LinearLayout
    private lateinit var cartContentLayout: LinearLayout
    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var tvTotalPrice: TextView
    private lateinit var btnCheckout: Button
    private lateinit var btnAddOrderContentProvider: Button
    private lateinit var lvOrders: ListView

    private val orderDisplayList = mutableListOf<String>()
    private val orderIdList = mutableListOf<Long>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_cart,
            container,
            false
        )

        emptyCartLayout = view.findViewById(R.id.emptyCartLayout)
        cartContentLayout = view.findViewById(R.id.cartContentLayout)
        cartItemsContainer = view.findViewById(R.id.cartItemsContainer)
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice)
        btnCheckout = view.findViewById(R.id.btnCheckout)
        btnAddOrderContentProvider = view.findViewById(R.id.btnAddOrderContentProvider)
        lvOrders = view.findViewById(R.id.lvOrders)

        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, orderDisplayList)
        lvOrders.adapter = adapter

        btnCheckout.setOnClickListener {
            val total = CartManager.getTotalPrice()
            if (total <= 0) return@setOnClickListener

            AlertDialog.Builder(requireContext())
                .setTitle("Checkout Order")
                .setMessage("Your total amount is ₹${total.toInt()}.\nDo you want to place the order?")
                .setPositiveButton("Place Order") { _, _ ->
                    // Insert order into ContentProvider (PRACTICAL 6A PART B)
                    for (item in CartManager.items) {
                        insertOrderContentProvider(item.name, item.quantity, item.price * item.quantity)
                    }

                    CartManager.clear(requireContext())
                    refreshCart()
                    loadContentProviderOrders()
                    Toast.makeText(requireContext(), "Order Placed & Saved via ContentProvider!", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // CRUD: Insert via Content Provider Button
        btnAddOrderContentProvider.setOnClickListener {
            showAddOrderDialog()
        }

        // CRUD: Update/Delete via Content Provider ListView Item Click
        lvOrders.setOnItemClickListener { _, _, position, _ ->
            if (position in orderIdList.indices) {
                val id = orderIdList[position]
                val orderText = orderDisplayList[position]
                showOrderOptionsDialog(id, orderText)
            }
        }

        refreshCart()
        loadContentProviderOrders()

        return view
    }

    override fun onResume() {
        super.onResume()
        refreshCart()
        loadContentProviderOrders()
    }

    private fun refreshCart() {
        if (!isAdded) return
        CartManager.loadCart(requireContext())

        if (CartManager.items.isEmpty()) {
            emptyCartLayout.visibility = View.VISIBLE
            cartContentLayout.visibility = View.GONE
        } else {
            emptyCartLayout.visibility = View.GONE
            cartContentLayout.visibility = View.VISIBLE

            cartItemsContainer.removeAllViews()

            for (item in CartManager.items) {
                val card = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_cart, cartItemsContainer, false)

                val tvName = card.findViewById<TextView>(R.id.tvCartFoodName)
                val tvPrice = card.findViewById<TextView>(R.id.tvCartFoodPrice)
                val tvQuantity = card.findViewById<TextView>(R.id.tvCartQuantity)
                val btnMinus = card.findViewById<Button>(R.id.btnMinus)
                val btnPlus = card.findViewById<Button>(R.id.btnPlus)
                val btnRemove = card.findViewById<Button>(R.id.btnRemove)

                tvName.text = item.name
                tvPrice.text = "₹${(item.price * item.quantity).toInt()}"
                tvQuantity.text = item.quantity.toString()

                btnMinus.setOnClickListener {
                    CartManager.updateQuantity(requireContext(), item.name, -1)
                    refreshCart()
                }

                btnPlus.setOnClickListener {
                    CartManager.updateQuantity(requireContext(), item.name, 1)
                    refreshCart()
                }

                btnRemove.setOnClickListener {
                    CartManager.removeItem(requireContext(), item.name)
                    refreshCart()
                }

                cartItemsContainer.addView(card)
            }

            val total = CartManager.getTotalPrice()
            tvTotalPrice.text = "₹${total.toInt()}"
        }
    }

    // --- CONTENT PROVIDER CRUD OPERATIONS (PRACTICAL 6A PART B) ---

    // 1. CREATE (INSERT)
    private fun insertOrderContentProvider(itemName: String, qty: Int, total: Double) {
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val cv = ContentValues().apply {
            put(DatabaseHelper.COL_ORDER_ITEM, itemName)
            put(DatabaseHelper.COL_ORDER_QTY, qty)
            put(DatabaseHelper.COL_ORDER_TOTAL, total)
            put(DatabaseHelper.COL_ORDER_DATE, dateStr)
        }
        requireContext().contentResolver.insert(OrderContentProvider.CONTENT_URI, cv)
    }

    // 2. READ (QUERY & DISPLAY IN LISTVIEW)
    private fun loadContentProviderOrders() {
        if (!isAdded) return
        orderDisplayList.clear()
        orderIdList.clear()

        val cursor: Cursor? = requireContext().contentResolver.query(
            OrderContentProvider.CONTENT_URI,
            null, null, null,
            "${DatabaseHelper.COL_ORDER_ID} DESC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ID)
            val itemIndex = it.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_ITEM)
            val qtyIndex = it.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_QTY)
            val totalIndex = it.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_TOTAL)
            val dateIndex = it.getColumnIndexOrThrow(DatabaseHelper.COL_ORDER_DATE)

            while (it.moveToNext()) {
                val id = it.getLong(idIndex)
                val item = it.getString(itemIndex)
                val qty = it.getInt(qtyIndex)
                val total = it.getDouble(totalIndex)
                val date = it.getString(dateIndex)

                orderIdList.add(id)
                orderDisplayList.add("Order #$id: $item (Qty: $qty) - ₹${total.toInt()} [$date]")
            }
        }
        adapter.notifyDataSetChanged()
    }

    // 3. UPDATE / DELETE DIALOGS
    private fun showOrderOptionsDialog(orderId: Long, orderText: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Order Options (ContentProvider)")
            .setMessage(orderText)
            .setPositiveButton("Update Quantity") { _, _ ->
                showUpdateDialog(orderId)
            }
            .setNegativeButton("Delete Order") { _, _ ->
                // DELETE
                val itemUri = Uri.withAppendedPath(OrderContentProvider.CONTENT_URI, orderId.toString())
                requireContext().contentResolver.delete(itemUri, null, null)
                loadContentProviderOrders()
                Toast.makeText(requireContext(), "Order deleted via ContentProvider", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showUpdateDialog(orderId: Long) {
        val input = EditText(requireContext())
        input.hint = "Enter new Quantity"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        AlertDialog.Builder(requireContext())
            .setTitle("Update Order Quantity")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newQty = input.text.toString().toIntOrNull()
                if (newQty != null && newQty > 0) {
                    val cv = ContentValues().apply {
                        put(DatabaseHelper.COL_ORDER_QTY, newQty)
                    }
                    val itemUri = Uri.withAppendedPath(OrderContentProvider.CONTENT_URI, orderId.toString())
                    requireContext().contentResolver.update(itemUri, cv, null, null)
                    loadContentProviderOrders()
                    Toast.makeText(requireContext(), "Order updated via ContentProvider", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddOrderDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        val etItem = EditText(requireContext()).apply { hint = "Food Name" }
        val etQty = EditText(requireContext()).apply { hint = "Quantity"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        val etPrice = EditText(requireContext()).apply { hint = "Price per item (₹)"; inputType = android.text.InputType.TYPE_CLASS_NUMBER }

        layout.addView(etItem)
        layout.addView(etQty)
        layout.addView(etPrice)

        AlertDialog.Builder(requireContext())
            .setTitle("Add Order (ContentProvider)")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val item = etItem.text.toString().trim()
                val qty = etQty.text.toString().toIntOrNull() ?: 1
                val price = etPrice.text.toString().toDoubleOrNull() ?: 100.0

                if (item.isNotEmpty()) {
                    insertOrderContentProvider(item, qty, qty * price)
                    loadContentProviderOrders()
                    Toast.makeText(requireContext(), "Order added via ContentProvider", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}