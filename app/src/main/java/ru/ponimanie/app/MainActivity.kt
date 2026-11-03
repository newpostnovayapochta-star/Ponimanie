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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Night = Color(0xFF05070B)
private val Panel = Color(0xFF101722)
private val NeonBlue = Color(0xFF16D9FF)
private val MainText = Color(0xFFEAF7FF)
private val MutedText = Color(0xFF91A6B5)

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
                "Понима помогает тебе думать вместе с технологией: ты ставишь цель, а ИИ подсказывает варианты без давления. Ты выбираешь, ИИ поддерживает твой выбор и уточняет детали. Без спешки, без команд, вместе, но ты главный.",
                fromUser = false
            )
        )
    }
    var draft by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = Night) {
        Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
            Text(
                text = "ПОНИМА",
                color = NeonBlue,
                fontSize = 28.sp,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "ЧЕЛОВЕК  +  ИСКУССТВЕННЫЙ ИНТЕЛЛЕКТ",
                color = MutedText,
                fontSize = 10.sp,
                letterSpacing = 1.3.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Panel, RoundedCornerShape(22.dp))
                    .padding(18.dp)
            ) {
                Column {
                    Text(
                        text = "Думать вместе. Решать самому.",
                        color = MainText,
                        fontSize = 19.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Ты задаёшь направление. ИИ помогает исследовать идеи, замечать связи и находить варианты. Последнее слово всегда за человеком.",
                        color = MutedText,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ДИАЛОГ С РИТОЙ",
                color = NeonBlue,
                fontSize = 12.sp,
                letterSpacing = 1.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { message -> MessageBubble(message) }
            }

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Напиши свою мысль…", color = MutedText) },
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MainText,
                    unfocusedTextColor = MainText,
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color(0xFF344452),
                    cursorColor = NeonBlue,
                    focusedContainerColor = Panel,
                    unfocusedContainerColor = Panel
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val text = draft.trim()
                    if (text.isNotEmpty()) {
                        messages.add(ChatMessage(text, fromUser = true))
                        messages.add(
                            ChatMessage(
                                "Я сохранила твою мысль в этом открытом диалоге. Пока это демонстрационный экран: подключение настоящей модели ИИ будет следующим этапом.",
                                fromUser = false
                            )
                        )
                        draft = ""
                    }
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonBlue,
                    contentColor = Night
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("ОТПРАВИТЬ")
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
                .fillMaxWidth(0.9f)
                .background(
                    color = if (message.fromUser) Color(0xFF073B50) else Panel,
                    shape = RoundedCornerShape(18.dp)
                )
                .padding(15.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.fromUser) MainText else MutedText,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}
