package com.example.lab2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class OrderFragment : Fragment() {

    private lateinit var listener: OnOrderListener

    private lateinit var rootView: View
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_order, container, false)

        initViews()

        setupListeners()

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnOrderListener) {
            listener = context
        } else {
            throw RuntimeException("$context повинен реалізувати OnOrderListener")
        }
    }

    private fun initViews() {
        editTextName = rootView.findViewById(R.id.editTextName)
        radioPepperoni = rootView.findViewById(R.id.radioPepperoni)
        radioMargherita = rootView.findViewById(R.id.radioMargherita)
        radioFourCheese = rootView.findViewById(R.id.radioFourCheese)
        radioSmall = rootView.findViewById(R.id.radioSmall)
        radioMedium = rootView.findViewById(R.id.radioMedium)
        radioLarge = rootView.findViewById(R.id.radioLarge)
        checkCheese = rootView.findViewById(R.id.checkCheese)
        checkMushrooms = rootView.findViewById(R.id.checkMushrooms)
        checkOlives = rootView.findViewById(R.id.checkOlives)
        btnOrder = rootView.findViewById(R.id.btnOrder)
    }

    private fun setupListeners() {
        rootView.setOnClickListener {
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

        val order = PizzaOrder(
            name = customerName,
            pizzaType = selectedPizzaType,
            size = selectedSize,
            toppings = toppings
        )

        listener.onOrderPlaced(order)
    }

    fun clearForm() {
        editTextName.setText("")
        radioPepperoni.isChecked = false
        radioMargherita.isChecked = false
        radioFourCheese.isChecked = false
        radioSmall.isChecked = false
        radioMedium.isChecked = false
        radioLarge.isChecked = false
        checkCheese.isChecked = false
        checkMushrooms.isChecked = false
        checkOlives.isChecked = false
    }

    private fun hideKeyboard() {
        val view = activity?.currentFocus ?: View(activity)

        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)

        rootView.requestFocus()
    }

    private fun showAlert(message: String, title: String = "Увага") {
        context?.let {
            AlertDialog.Builder(it)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show()
        }
    }
}