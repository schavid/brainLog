package com.example.brainlog.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchResultsList(results: List<String>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        results.forEachIndexed { index, result ->
            // Einzelnes Ergebnis
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = result,
                        color = Color.Black
                    )
                }
            }

            // Trennlinie (außer beim letzten Eintrag)
            if (index < results.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    thickness = 1.dp,
                    color = Color(0xFFE0E0E0)
                )
            }
        }
    }
}