package com.example.lab3

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment() {

    private lateinit var listener: OnOrderListener
    private lateinit var textViewResult: TextView
    private lateinit var buttonCancel: Button

    companion object {
        private const val ARG_ORDER = "order"

        fun newInstance(order: PizzaOrder): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putSerializable(ARG_ORDER, order)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        textViewResult = view.findViewById(R.id.textViewResult)
        buttonCancel = view.findViewById(R.id.buttonCancel)

        val order = arguments?.getSerializable(ARG_ORDER) as? PizzaOrder

        if (order != null) {
            displayOrder(order)
        }

        buttonCancel.setOnClickListener {
            listener.onCancelOrder()
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnOrderListener) {
            listener = context
        } else {
            throw RuntimeException("$context повинен реалізувати OnOrderListener")
        }
    }

    private fun displayOrder(order: PizzaOrder) {
        val dateFormat = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm:ss", java.util.Locale.getDefault())
        val dateTime = dateFormat.format(java.util.Date(order.timestamp))

        val orderText = buildString {
            append("Ім'я: ${order.name}\n")
            append("Тип піци: ${order.pizzaType}\n")
            append("Розмір: ${order.size}\n")

            if (order.toppings.isNotEmpty()) {
                append("Додаткові інгредієнти: ${order.toppings.joinToString(", ")}\n")
            } else {
                append("Без додаткових інгредієнтів\n")
            }

            append("\nДата та час: $dateTime")
        }

        textViewResult.text = orderText
    }
}