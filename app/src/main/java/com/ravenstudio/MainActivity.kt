package com.ravenstudio

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ravenstudio.ui.components.*
import com.ravenstudio.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()
    private var webView: WebView? = null

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("neon_turret_prefs", MODE_PRIVATE)
        val lang = prefs.getString("app_lang", "fa") ?: "fa"
        super.attachBaseContext(LocaleHelper.setLocale(newBase, lang))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Restore screen state if we just switched languages
        val prefs = getSharedPreferences("neon_turret_prefs", MODE_PRIVATE)
        if (prefs.getBoolean("should_open_settings", false)) {
            viewModel.currentScreen = GameScreen.SETTINGS
            prefs.edit().putBoolean("should_open_settings", false).apply()
        }

        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBg
                ) {
                    NeonTurretGame(viewModel)
                }
            }
        }
    }

    fun startGame() {
        webView?.evaluateJavascript("highScore = ${viewModel.highScore}; startGame()", null)
    }

    fun setPaused(paused: Boolean) {
        webView?.evaluateJavascript("setPaused($paused)", null)
    }

    fun setSoundEnabled(enabled: Boolean) {
        webView?.evaluateJavascript("setSoundEnabled($enabled)", null)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        webView?.evaluateJavascript("setHapticsEnabled($enabled)", null)
    }

    fun setWebViewRef(wv: WebView) {
        webView = wv
        setSoundEnabled(viewModel.soundEnabled)
        setHapticsEnabled(viewModel.hapticsEnabled)
        webView?.evaluateJavascript("highScore = ${viewModel.highScore}", null)
    }

    fun setTimeScale(scale: Float) {
        webView?.evaluateJavascript("window.setTimeScale($scale)", null)
    }

    fun setScoreMultiplier(multiplier: Int) {
        webView?.evaluateJavascript("window.setScoreMultiplier($multiplier)", null)
    }

    fun setShield(active: Boolean) {
        webView?.evaluateJavascript("window.setShield($active)", null)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NeonTurretGame(viewModel: GameViewModel) {
    val context = LocalActivity.current as? MainActivity ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    // Observe power-up state changes and notify WebView
    DisposableEffect(viewModel) {
        viewModel.onPowerUpStateChanged = { type, active ->
            when (type) {
                PowerUpType.DOUBLE_XP -> context.setScoreMultiplier(if (active) 2 else 1)
                PowerUpType.SLOW_MOTION -> context.setTimeScale(if (active) 0.5f else 1.0f)
                PowerUpType.SHIELD -> context.setShield(active)
            }
        }
        onDispose {
            viewModel.onPowerUpStateChanged = null
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    if (viewModel.currentScreen == GameScreen.PLAYING) {
                        viewModel.currentScreen = GameScreen.PAUSED
                        context.setPaused(true)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setBackgroundColor(android.graphics.Color.parseColor("#06050e"))
                    isVerticalScrollBarEnabled = false
                    isHorizontalScrollBarEnabled = false
                    overScrollMode = View.OVER_SCROLL_NEVER
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        mediaPlaybackRequiresUserGesture = false
                        allowFileAccess = true
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }

                    addJavascriptInterface(
                        WebAppInterface(
                            onScoreUpdate = { viewModel.updateScore(it) },
                            onHighScoreUpdate = { viewModel.updateHighScore(it) },
                            onGameStateChange = { viewModel.setGameState(it) },
                            onPowerUpCollected = { typeStr ->
                                try {
                                    val type = PowerUpType.valueOf(typeStr)
                                    val duration = when (type) {
                                        PowerUpType.DOUBLE_XP -> 10
                                        PowerUpType.SLOW_MOTION -> 6
                                        PowerUpType.SHIELD -> 10
                                    }
                                    viewModel.powerUpManager.activatePowerUp(type, duration)
                                } catch (e: Exception) {}
                            },
                            onShieldConsumed = {
                                viewModel.powerUpManager.deactivatePowerUp(PowerUpType.SHIELD)
                            }
                        ),
                        "Android"
                    )

                    webChromeClient = WebChromeClient()
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            view?.setBackgroundColor(android.graphics.Color.parseColor("#06050e"))
                        }
                    }

                    loadUrl("file:///android_asset/game.html")
                    context.setWebViewRef(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Screens
        Crossfade(targetState = viewModel.currentScreen, label = "screen_transition") { screen ->
            when (screen) {
                GameScreen.MAIN_MENU -> MainMenuOverlay(viewModel, context)
                GameScreen.PLAYING -> HudOverlay(viewModel, context)
                GameScreen.PAUSED -> PauseMenuOverlay(viewModel, context)
                GameScreen.SETTINGS -> SettingsOverlay(viewModel, context)
                GameScreen.HOW_TO_PLAY -> HowToPlayOverlay(viewModel, context)
                GameScreen.GAME_OVER -> GameOverOverlay(viewModel, context)
            }
        }
    }
}

@Composable
fun MainMenuOverlay(viewModel: GameViewModel, context: MainActivity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.85f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NeonTitle(stringResource(R.string.game_title))
        NeonSubtitle(stringResource(R.string.game_subtitle))
        Spacer(modifier = Modifier.height(24.dp))
        ScoreBadge(stringResource(R.string.best_score, viewModel.highScore))
        Spacer(modifier = Modifier.height(30.dp))

        Column(
            modifier = Modifier.widthIn(max = 290.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NeonButton(
                text = "▶ " + stringResource(R.string.play),
                color = LimeNeon,
                onClick = { context.startGame() },
                modifier = Modifier.fillMaxWidth()
            )
            NeonButton(
                text = "⚙ " + stringResource(R.string.settings),
                color = CyanNeon,
                onClick = { viewModel.currentScreen = GameScreen.SETTINGS },
                modifier = Modifier.fillMaxWidth()
            )
            NeonButton(
                text = "📖 " + stringResource(R.string.how_to_play),
                color = MagentaNeon,
                onClick = { viewModel.currentScreen = GameScreen.HOW_TO_PLAY },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun HudOverlay(viewModel: GameViewModel, context: MainActivity) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(
                text = viewModel.score.toString(),
                color = Color(0xFFFFFFFF),
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                style = TextStyle(
                    shadow = Shadow(color = CyanNeon, blurRadius = 10f),
                    letterSpacing = 0.sp
                ),
                maxLines = 1,
                softWrap = false
            )
            Text(
                text = stringResource(R.string.best_score, viewModel.highScore),
                color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(letterSpacing = 0.sp),
                maxLines = 1,
                softWrap = false
            )
        }

        NeonButton(
            onClick = {
                viewModel.currentScreen = GameScreen.PAUSED
                context.setPaused(true)
            },
            modifier = Modifier.size(54.dp).align(Alignment.TopEnd),
            color = CyanNeon
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height(20.dp)
                            .background(CyanNeon, RoundedCornerShape(2.dp))
                            .padding(1.dp)
                    )
                }
            }
        }

        // Power-up indicators
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 60.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.powerUpManager.powerUpTimers.forEach { (type, time) ->
                val (text, color) = when (type) {
                    PowerUpType.DOUBLE_XP -> stringResource(R.string.powerup_double_xp, time) to YellowNeon
                    PowerUpType.SLOW_MOTION -> stringResource(R.string.powerup_slow_mo, time) to CyanNeon
                    else -> "" to Color.White
                }
                
                if (text.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .background(DarkBg.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                            .border(1.dp, color, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = text,
                            color = color,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
            
            if (viewModel.powerUpManager.isShieldActive) {
                val shieldTime = viewModel.powerUpManager.powerUpTimers[PowerUpType.SHIELD] ?: 10
                Row(
                    modifier = Modifier
                        .background(DarkBg.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                        .border(1.dp, LimeNeon, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stringResource(R.string.powerup_shield_active)} ($shieldTime)",
                        color = LimeNeon,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun PauseMenuOverlay(viewModel: GameViewModel, context: MainActivity) {
    NeonDialog(
        title = stringResource(R.string.paused),
        onDismissRequest = {
            viewModel.currentScreen = GameScreen.PLAYING
            context.setPaused(false)
        }
    ) {
        Text(
            text = stringResource(R.string.score, viewModel.score),
            color = YellowNeon,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            style = TextStyle(letterSpacing = 0.sp),
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            NeonButton(
                text = "▶ " + stringResource(R.string.resume),
                color = LimeNeon,
                onClick = {
                    viewModel.currentScreen = GameScreen.PLAYING
                    context.setPaused(false)
                },
                modifier = Modifier.fillMaxWidth()
            )
            NeonButton(
                text = "🔄 " + stringResource(R.string.restart),
                color = CyanNeon,
                onClick = { context.startGame() },
                modifier = Modifier.fillMaxWidth()
            )
            NeonButton(
                text = "🏠 " + stringResource(R.string.main_menu),
                color = MagentaNeon,
                onClick = { viewModel.currentScreen = GameScreen.MAIN_MENU },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SettingsOverlay(viewModel: GameViewModel, context: MainActivity) {
    NeonDialog(
        title = stringResource(R.string.settings),
        onDismissRequest = { viewModel.currentScreen = GameScreen.MAIN_MENU }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                label = stringResource(R.string.sound_fx),
                isActive = viewModel.soundEnabled,
                onClick = {
                    viewModel.toggleSound()
                    context.setSoundEnabled(viewModel.soundEnabled)
                }
            )
            SettingRow(
                label = stringResource(R.string.haptics),
                isActive = viewModel.hapticsEnabled,
                onClick = {
                    viewModel.toggleHaptics()
                    context.setHapticsEnabled(viewModel.hapticsEnabled)
                }
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.language),
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(letterSpacing = 0.sp),
                    maxLines = 1,
                    softWrap = false
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LanguageButton(
                        label = stringResource(R.string.english),
                        isSelected = viewModel.currentLanguage == "en",
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.currentLanguage != "en") {
                            viewModel.changeLanguage("en")
                            context.finish()
                            context.startActivity(context.intent)
                        }
                    }
                    LanguageButton(
                        label = stringResource(R.string.persian),
                        isSelected = viewModel.currentLanguage == "fa",
                        modifier = Modifier.weight(1f)
                    ) {
                        if (viewModel.currentLanguage != "fa") {
                            viewModel.changeLanguage("fa")
                            context.finish()
                            context.startActivity(context.intent)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        NeonButton(
            text = stringResource(R.string.done),
            color = CyanNeon,
            onClick = { viewModel.currentScreen = GameScreen.MAIN_MENU },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun SettingRow(label: String, isActive: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Bold,
            style = TextStyle(letterSpacing = 0.sp),
            maxLines = 1,
            softWrap = false
        )
        NeonButton(
            text = if (isActive) stringResource(R.string.on) else stringResource(R.string.off),
            color = if (isActive) LimeNeon else MagentaNeon,
            onClick = onClick,
            modifier = Modifier.width(80.dp)
        )
    }
}

@Composable
fun LanguageButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    NeonButton(
        text = label,
        color = if (isSelected) CyanNeon else Color(0xFF888888),
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun HowToPlayOverlay(viewModel: GameViewModel, context: MainActivity) {
    NeonDialog(
        title = stringResource(R.string.how_to_play),
        onDismissRequest = { viewModel.currentScreen = GameScreen.MAIN_MENU }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            InstructionRow("🎯", stringResource(R.string.how_to_play_1_title), stringResource(R.string.how_to_play_1_desc))
            InstructionRow("💥", stringResource(R.string.how_to_play_2_title), stringResource(R.string.how_to_play_2_desc))
            InstructionRow("⚡", stringResource(R.string.how_to_play_3_title), stringResource(R.string.how_to_play_3_desc))
            InstructionRow("🔄", stringResource(R.string.how_to_play_4_title), stringResource(R.string.how_to_play_4_desc))
        }
        Spacer(modifier = Modifier.height(22.dp))
        NeonButton(
            text = stringResource(R.string.got_it),
            color = LimeNeon,
            onClick = { viewModel.currentScreen = GameScreen.MAIN_MENU },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun InstructionRow(icon: String, title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0x0AFFFFFF), RoundedCornerShape(12.dp)).padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Column {
            Text(
                title,
                color = CyanNeon,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                style = TextStyle(letterSpacing = 0.sp),
                maxLines = 1,
                softWrap = false
            )
            Text(
                desc,
                color = Color(0xD9FFFFFF),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                style = TextStyle(letterSpacing = 0.sp)
            )
        }
    }
}

@Composable
fun GameOverOverlay(viewModel: GameViewModel, context: MainActivity) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF100C22))
                .border(1.5.dp, MagentaNeon.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.game_over),
                style = TextStyle(
                    color = MagentaNeon,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(color = MagentaNeon, blurRadius = 24f),
                    letterSpacing = 0.sp
                ),
                maxLines = 1,
                softWrap = false
            )
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Text(
                text = viewModel.score.toString(),
                style = TextStyle(
                    color = Color(0xFFFFFFFF),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(color = CyanNeon, blurRadius = 18f),
                    letterSpacing = 0.sp
                ),
                maxLines = 1,
                softWrap = false
            )
            
            Text(
                text = stringResource(R.string.final_score),
                color = Color(0x8CFFFFFF),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(letterSpacing = 0.sp),
                maxLines = 1,
                softWrap = false
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = stringResource(R.string.best_score, viewModel.highScore),
                color = YellowNeon,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                style = TextStyle(letterSpacing = 0.sp),
                maxLines = 1,
                softWrap = false
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                NeonButton(
                    text = "🔄 " + stringResource(R.string.play_again),
                    color = LimeNeon,
                    onClick = { context.startGame() },
                    modifier = Modifier.fillMaxWidth()
                )
                NeonButton(
                    text = "🏠 " + stringResource(R.string.main_menu),
                    color = CyanNeon,
                    onClick = { viewModel.currentScreen = GameScreen.MAIN_MENU },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
