package com.example.brainlog.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun SearchBar(
    isSearchExpanded: Boolean,
    searchText: String,
    onSearchExpandedChange: (Boolean) -> Unit,
    onSearchTextChange: (String) -> Unit
) {
    AnimatedVisibility(
        visible = isSearchExpanded,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 500)
        ) + fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 500)
        ) + fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Column (
            modifier = Modifier.fillMaxWidth()
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier.weight(1f).background(Color.White),
                    placeholder = { Text("Search") }
                )
                IconButton(onClick = {
                    onSearchExpandedChange(false)
                    onSearchTextChange("")
                }) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        }
    }
}