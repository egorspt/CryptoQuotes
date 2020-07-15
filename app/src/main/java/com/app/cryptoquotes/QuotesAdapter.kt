package com.app.cryptoquotes

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.DecimalFormat

class QuotesAdapter(var products: ArrayList<Quotation>) :
    RecyclerView.Adapter<QuotesAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MainHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.quotation_item,
            parent,
            false
        )
    )

    override fun getItemCount() = products.size

    override fun onBindViewHolder(holder: QuotesAdapter.MainHolder, position: Int) {
        holder.bind(products[position])
    }

    inner class MainHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var cryptoName = itemView.findViewById(R.id.crypto_name) as TextView
        private var sellPrice = itemView.findViewById(R.id.crypto_sell_price) as TextView
        private var sellPercent = itemView.findViewById(R.id.crypto_sell_percent) as TextView
        private var buyPrice = itemView.findViewById(R.id.crypto_buy_price) as TextView
        private var buyPercent = itemView.findViewById(R.id.crypto_buy_percent) as TextView

        fun bind(product: Quotation) {
            val df = DecimalFormat("#.###")

            cryptoName.text = product.cryptoName
            sellPrice.text = product.sellPrice.toString()
            sellPercent.text = df.format(product.sellPercent).replace(",", ".") + "%"
            buyPrice.text = product.buyPrice.toString()
            buyPercent.text = df.format(product.buyPercent).replace(",", ".") + "%"

            if (product.buyPercent > 0)
                buyPercent.setTextColor(Color.GREEN)
            else buyPercent.setTextColor(Color.RED)

            if (product.sellPercent > 0)
                sellPercent.setTextColor(Color.GREEN)
            else sellPercent.setTextColor(Color.RED)
        }
    }

    fun update(updatedItem: Quotation?) {
        if (updatedItem != null)
            products.add(0, updatedItem)
        notifyDataSetChanged()
    }

}