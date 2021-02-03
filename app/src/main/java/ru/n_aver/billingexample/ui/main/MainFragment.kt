package ru.n_aver.billingexample.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.n_aver.billingexample.R
import ru.n_aver.billingexample.databinding.MainFragmentBinding

@AndroidEntryPoint
class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var binding: MainFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        binding?.purchasesList?.apply {
            layoutManager = LinearLayoutManager(context)
            adapter =
                PurchasesAdapter(PurchasesAdapter.BuyButtonListener { skuDetails ->
                    viewModel.startBillingFlow(requireActivity(), skuDetails)
                })
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.availablePurchases.collect { skuDetails ->
                (binding?.purchasesList?.adapter as PurchasesAdapter).data = skuDetails
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.purchases.collect { purchases ->
                when (purchases.size) {
                    1 -> {
                        Toast.makeText(context, "You purchase item", Toast.LENGTH_LONG).show()
                        binding?.lastPurchasedItem?.text = purchases.first().orderId
                        viewModel.consumePurchase(purchases)
                    }
                    0 -> {
                    }
                    else -> {
                        Toast.makeText(
                            context,
                            "You somehow purchase more than one item. Congratulations!",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.consumePurchase(purchases)
                    }
                }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}