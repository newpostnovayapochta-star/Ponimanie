package ru.ponimanie.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class ChatMessage(val text: String, val fromUser: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                PonimanieScreen()
            }
        }
    }
}

@Composable
private fun PonimanieScreen() {
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                "Давай не просто обмениваться ответами. Попробуем вместе разобраться, что именно стоит за твоим вопросом.",
                fromUser = false
            )
        )
    }
    var draft by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF6F4F0)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            Text(
                text = "Понимание",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF202522)
            )
            Text(
                text = "Диалог человека и искусственного интеллекта",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF68716B)
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message ->
                    MessageBubble(message)
                }
            }

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("О чём хочешь поразмышлять?") },
                shape = RoundedCornerShape(18.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        messages.add(ChatMessage(text, fromUser = true))
                        messages.add(
                            ChatMessage(
                                "Я услышала вопрос. Следующий этап — подключить языковую модель, чтобы я могла исследовать его вместе с тобой, уточнять смысл и проверять ход рассуждения.",
                                fromUser = false
                            )
                        )
                        draft = ""
                    }
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Отправить")
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = if (message.fromUser) Color(0xFFE2E9E3) else Color.White,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                color = Color(0xFF202522),
                fontSize = 15.sp
            )
        }
    }
}
