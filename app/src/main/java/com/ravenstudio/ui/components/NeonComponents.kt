package com.ravenstudio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.geometry.Offset

val CyanNeon = Color(0xFF00F3FF)
val MagentaNeon = Color(0xFFFF007F)
val LimeNeon = Color(0xFF39FF14)
val YellowNeon = Color(0xFFFFE600)
val DarkBg = Color(0xFF06050E)

@Composable
fun NeonButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CyanNeon,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "button_scale")

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = if (isPressed) 0.35f else 0.12f))
            .border(1.8.dp, color, RoundedCornerShape(14.dp))
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun NeonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CyanNeon,
    textStyle: TextStyle = TextStyle(
        fontSize = 15.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.sp // Fix: Set to 0 to ensure proper Persian character connections
    )
) {
    NeonButton(
        onClick = onClick,
        modifier = modifier,
        color = color
    ) {
        Text(
            text = text,
            maxLines = 1,
            softWrap = false,
            style = textStyle.copy(
                color = color,
                shadow = Shadow(
                    color = color.copy(alpha = 0.8f),
                    offset = Offset(0f, 0f),
                    blurRadius = 10f
                )
            )
        )
    }
}

@Composable
fun NeonTitle(text: String, color: Color = CyanNeon) {
    Text(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = 38.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.sp, // Fix: letterSpacing breaks Persian connections
            shadow = Shadow(
                color = color.copy(alpha = 0.8f),
                offset = Offset(0f, 0f),
                blurRadius = 15f
            )
        )
    )
}

@Composable
fun NeonSubtitle(text: String, color: Color = MagentaNeon) {
    Text(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp, // Fix: letterSpacing breaks Persian connections
            shadow = Shadow(
                color = color.copy(alpha = 0.6f),
                offset = Offset(0f, 0f),
                blurRadius = 12f
            )
        )
    )
}

@Composable
fun ScoreBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(YellowNeon.copy(alpha = 0.1f))
            .border(1.dp, YellowNeon.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = YellowNeon,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun NeonDialog(
    title: String,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = true, onClick = onDismissRequest),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 340.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF100C22))
                .border(1.5.dp, CyanNeon.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                .clickable(enabled = false) {}
                .padding(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = CyanNeon,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.sp,
                    shadow = Shadow(
                        color = CyanNeon.copy(alpha = 0.7f),
                        blurRadius = 12f
                    )
                ),
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(18.dp))
            content()
        }
    }
}
