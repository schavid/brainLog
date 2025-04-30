package com.example.brainlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.brainlog.ui.theme.BrainLogTheme
import android.graphics.Color


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ) { false }, // Annahme: Dunkler Hintergrund -> Helle Icons
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT
            ) { false }  // Annahme: Dunkler Hintergrund -> Helle Icons
        )

        setContent {
            BrainLogTheme {
                val backgroundImagePainter = painterResource(id = R.drawable.background)


                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = backgroundImagePainter,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                BrainLogApp()
            }
        }
    }
}
