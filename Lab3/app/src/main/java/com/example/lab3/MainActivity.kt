package com.example.lab3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.*

class MainActivity : AppCompatActivity(), OnOrderListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainer, OrderFragment())
                .commit()
        }
    }

    override fun onOrderPlaced(order: PizzaOrder) {
        saveOrderToFile(order)

        val resultFragment = ResultFragment.newInstance(order)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, resultFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onCancelOrder() {
        supportFragmentManager.popBackStack()

        Handler(Looper.getMainLooper()).postDelayed({
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            if (fragment is OrderFragment) {
                fragment.clearForm()
            }
        }, 100)
    }

    override fun onOpenOrderHistory() {
        val intent = Intent(this, OrderHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun saveOrderToFile(order: PizzaOrder) {
        try {
            val file = File(filesDir, ORDERS_FILENAME)

            if (!file.exists()) {
                file.createNewFile()
            }
            val orders = readOrdersFromFile(this).toMutableList()

            orders.add(order)

            ObjectOutputStream(FileOutputStream(file)).use { outputStream ->
                outputStream.writeObject(orders)
            }

            Toast.makeText(this, "Замовлення успішно збережено", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Помилка при збереженні замовлення: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    companion object {
        const val ORDERS_FILENAME = "pizza_orders.dat"

        fun readOrdersFromFile(context: Context): List<PizzaOrder> {
            try {
                val file = File(context.filesDir, ORDERS_FILENAME)

                if (!file.exists() || file.length() == 0L) {
                    return emptyList()
                }

                ObjectInputStream(FileInputStream(file)).use { inputStream ->
                    @Suppress("UNCHECKED_CAST")
                    return inputStream.readObject() as List<PizzaOrder>
                }

            } catch (e: Exception) {
                e.printStackTrace()
                return emptyList()
            }
        }
    }
}

interface OnOrderListener {
    fun onOrderPlaced(order: PizzaOrder)
    fun onCancelOrder()
    fun onOpenOrderHistory()
}

data class PizzaOrder(
    val name: String,
    val pizzaType: String,
    val size: String,
    val toppings: List<String>,
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable