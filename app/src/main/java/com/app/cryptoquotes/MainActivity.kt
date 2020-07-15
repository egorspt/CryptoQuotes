package com.app.cryptoquotes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback
import com.koushikdutta.async.http.WebSocket
import com.neovisionaries.ws.client.WebSocketAdapter
import com.neovisionaries.ws.client.WebSocketFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener


class MainActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var quotes = arrayListOf<Quotation>()
    private var adapter: QuotesAdapter = QuotesAdapter(quotes)
    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (currency: String in Constants().CURRENCIES_USD.split(",")) {
            quotes.add(Quotation(currency.replace("[\" ]".toRegex(), ""), 0.0, 0.0, 0.0, 0.0))
        }

        rv = findViewById(R.id.rv)
        rv?.layoutManager = LinearLayoutManager(this)
        rv?.adapter = adapter
        nvWebsocketClient()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            R.id.usd -> {
                true
            }
            R.id.eur -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun start() {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url("wss://ws-feed.gdax.com").build()
        val listener: EchoWebSocketListener = EchoWebSocketListener()
        val ws = client.newWebSocket(request, listener)
        client.dispatcher().executorService().shutdown()
    }

    private fun output(pojo: PojoQuatation) {
        if (pojo.side == null) {
            return
        }
        val quo = quotes.find { it.cryptoName == pojo.product_id }

        if (pojo.side.equals("buy")) {
            var buyPrice = quo?.buyPrice
            if (buyPrice != null && buyPrice > 0)
                quo?.buyPercent = (pojo.price - buyPrice) / buyPrice * 100
            quo?.buyPrice = pojo.price
        } else {
            var sellPrice = quo?.sellPrice
            if (sellPrice != null && sellPrice > 0)
                quo?.sellPercent = (pojo.price - sellPrice) / sellPrice * 100
            quo?.sellPrice = pojo.price
        }

        val index = quotes.indexOf(quo)
        var q = quotes.find { it.cryptoName.equals(pojo.product_id) }
        if (q != null)

            this@MainActivity.runOnUiThread(java.lang.Runnable {
                adapter.notifyDataSetChanged()
            })
    }

    private inner class EchoWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
            webSocket.send(
                "{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}"
            )
            //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }

        override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
            output(Gson().fromJson(text, PojoQuatation::class.java))
        }

        override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
            webSocket.close(Constants().NORMAL_CLOSURE_STATUS, null)
            //output("Closing : $code / $reason")
        }

        override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response) {
            //output("Error : " + t.message)
        }
    }

    private fun AndroidAsync() {
        AsyncHttpClient.getDefaultInstance()
            .websocket("wss://ws-feed.gdax.com", "wss", object : WebSocketConnectCallback {
                override fun onCompleted(ex: Exception?, webSocket: WebSocket) {
                    if (ex != null) {
                        ex.printStackTrace()
                        return
                    }
                    webSocket.send("{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}")
                    webSocket.setStringCallback(object :
                        com.koushikdutta.async.http.WebSocket.StringCallback {
                        override fun onStringAvailable(s: String) {
                            output(Gson().fromJson(s, PojoQuatation::class.java))
                        }
                    })
                }
            })
    }

    private fun nvWebsocketClient() {
        val ws: com.neovisionaries.ws.client.WebSocket = WebSocketFactory().createSocket("wss://ws-feed.gdax.com")
        ws.addListener(object: WebSocketAdapter() {
            override fun onTextMessage(webSocket: com.neovisionaries.ws.client.WebSocket, message: String){
                var m = message

            }
        });
        ws.sendText("{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}")

    }



}