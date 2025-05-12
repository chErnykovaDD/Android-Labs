package com.example.lab1

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: View
    private lateinit var editTextName: TextInputEditText
    private lateinit var radioPepperoni: RadioButton
    private lateinit var radioMargherita: RadioButton
    private lateinit var radioFourCheese: RadioButton
    private lateinit var radioSmall: RadioButton
    private lateinit var radioMedium: RadioButton
    private lateinit var radioLarge: RadioButton
    private lateinit var checkCheese: CheckBox
    private lateinit var checkMushrooms: CheckBox
    private lateinit var checkOlives: CheckBox
    private lateinit var btnOrder: MaterialButton
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()

        setupListeners()
    }

    private fun initializeViews() {
        rootLayout = findViewById(R.id.rootLayout)
        editTextName = findViewById(R.id.editTextName)
        radioPepperoni = findViewById(R.id.radioPepperoni)
        radioMargherita = findViewById(R.id.radioMargherita)
        radioFourCheese = findViewById(R.id.radioFourCheese)
        radioSmall = findViewById(R.id.radioSmall)
        radioMedium = findViewById(R.id.radioMedium)
        radioLarge = findViewById(R.id.radioLarge)
        checkCheese = findViewById(R.id.checkCheese)
        checkMushrooms = findViewById(R.id.checkMushrooms)
        checkOlives = findViewById(R.id.checkOlives)
        btnOrder = findViewById(R.id.btnOrder)
        textViewResult = findViewById(R.id.textViewResult)
    }

    private fun setupListeners() {
        rootLayout.setOnClickListener {
            hideKeyboard()
        }

        btnOrder.setOnClickListener {
            processOrder()
        }
    }

    private fun processOrder() {
        val customerName = editTextName.text.toString().trim()

        if (customerName.isEmpty()) {
            showAlert("Будь ласка, введіть ваше ім'я")
            return
        }

        val selectedPizzaType = when {
            radioPepperoni.isChecked -> radioPepperoni.text.toString()
            radioMargherita.isChecked -> radioMargherita.text.toString()
            radioFourCheese.isChecked -> radioFourCheese.text.toString()
            else -> ""
        }

        if (selectedPizzaType.isEmpty()) {
            showAlert("Будь ласка, оберіть тип піци")
            return
        }

        val selectedSize = when {
            radioSmall.isChecked -> radioSmall.text.toString()
            radioMedium.isChecked -> radioMedium.text.toString()
            radioLarge.isChecked -> radioLarge.text.toString()
            else -> ""
        }

        if (selectedSize.isEmpty()) {
            showAlert("Будь ласка, оберіть розмір піци")
            return
        }

        val toppings = mutableListOf<String>()

        if (checkCheese.isChecked) toppings.add(checkCheese.text.toString())
        if (checkMushrooms.isChecked) toppings.add(checkMushrooms.text.toString())
        if (checkOlives.isChecked) toppings.add(checkOlives.text.toString())

        val orderSummary = buildString {
            append("Ім'я: $customerName\n")
            append("Тип піци: $selectedPizzaType\n")
            append("Розмір: $selectedSize\n")

            if (toppings.isNotEmpty()) {
                append("Додаткові інгредієнти: ${toppings.joinToString(", ")}")
            } else {
                append("Без додаткових інгредієнтів")
            }
        }

        textViewResult.text = orderSummary

        hideKeyboard()

        showAlert("Очікуйте на доставку!", "Замовлення прийнято")
    }

    private fun hideKeyboard() {
        val view = currentFocus ?: View(this)

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        rootLayout.requestFocus()
    }

    private fun showAlert(message: String, title: String = "Увага") {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}