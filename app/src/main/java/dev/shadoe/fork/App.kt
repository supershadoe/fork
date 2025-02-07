package dev.shadoe.fork

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun App() {
    Column {
        Box(modifier = Modifier.weight(1f)) {
            Text("Hello, world")
        }
    }
}