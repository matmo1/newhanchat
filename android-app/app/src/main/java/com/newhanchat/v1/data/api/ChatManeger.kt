package com.newhanchat.v1.data.api

import android.util.Log
import com.google.gson.Gson
import com.newhanchat.v1.data.model.ChatMessage
import com.newhanchat.v1.data.model.ChatMessageDTO
import com.newhanchat.v1.data.model.EditedMessage
import com.newhanchat.v1.data.model.MessageUpdate
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.schedulers.Schedulers as SchedulersRx2
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.ArrayDeque
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader

class ChatManager {
    private var stompClient: StompClient? = null
    private var compositeDisposable = CompositeDisposable()
    private var isConnected = false
    private val pendingMessages = ArrayDeque<String>()

    private val _incomingMessages = MutableSharedFlow<ChatMessageDTO>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages = _incomingMessages.asSharedFlow()

    private val _messageUpdates = MutableSharedFlow<MessageUpdate>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messageUpdates = _messageUpdates.asSharedFlow()

    fun connect(jwtToken: String) {
        if (compositeDisposable.isDisposed) {
            compositeDisposable = CompositeDisposable()
        }

        // Stomp.over requires okhttp3.OkHttpClient in the classpath
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://192.168.1.89:8082/ws-chat")
        stompClient?.withClientHeartbeat(10000)?.withServerHeartbeat(10000)

        val headers = listOf(StompHeader("Authorization", "Bearer $jwtToken"))
        stompClient?.connect(headers)

        val lifecycleSubscription = stompClient?.lifecycle()
            ?.subscribeOn(SchedulersRx2.io())
            ?.subscribe({ lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        isConnected = true
                        sendQueuedMessages()
                        Log.d("ChatManager", "STOMP connection opened")
                    }
                    LifecycleEvent.Type.ERROR -> {
                        Log.e("ChatManager", "STOMP error", lifecycleEvent.exception)
                        isConnected = false
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        isConnected = false
                        Log.d("ChatManager", "STOMP connection closed")
                    }
                    else -> {}
                }
            }, { it.printStackTrace() })

        lifecycleSubscription?.let { compositeDisposable.add(it as Disposable) }

        // Subscription for Chat Messages
        val topicSubscription = stompClient?.topic("/user/queue/messages")
            ?.subscribeOn(SchedulersRx2.io())
            ?.subscribe({ topicMessage ->
                try {
                    val receivedMessage = Gson().fromJson(topicMessage.payload, ChatMessageDTO::class.java)
                    _incomingMessages.tryEmit(receivedMessage)
                } catch (e: Exception) {
                    Log.e("ChatManager", "Parse Error in messages", e)
                }
            }, { it.printStackTrace() })

        topicSubscription?.let { compositeDisposable.add(it as Disposable) }

        // Subscription for Message Updates (Edits)
        val editSubscription = stompClient?.topic("/user/queue/message-updates")
            ?.subscribeOn(SchedulersRx2.io())
            ?.subscribe({ topicMessage ->
                try {
                    val update = Gson().fromJson(topicMessage.payload, MessageUpdate::class.java)
                    _messageUpdates.tryEmit(update)
                } catch (e: Exception) {
                    Log.e("ChatManager", "Edit Parse Error", e)
                }
            }, { it.printStackTrace() })

        editSubscription?.let { compositeDisposable.add(it as Disposable) }
    }

    fun sendMessage(message: ChatMessage) {
        val jsonMessage = Gson().toJson(message)
        if (isConnected) {
            internalSendMessage(jsonMessage)
        } else {
            pendingMessages.add(jsonMessage)
        }
    }

    private fun internalSendMessage(jsonMessage: String) {
        val sendSub = stompClient?.send("/app/chat", jsonMessage)
            ?.subscribeOn(SchedulersRx2.io())
            ?.subscribe({
                Log.d("ChatManager", "Message sent")
            }, {
                Log.e("ChatManager", "Send error", it)
            })
        sendSub?.let { compositeDisposable.add(it as Disposable) }
    }

    fun editMessage(messageId: String, newContent: String) {
        val payload = Gson().toJson(EditedMessage(messageId, newContent))
        val editSub = stompClient?.send("/app/edit", payload)
            ?.subscribeOn(SchedulersRx2.io())
            ?.subscribe({
                Log.d("ChatManager", "Edit request sent")
            }, {
                Log.e("ChatManager", "Edit error", it)
            })
        editSub?.let { compositeDisposable.add(it as Disposable) }
    }

    private fun sendQueuedMessages() {
        while (!pendingMessages.isEmpty() && isConnected) {
            pendingMessages.poll()?.let { internalSendMessage(it) }
        }
    }

    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.dispose()
        isConnected = false
    }
}