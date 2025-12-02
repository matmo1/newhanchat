package com.newhanchat.demo.chatservices

import com.google.gson.Gson
import com.newhanchat.demo.loginandregister.ChatMessage
import com.newhanchat.demo.loginandregister.ChatMessageDTO
import com.newhanchat.demo.loginandregister.EditedMessage
import com.newhanchat.demo.loginandregister.MessageUpdate
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompHeader
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.ArrayDeque

class ChatManager {
    private var stompClient: StompClient? = null
    private var compositeDisposable = CompositeDisposable()
    private var isConnected = false
    private val pendingMessages = ArrayDeque<String>()

    // Flows for UI updates
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

        // REPLACE WITH YOUR IP
        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.143.133.97:8080/ws-chat")
        stompClient?.withClientHeartbeat(0)?.withServerHeartbeat(0)

        val headers = listOf(StompHeader("Authorization", "Bearer $jwtToken"))
        stompClient?.connect(headers)

        val lifecycleSubscription = stompClient?.lifecycle()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(Schedulers.io())
            ?.subscribe { lifecycleEvent ->
                when (lifecycleEvent.type) {
                    LifecycleEvent.Type.OPENED -> {
                        println("✅ Stomp connection opened")
                        isConnected = true
                        sendQueuedMessages()
                    }
                    LifecycleEvent.Type.ERROR -> {
                        println("❌ Stomp Error: ${lifecycleEvent.exception}")
                        isConnected = false
                    }
                    LifecycleEvent.Type.CLOSED -> {
                        println("⚠️ Stomp connection closed")
                        isConnected = false
                    }
                    else -> {}
                }
            }

        if (lifecycleSubscription != null) {
            compositeDisposable.add(lifecycleSubscription)
        }

        // 1. Subscribe to Messages
        val topicSubscription = stompClient?.topic("/user/queue/messages")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(Schedulers.io())
            ?.subscribe({ topicMessage ->
                try {
                    val receivedMessage = Gson().fromJson(topicMessage.payload, ChatMessageDTO::class.java)
                    _incomingMessages.tryEmit(receivedMessage)
                } catch (e: Exception) {
                    println("❌ Error parsing message: ${e.message}")
                }
            }, { throwable -> println("❌ Error on topic subscription: $throwable") })

        if (topicSubscription != null) {
            compositeDisposable.add(topicSubscription)
        }

        // 2. Subscribe to Edits
        val editSubscription = stompClient?.topic("/user/queue/message-updates")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(Schedulers.io())
            ?.subscribe({ topicMessage ->
                try {
                    val update = Gson().fromJson(topicMessage.payload, MessageUpdate::class.java)
                    _messageUpdates.tryEmit(update)
                } catch (e: Exception) {
                    println("❌ Error parsing edit: ${e.message}")
                }
            }, { error -> println("❌ Edit sub error: $error") })

        if (editSubscription != null) {
            compositeDisposable.add(editSubscription)
        }
    }

    fun sendMessage(message: ChatMessage) {
        val jsonMessage = Gson().toJson(message)
        if (isConnected) {
            internalSendMessage(jsonMessage)
        } else {
            println("⏳ Connection lost. Queuing message: ${message.content}")
            pendingMessages.add(jsonMessage)
        }
    }

    fun editMessage(messageId: String, newContent: String) {
        val payload = Gson().toJson(EditedMessage(messageId, newContent))
        stompClient?.send("/app/edit", payload)
            ?.subscribeOn(Schedulers.io())
            ?.subscribe(
                { println("✏️ Edit sent") },
                { error -> println("❌ Error sending edit: $error") }
            )
    }

    private fun sendQueuedMessages() {
        while (!pendingMessages.isEmpty() && isConnected) {
            val jsonMsg = pendingMessages.poll()
            if (jsonMsg != null) {
                internalSendMessage(jsonMsg)
            }
        }
    }

    private fun internalSendMessage(jsonMessage: String) {
        val sendSubscription = stompClient?.send("/app/chat", jsonMessage)
            ?.subscribeOn(Schedulers.io())
            ?.subscribe(
                { println("📤 Message Sent!") },
                { error -> println("❌ Error sending message: $error") }
            )
        if (sendSubscription != null) {
            compositeDisposable.add(sendSubscription)
        }
    }

    fun disconnect() {
        stompClient?.disconnect()
        compositeDisposable.dispose()
        isConnected = false
    }
}