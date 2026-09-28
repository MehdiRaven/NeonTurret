package com.ravenstudio

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope

enum class GameScreen {
    MAIN_MENU,
    PLAYING,
    PAUSED,
    SETTINGS,
    HOW_TO_PLAY,
    GAME_OVER
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("neon_turret_prefs", Context.MODE_PRIVATE)
    
    // PowerUp listeners defined before manager
    var onPowerUpStateChanged: ((PowerUpType, Boolean) -> Unit)? = null

    val powerUpManager = PowerUpManager(
        scope = viewModelScope,
        onStateChange = { type, active ->
            onPowerUpStateChanged?.invoke(type, active)
        }
    )

    var currentScreen by mutableStateOf(GameScreen.MAIN_MENU)
    var score by mutableIntStateOf(0)
    var highScore by mutableIntStateOf(prefs.getInt("high_score", 0))

    var soundEnabled by mutableStateOf(prefs.getBoolean("sound_enabled", true))
    var hapticsEnabled by mutableStateOf(prefs.getBoolean("haptics_enabled", true))
    
    var currentLanguage by mutableStateOf(getSavedLanguage())
        private set

    private fun getSavedLanguage(): String {
        return prefs.getString("app_lang", "fa") ?: "fa"
    }

    fun updateScore(newScore: Int) {
        score = newScore
        if (score > highScore) {
            updateHighScore(score)
        }
    }

    fun updateHighScore(newHighScore: Int) {
        if (newHighScore > highScore) {
            highScore = newHighScore
            prefs.edit().putInt("high_score", highScore).apply()
        }
    }

    fun setGameState(state: String) {
        when (state) {
            "START" -> {
                currentScreen = GameScreen.MAIN_MENU
                powerUpManager.reset()
            }
            "PLAYING" -> currentScreen = GameScreen.PLAYING
            "GAMEOVER" -> {
                currentScreen = GameScreen.GAME_OVER
                powerUpManager.reset()
            }
        }
    }

    fun toggleSound() {
        soundEnabled = !soundEnabled
        prefs.edit().putBoolean("sound_enabled", soundEnabled).apply()
    }

    fun toggleHaptics() {
        hapticsEnabled = !hapticsEnabled
        prefs.edit().putBoolean("haptics_enabled", hapticsEnabled).apply()
    }

    fun changeLanguage(lang: String) {
        currentLanguage = lang
        // Set flag to re-open settings after activity recreation
        prefs.edit().putString("app_lang", lang).apply()
        prefs.edit().putBoolean("should_open_settings", true).apply()
    }
}
