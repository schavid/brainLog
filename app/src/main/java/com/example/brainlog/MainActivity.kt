package com.example.brainlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.brainlog.ui.theme.BrainLogTheme
import com.example.brainlog.view.Home


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BrainLogTheme {
                Scaffold(
                    // Falls du später z.B. topBar oder bottomBar verwenden möchtest, kannst du hier die entsprechenden Komponenten einfügen.
                    content = { innerPadding ->
                        // Hier wird der Homescreen gerendert. Wir übergeben das Padding aus dem Scaffold.
                        Home(modifier = Modifier.padding(innerPadding))
                    }
                )
            }
        }
    }
}