package com.app.cryptoquotes

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.koushikdutta.async.http.AsyncHttpClient
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback
import com.koushikdutta.async.http.WebSocket
import com.neovisionaries.ws.client.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private var rv: RecyclerView? = null
    private var quotes = arrayListOf<Quotation>()
    private var adapter: QuotesAdapter = QuotesAdapter(quotes)

    private lateinit var wbOkHttp: okhttp3.WebSocket
    private lateinit var wbAndroidAsync: WebSocket
    private lateinit var wbNV: com.neovisionaries.ws.client.WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        for (currency: String in Constants().CURRENCIES_USD.split(",")) {
            quotes.add(Quotation(currency.replace("[\" ]".toRegex(), ""), 0.0, 0.0, 0.0, 0.0))
        }

        rv = findViewById(R.id.rv)
        rv?.layoutManager = LinearLayoutManager(this)
        rv?.adapter = adapter
        choiceLib()
    }

    private fun choiceLib() {
        AlertDialog.Builder(this, R.style.MyDialogTheme)
            .setTitle("Choose WebSocket client")
            .setSingleChoiceItems(
                arrayOf("OkHttp", "AndroidAsync", "NeoVisionaries"),
                -1) { dialog, i ->
                thread(start = true) {
                    when(i) {
                        0 -> okhttp()
                        1 -> androidAsync()
                        2 -> nvWebsocketClient()
                    }
                    Thread.sleep(1000)
                    dialog.cancel()
                }
            }
            .setCancelable(false)
            .show()
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

        var q = quotes.find { it.cryptoName.equals(pojo.product_id) }
        if (q != null)

            this@MainActivity.runOnUiThread(java.lang.Runnable {
                adapter.notifyDataSetChanged()
            })
    }

    private fun okhttp() {
        val client = OkHttpClient()
        val request: Request = Request.Builder().url("wss://ws-feed.gdax.com").build()
        wbOkHttp = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: okhttp3.WebSocket, response: Response) {
                webSocket.send(
                    "{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}"
                )
            }

            override fun onMessage(webSocket: okhttp3.WebSocket, text: String) {
                output(Gson().fromJson(text, PojoQuatation::class.java))
            }

            override fun onClosing(webSocket: okhttp3.WebSocket, code: Int, reason: String) {
                webSocket.close(Constants().NORMAL_CLOSURE_STATUS, null)
                Toast.makeText(this@MainActivity, "Web socket is close", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(webSocket: okhttp3.WebSocket, t: Throwable, response: Response) {
                Toast.makeText(this@MainActivity, "Error installing web socket", Toast.LENGTH_LONG).show()
            }
        })
        client.dispatcher().executorService().shutdown()
    }

    private fun androidAsync() {
        AsyncHttpClient.getDefaultInstance()
            .websocket("wss://ws-feed.gdax.com", "wss", object : WebSocketConnectCallback {
                override fun onCompleted(ex: Exception?, webSocket: WebSocket) {
                    if (ex != null) {
                        ex.printStackTrace()
                        return
                    }
                    wbAndroidAsync = webSocket
                    webSocket.send("{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}")
                    webSocket.setStringCallback(object : com.koushikdutta.async.http.WebSocket.StringCallback {
                        override fun onStringAvailable(s: String) {
                            output(Gson().fromJson(s, PojoQuatation::class.java))
                        }
                    })
                }
            })

    }

    private fun nvWebsocketClient() {
        val ws: com.neovisionaries.ws.client.WebSocket = WebSocketFactory().createSocket("wss://ws-feed.gdax.com")
        wbNV = ws

        ws.addListener(object : WebSocketAdapter() {
            override fun onTextMessage(webSocket: com.neovisionaries.ws.client.WebSocket, message: String) {
                output(Gson().fromJson(message, PojoQuatation::class.java))
            }

            override fun onConnectError(websocket: com.neovisionaries.ws.client.WebSocket?, exception: WebSocketException?) {
                Toast.makeText(this@MainActivity, "Error installing web socket", Toast.LENGTH_LONG).show()
            }

            override fun onCloseFrame(websocket: com.neovisionaries.ws.client.WebSocket?, frame: WebSocketFrame?) {
                Toast.makeText(this@MainActivity, "Web socket is close", Toast.LENGTH_SHORT).show()
            }
        })

        thread(start = true) {
            ws.connect()
            ws.sendText("{\"type\": \"subscribe\", \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [" + Constants().CURRENCIES_USD + ", " + Constants().CURRENCIES_EUR + "] }]}")
        }
    }

    private fun closeWebSocket() {
        if(wbOkHttp != null)
            wbOkHttp.cancel()

        if(wbAndroidAsync != null)
            wbAndroidAsync.close()

        if(wbNV != null)
            wbNV.disconnect()
    }

}
