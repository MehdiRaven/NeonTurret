package com.ravenstudio

import android.webkit.JavascriptInterface

class WebAppInterface(
    private val onScoreUpdate: (Int) -> Unit,
    private val onHighScoreUpdate: (Int) -> Unit,
    private val onGameStateChange: (String) -> Unit,
    private val onPowerUpCollected: (String) -> Unit,
    private val onShieldConsumed: () -> Unit
) {
    @JavascriptInterface
    fun updateScore(score: Int) {
        onScoreUpdate(score)
    }

    @JavascriptInterface
    fun updateHighScore(highScore: Int) {
        onHighScoreUpdate(highScore)
    }

    @JavascriptInterface
    fun onGameStateChanged(state: String) {
        onGameStateChange(state)
    }

    @JavascriptInterface
    fun collectPowerUp(type: String) {
        onPowerUpCollected(type)
    }

    @JavascriptInterface
    fun shieldConsumed() {
        onShieldConsumed()
    }
}
