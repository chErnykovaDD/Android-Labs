package com.example.lab2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

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
}

interface OnOrderListener {
    fun onOrderPlaced(order: PizzaOrder)
    fun onCancelOrder()
}

data class PizzaOrder(
    val name: String,
    val pizzaType: String,
    val size: String,
    val toppings: List<String>
) : java.io.Serializable