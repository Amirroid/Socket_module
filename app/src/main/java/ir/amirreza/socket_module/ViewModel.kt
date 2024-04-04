package ir.amirreza.socket_module

import android.util.Log
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.ByteString
import java.io.IOException
import java.net.Socket
import java.time.Duration
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class HomeViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<String>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _times = MutableStateFlow<List<String>>(emptyList())
    val times = _times.asStateFlow()

    var messageState by mutableStateOf(ResponseState.LOADING)
    var timeState by mutableStateOf(ResponseState.LOADING)

    var text by mutableStateOf("")

    private lateinit var socket: WebSocket


    private val client = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.DAYS)
        .build()

    private val request = Request.Builder()
        .url("https://echo.websocket.org/.sse")
        .addHeader("Accept", "text/event-stream")
        .build()


    private val messageRequest = Request.Builder()
        .url("ws://echo.websocket.org/")
        .build()

    fun connect() = viewModelScope.launch(Dispatchers.IO) {
        EventSources.createFactory(client)
            .newEventSource(request, TimeCallback())
        socket = client.newWebSocket(messageRequest, MessageCallback())
    }

    fun testConnect(callback: EventSourceListener) = viewModelScope.launch(Dispatchers.IO) {
        EventSources.createFactory(client)
            .newEventSource(request, callback)
    }

    fun send() {
        socket.send(text)
        text = ""
    }

    inner class TimeCallback : EventSourceListener() {
        override fun onOpen(eventSource: EventSource, response: Response) {
            timeState = ResponseState.CONNECTED
            super.onOpen(eventSource, response)
        }

        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            Log.d(MainActivity.TAG, "onEvent: $type")
            if (type == "time") {
                _times.update { it + "$id - $data" }
            }
            super.onEvent(eventSource, id, type, data)
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            timeState = ResponseState.ERROR
            t?.printStackTrace()
            Log.e(MainActivity.TAG, "onFailure: ${t?.message}")
            super.onFailure(eventSource, t, response)
        }
    }

    inner class MessageCallback : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            _messages.update { it + text }
            super.onMessage(webSocket, text)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            messageState = ResponseState.ERROR
            super.onFailure(webSocket, t, response)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            messageState = ResponseState.CONNECTED
            super.onOpen(webSocket, response)
        }
    }
}