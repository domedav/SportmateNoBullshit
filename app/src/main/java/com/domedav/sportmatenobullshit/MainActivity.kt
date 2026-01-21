package com.domedav.sportmatenobullshit

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.domedav.sportmatenobullshit.ui.MainScreen
import com.domedav.sportmatenobullshit.ui.theme.SportmateNoBullshitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    fun getAnimationScale(context: Context): Float {
        return Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        enableEdgeToEdge()

        var isDataReady = false
        var isMinTimeElapsed = false

        val baseDuration = 1000L
        val scale = getAnimationScale(this)
        val actualDuration = (baseDuration * scale).toLong()

        lifecycleScope.launch {
            delay(actualDuration)
            isMinTimeElapsed = true
        }

        setContent {
            SportmateNoBullshitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(topPadding = innerPadding.calculateTopPadding())
                    LaunchedEffect(Unit) {
                        isDataReady = true
                    }
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            !(isMinTimeElapsed && isDataReady)
        }
    }
}
