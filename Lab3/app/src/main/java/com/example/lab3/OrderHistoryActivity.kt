package com.example.lab3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var btnClearHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_history)

        recyclerView = findViewById(R.id.recyclerViewOrders)
        emptyView = findViewById(R.id.textViewEmpty)
        btnClearHistory = findViewById(R.id.btnClearHistory)

        recyclerView.layoutManager = LinearLayoutManager(this)

        btnClearHistory.setOnClickListener {
            confirmClearHistory()
        }

        loadOrders()
    }

    override fun onResume() {
        super.onResume()
        loadOrders()
    }

    private fun loadOrders() {
        val orders = MainActivity.readOrdersFromFile(this)

        if (orders.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            btnClearHistory.visibility = View.GONE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            btnClearHistory.visibility = View.VISIBLE

            recyclerView.adapter = OrderAdapter(orders.sortedByDescending { it.timestamp })
        }
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(this)
            .setTitle("Очистити історію")
            .setMessage("Ви впевнені, що хочете видалити всі замовлення?")
            .setPositiveButton("Так") { _, _ ->
                clearOrderHistory()
            }
            .setNegativeButton("Ні", null)
            .show()
    }

    private fun clearOrderHistory() {
        try {
            openFileOutput(MainActivity.ORDERS_FILENAME, MODE_PRIVATE).close()

            loadOrders()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class OrderAdapter(private val orders: List<PizzaOrder>) :
        RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order, parent, false)
            return OrderViewHolder(view)
        }

        override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
            val order = orders[position]
            holder.bind(order)
        }

        override fun getItemCount(): Int = orders.size

        inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val tvOrderInfo: TextView = itemView.findViewById(R.id.textViewOrderInfo)
            private val tvOrderDate: TextView = itemView.findViewById(R.id.textViewOrderDate)
            private val btnDelete: Button = itemView.findViewById(R.id.btnDeleteOrder)

            fun bind(order: PizzaOrder) {
                val orderInfo = buildString {
                    append("Ім'я: ${order.name}\n")
                    append("Тип піци: ${order.pizzaType}\n")
                    append("Розмір: ${order.size}")

                    if (order.toppings.isNotEmpty()) {
                        append("\nДодатки: ${order.toppings.joinToString(", ")}")
                    }
                }

                val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                val dateString = dateFormat.format(Date(order.timestamp))

                tvOrderInfo.text = orderInfo
                tvOrderDate.text = dateString

                btnDelete.setOnClickListener {
                    deleteOrder(order)
                }
            }
        }

        private fun deleteOrder(orderToDelete: PizzaOrder) {
            AlertDialog.Builder(this@OrderHistoryActivity)
                .setTitle("Видалення замовлення")
                .setMessage("Ви впевнені, що хочете видалити це замовлення?")
                .setPositiveButton("Так") { _, _ ->
                    try {
                        val allOrders = MainActivity.readOrdersFromFile(this@OrderHistoryActivity).toMutableList()
                        allOrders.removeIf { it.timestamp == orderToDelete.timestamp }

                        val fileOutputStream = openFileOutput(MainActivity.ORDERS_FILENAME, MODE_PRIVATE)
                        val objectOutputStream = java.io.ObjectOutputStream(fileOutputStream)
                        objectOutputStream.writeObject(allOrders)
                        objectOutputStream.close()

                        loadOrders()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .setNegativeButton("Ні", null)
                .show()
        }
    }
}