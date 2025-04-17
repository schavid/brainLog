package com.example.brainlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.brainlog.ui.theme.BrainLogTheme
import com.example.brainlog.view.Home
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier




class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainLogTheme {
                Home()
            }
        }
    }
}
