package ir.amirreza.socket_module

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.internal.wait
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(JUnit4::class)
class ConnectionTest {

    private val viewModel = HomeViewModel()

    @Test
    fun check_connect_to_sse() {
        var connected = false
        val callback = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                connected = true
            }
        }
        viewModel.testConnect(callback)
        Thread.sleep(5000)
        assert(connected)
    }

    @Test
    fun check_connect_to_ws() {
        var connected = false
        val callback = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                connected = true
            }
        }
        viewModel.testConnectMessage(callback)
        Thread.sleep(5000)
        assert(connected)
    }

    @Test
    fun check_send_message() {
        val message = "Hello"
        var receivedMessage = ""
        val callback = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                receivedMessage = text
                super.onMessage(webSocket, text)
            }
        }
        viewModel.testConnectMessage(callback)
        viewModel.text = message
        viewModel.send()
        Thread.sleep(5000)
        assert(message == receivedMessage)
    }

    @Test
    fun check_receive_is_time() {
        var event = ""
        val countDownLatch = CountDownLatch(0)
        val callback = object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                event = type ?: ""
                countDownLatch.countDown()
                super.onEvent(eventSource, id, type, data)
            }
        }
        viewModel.testConnect(callback)
        countDownLatch.await()
        assert(event == "time")
    }
}