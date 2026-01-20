package com.domedav.sportmatenobullshit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.domedav.sportmatenobullshit.ui.MainScreen
import com.domedav.sportmatenobullshit.ui.theme.SportmateNoBullshitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SportmateNoBullshitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(topPadding = innerPadding.calculateTopPadding())
                }
            }
        }
    }
}
