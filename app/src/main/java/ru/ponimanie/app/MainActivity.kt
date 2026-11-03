package ru.ponimanie.app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

private val Night = Color(0xFF05070B)
private val Panel = Color(0xFF101722)
private val NeonBlue = Color(0xFF16D9FF)
private val MainText = Color(0xFFEAF7FF)
private val MutedText = Color(0xFF91A6B5)
private val mainHandler = Handler(Looper.getMainLooper())
private val networkExecutor = Executors.newSingleThreadExecutor()
private data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PonimanieScreen() } }
    }
}

@Composable
private fun PonimanieScreen() {
    val messages = remember { mutableStateListOf(ChatMessage("Я Рита. Давай исследовать мысли и вопросы вместе. Напиши, с чего начнём.", false)) }
    var draft by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Сообщения будут отправляться на сервер ИИ.") }

    Surface(Modifier.fillMaxSize(), color = Night) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("ПОНИМА", color = NeonBlue, fontSize = 28.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(5.dp))
            Text("ЧЕЛОВЕК  +  ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ", color = MutedText, fontSize = 10.sp, letterSpacing = 1.3.sp)
            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(18.dp)) {
                Column {
                    Text("Думать вместе. Решать самому.", color = MainText, fontSize = 19.sp)
                    Spacer(Modifier.height(10.dp))
                    Text("Ты задаёшь направление. ИИ помогает исследовать идеи, замечать связи и находить варианты. Последнее слово всегда за человеком.", color = MutedText, fontSize = 14.sp, lineHeight = 21.sp)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("ДИАЛОГ С РИТОЙ", color = NeonBlue, fontSize = 12.sp, letterSpacing = 1.8.sp)
            Spacer(Modifier.height(10.dp))
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(messages) { MessageBubble(it) }
            }
            Text(status, color = MutedText, fontSize = 11.sp, lineHeight = 15.sp)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = draft, onValueChange = { draft = it }, modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Напиши свою мысль…", color = MutedText) },
                shape = RoundedCornerShape(16.dp), maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MainText, unfocusedTextColor = MainText,
                    focusedBorderColor = NeonBlue, unfocusedBorderColor = Color(0xFF344452),
                    cursorColor = NeonBlue, focusedContainerColor = Panel, unfocusedContainerColor = Panel
                )
            )
            Spacer(Modifier.height(10.dp))
            Button(
                enabled = !sending && draft.isNotBlank(),
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty() && !sending) {
                        messages.add(ChatMessage(text, true))
                        draft = ""
                        sending = true
                        status = "Отправляю запрос серверу…"
                        val history = messages.drop(1).toList()
                        networkExecutor.execute {
                            try {
                                val answer = requestRita(history)
                                mainHandler.post {
                                    messages.add(ChatMessage(answer, false))
                                    status = "Ответ получен от сервера ИИ."
                                    sending = false
                                }
                            } catch (e: Exception) {
                                val reason = e.message ?: "ошибка соединения"
                                mainHandler.post {
                                    messages.add(ChatMessage("Не удалось получить ответ: " + reason, false))
                                    status = "Сервер недоступен или ещё не настроен."
                                    sending = false
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, contentColor = Night),
                shape = RoundedCornerShape(14.dp)
            ) { Text(if (sending) "ОТПРАВЛЯЮ…" else "ОТПРАВИТЬ") }
        }
    }
}

private fun requestRita(history: List<ChatMessage>): String {
    val base = BuildConfig.PONIMANIE_API_URL.trim().trimEnd('/')
    require(base.startsWith("https://")) { "HTTPS-адрес сервера ещё не задан" }
    val payload = JSONObject().put("messages", JSONArray().apply {
        history.forEach { m ->
            put(JSONObject().put("role", if (m.fromUser) "user" else "assistant").put("content", m.text))
        }
    })
    val connection = URL(base + "/chat").openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 50000
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
        connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
        val code = connection.responseCode
        val stream = if (code in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        if (code !in 200..299) throw IllegalStateException("сервер вернул HTTP " + code)
        return JSONObject(body).getString("reply").takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("сервер вернул пустой ответ")
    } finally {
        connection.disconnect()
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start) {
        Box(
            Modifier.fillMaxWidth(0.9f).background(
                if (message.fromUser) Color(0xFF073B50) else Panel,
                RoundedCornerShape(18.dp)
            ).padding(15.dp)
        ) {
            Text(message.text, color = if (message.fromUser) MainText else MutedText, fontSize = 14.sp, lineHeight = 21.sp)
        }
    }
}
