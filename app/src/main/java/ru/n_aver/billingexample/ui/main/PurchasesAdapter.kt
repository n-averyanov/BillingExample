package ru.n_aver.billingexample.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.SkuDetails
import ru.n_aver.billingexample.databinding.PurchaseItemViewBinding

class PurchasesAdapter(
    private val clickListener: BuyButtonListener
) : RecyclerView.Adapter<PurchasesAdapter.ViewHolder>() {

    var data: List<SkuDetails> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, clickListener)
    }

    override fun getItemCount() = data.size

    class ViewHolder private constructor(val binding: PurchaseItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SkuDetails, clickListener: BuyButtonListener) {
            binding.itemTitle.text = item.title
            binding.price.text = item.price
            binding.description.text = item.description
            binding.buyButton.setOnClickListener {
                clickListener.onClick(item)
            }

        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PurchaseItemViewBinding.inflate(layoutInflater, parent, false)

                return ViewHolder(binding)
            }
        }
    }

    class BuyButtonListener(val clickListener: (skuDetails: SkuDetails) -> Unit) {

        fun onClick(skuDetails: SkuDetails) = clickListener(skuDetails)
    }
}