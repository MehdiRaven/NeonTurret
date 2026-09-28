package com.ravenstudio

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.*

enum class PowerUpType {
    DOUBLE_XP,
    SLOW_MOTION,
    SHIELD
}

data class PowerUpState(
    val type: PowerUpType,
    val durationSeconds: Int,
    val isActive: Boolean = false
)

class PowerUpManager(
    private val scope: CoroutineScope,
    private val onStateChange: (PowerUpType, Boolean) -> Unit
) {
    private val activeJobs = mutableMapOf<PowerUpType, Job>()
    
    // State for UI to observe timers
    val powerUpTimers = mutableStateMapOf<PowerUpType, Int>()
    
    var isShieldActive by mutableStateOf(false)
        private set

    fun activatePowerUp(type: PowerUpType, duration: Int) {
        if (type == PowerUpType.SHIELD) {
            isShieldActive = true
        }

        // Cancel existing job if already active
        activeJobs[type]?.cancel()
        
        activeJobs[type] = scope.launch {
            powerUpTimers[type] = duration
            onStateChange(type, true)
            
            for (i in duration downTo 1) {
                powerUpTimers[type] = i
                delay(1000)
            }
            
            deactivatePowerUp(type)
        }
    }

    fun deactivatePowerUp(type: PowerUpType) {
        activeJobs[type]?.cancel()
        activeJobs.remove(type)
        powerUpTimers.remove(type)
        if (type == PowerUpType.SHIELD) {
            isShieldActive = false
        }
        onStateChange(type, false)
    }

    fun reset() {
        activeJobs.values.forEach { it.cancel() }
        activeJobs.clear()
        powerUpTimers.clear()
        isShieldActive = false
    }
}
