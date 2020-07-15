package com.app.cryptoquotes

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PojoQuatation(
    val type: String,
    val sequence: String,
    val product_id: String,
    val price: Double,
    val open_24h: String,
    val volume_24h: String,
    val low_24h: String,
    val high_24h: String,
    val volume_30d: String,
    val best_bid: String,
    val best_ask: String,
    val side: String,
    val time: String,
    val trade_id: String,
    val last_size: String
)