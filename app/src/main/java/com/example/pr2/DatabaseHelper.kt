package com.example.pr2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "YummCart.db"
        private const val DATABASE_VERSION = 1

        // USERS TABLE
        const val TABLE_USERS = "users"
        const val COL_USER_ID = "id"
        const val COL_USER_NAME = "name"
        const val COL_USER_EMAIL = "email"
        const val COL_USER_PHONE = "phone"
        const val COL_USER_PASSWORD = "password"
        const val COL_USER_ADDRESS = "address"
        const val COL_USER_DOB = "dob"
        const val COL_USER_IMAGE = "image"

        // FOODS TABLE
        const val TABLE_FOODS = "foods"
        const val COL_FOOD_ID = "id"
        const val COL_FOOD_NAME = "name"
        const val COL_FOOD_PRICE = "price"
        const val COL_FOOD_CATEGORY = "category"
        const val COL_FOOD_EMOJI = "emoji"

        // CART TABLE
        const val TABLE_CART = "cart"
        const val COL_CART_ID = "id"
        const val COL_CART_NAME = "name"
        const val COL_CART_PRICE = "price"
        const val COL_CART_QUANTITY = "quantity"

        // ORDERS TABLE (for ContentProvider)
        const val TABLE_ORDERS = "orders"
        const val COL_ORDER_ID = "_id"
        const val COL_ORDER_ITEM = "item_name"
        const val COL_ORDER_QTY = "quantity"
        const val COL_ORDER_TOTAL = "total_price"
        const val COL_ORDER_DATE = "order_date"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = ("CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_NAME + " TEXT, "
                + COL_USER_EMAIL + " TEXT UNIQUE, "
                + COL_USER_PHONE + " TEXT, "
                + COL_USER_PASSWORD + " TEXT, "
                + COL_USER_ADDRESS + " TEXT, "
                + COL_USER_DOB + " TEXT, "
                + COL_USER_IMAGE + " TEXT)")

        val createFoodsTable = ("CREATE TABLE " + TABLE_FOODS + " ("
                + COL_FOOD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FOOD_NAME + " TEXT, "
                + COL_FOOD_PRICE + " REAL, "
                + COL_FOOD_CATEGORY + " TEXT, "
                + COL_FOOD_EMOJI + " TEXT)")

        val createCartTable = ("CREATE TABLE " + TABLE_CART + " ("
                + COL_CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CART_NAME + " TEXT UNIQUE, "
                + COL_CART_PRICE + " REAL, "
                + COL_CART_QUANTITY + " INTEGER)")

        val createOrdersTable = ("CREATE TABLE " + TABLE_ORDERS + " ("
                + COL_ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_ORDER_ITEM + " TEXT, "
                + COL_ORDER_QTY + " INTEGER, "
                + COL_ORDER_TOTAL + " REAL, "
                + COL_ORDER_DATE + " TEXT)")

        db.execSQL(createUsersTable)
        db.execSQL(createFoodsTable)
        db.execSQL(createCartTable)
        db.execSQL(createOrdersTable)

        insertDefaultFoods(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOODS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CART")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ORDERS")
        onCreate(db)
    }

    private fun insertDefaultFoods(db: SQLiteDatabase) {
        val foods = listOf(
            arrayOf("Margherita Pizza", "180.0", "Pizza", "🍕"),
            arrayOf("Cheese Burst Pizza", "250.0", "Pizza", "🍕"),
            arrayOf("Farmhouse Pizza", "220.0", "Pizza", "🍕"),
            arrayOf("Veg Burger", "120.0", "Burgers", "🍔"),
            arrayOf("Cheese Burger", "140.0", "Burgers", "🍔"),
            arrayOf("Crispy Paneer Burger", "160.0", "Burgers", "🍔"),
            arrayOf("French Fries", "90.0", "Burgers", "🍟"),
            arrayOf("Paneer Wrap", "150.0", "Burgers", "🌯"),
            arrayOf("Hakka Noodles", "130.0", "Chinese", "🍜"),
            arrayOf("Veg Manchurian", "140.0", "Chinese", "🥟"),
            arrayOf("Spring Rolls", "110.0", "Chinese", "🥢"),
            arrayOf("Chocolate Cake", "150.0", "Desserts", "🍰"),
            arrayOf("Ice Cream Sundae", "120.0", "Desserts", "🍨"),
            arrayOf("Gulab Jamun", "90.0", "Desserts", "🍩"),
            arrayOf("Cold Coffee", "90.0", "Beverages", "🥤"),
            arrayOf("Fresh Lime Soda", "60.0", "Beverages", "🍹"),
            arrayOf("Mango Smoothie", "110.0", "Beverages", "🧃")
        )

        for (f in foods) {
            val cv = ContentValues()
            cv.put(COL_FOOD_NAME, f[0])
            cv.put(COL_FOOD_PRICE, f[1].toDouble())
            cv.put(COL_FOOD_CATEGORY, f[2])
            cv.put(COL_FOOD_EMOJI, f[3])
            db.insert(TABLE_FOODS, null, cv)
        }
    }

    // --- USER CRUD ---
    fun registerUser(name: String, email: String, phone: String, pass: String): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_USER_NAME, name)
        cv.put(COL_USER_EMAIL, email)
        cv.put(COL_USER_PHONE, phone)
        cv.put(COL_USER_PASSWORD, pass)
        val result = db.insert(TABLE_USERS, null, cv)
        return result != -1L
    }

    fun checkUserLogin(email: String, pass: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL = ? AND $COL_USER_PASSWORD = ?",
            arrayOf(email, pass)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserProfile(email: String): ContentValues? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_USER_EMAIL = ?",
            arrayOf(email)
        )
        var cv: ContentValues? = null
        if (cursor.moveToFirst()) {
            cv = ContentValues()
            cv.put(COL_USER_NAME, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_NAME)))
            cv.put(COL_USER_EMAIL, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)))
            cv.put(COL_USER_PHONE, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PHONE)))
            cv.put(COL_USER_ADDRESS, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ADDRESS)))
            cv.put(COL_USER_DOB, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_DOB)))
            cv.put(COL_USER_IMAGE, cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_IMAGE)))
        }
        cursor.close()
        return cv
    }

    fun updateUserProfile(email: String, name: String, address: String, phone: String, dob: String, imageBase64: String?): Boolean {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put(COL_USER_NAME, name)
        cv.put(COL_USER_ADDRESS, address)
        cv.put(COL_USER_PHONE, phone)
        cv.put(COL_USER_DOB, dob)
        if (imageBase64 != null) {
            cv.put(COL_USER_IMAGE, imageBase64)
        }
        val result = db.update(TABLE_USERS, cv, "$COL_USER_EMAIL = ?", arrayOf(email))
        return result > 0
    }

    // --- FOOD DATA FETCHING ---
    fun getFoodsByCategory(category: String): List<Array<String>> {
        val list = mutableListOf<Array<String>>()
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_FOOD_NAME, $COL_FOOD_PRICE, $COL_FOOD_EMOJI FROM $TABLE_FOODS WHERE $COL_FOOD_CATEGORY = ?",
            arrayOf(category)
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val price = cursor.getDouble(1).toString()
            val emoji = cursor.getString(2)
            list.add(arrayOf(name, price, emoji))
        }
        cursor.close()
        return list
    }

    fun getAllFoods(): List<Array<String>> {
        val list = mutableListOf<Array<String>>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_FOOD_NAME, $COL_FOOD_PRICE, $COL_FOOD_EMOJI FROM $TABLE_FOODS", null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val price = cursor.getDouble(1).toString()
            val emoji = cursor.getString(2)
            list.add(arrayOf(name, price, emoji))
        }
        cursor.close()
        return list
    }

    // --- CART CRUD ---
    fun addToCart(name: String, price: Double) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT $COL_CART_QUANTITY FROM $TABLE_CART WHERE $COL_CART_NAME = ?", arrayOf(name))
        if (cursor.moveToFirst()) {
            val currentQty = cursor.getInt(0)
            val cv = ContentValues()
            cv.put(COL_CART_QUANTITY, currentQty + 1)
            db.update(TABLE_CART, cv, "$COL_CART_NAME = ?", arrayOf(name))
        } else {
            val cv = ContentValues()
            cv.put(COL_CART_NAME, name)
            cv.put(COL_CART_PRICE, price)
            cv.put(COL_CART_QUANTITY, 1)
            db.insert(TABLE_CART, null, cv)
        }
        cursor.close()
    }

    fun updateCartQuantity(name: String, delta: Int) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT $COL_CART_QUANTITY FROM $TABLE_CART WHERE $COL_CART_NAME = ?", arrayOf(name))
        if (cursor.moveToFirst()) {
            val newQty = cursor.getInt(0) + delta
            if (newQty <= 0) {
                db.delete(TABLE_CART, "$COL_CART_NAME = ?", arrayOf(name))
            } else {
                val cv = ContentValues()
                cv.put(COL_CART_QUANTITY, newQty)
                db.update(TABLE_CART, cv, "$COL_CART_NAME = ?", arrayOf(name))
            }
        }
        cursor.close()
    }

    fun removeFromCart(name: String) {
        val db = writableDatabase
        db.delete(TABLE_CART, "$COL_CART_NAME = ?", arrayOf(name))
    }

    fun getCartItems(): List<CartItem> {
        val list = mutableListOf<CartItem>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT $COL_CART_NAME, $COL_CART_PRICE, $COL_CART_QUANTITY FROM $TABLE_CART", null)
        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            val price = cursor.getDouble(1)
            val qty = cursor.getInt(2)
            list.add(CartItem(name, price, qty))
        }
        cursor.close()
        return list
    }

    fun clearCart() {
        val db = writableDatabase
        db.delete(TABLE_CART, null, null)
    }
}