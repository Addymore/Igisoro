package com.example.igisoro.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val IgisoroColorScheme = darkColorScheme(
    primary = RoyalGold,
    onPrimary = ObsidianBlack,
    primaryContainer = CarvedWoodLight,
    onPrimaryContainer = RoyalGoldLight,
    secondary = ImigongoCrimson,
    onSecondary = WarmIvory,
    secondaryContainer = ImigongoCrimsonDark,
    onSecondaryContainer = WarmIvory,
    tertiary = EmeraldAccent,
    background = ObsidianBlack,
    onBackground = WarmIvory,
    surface = CharcoalSurface,
    onSurface = WarmIvory,
    surfaceVariant = ElevatedWoodSurface,
    onSurfaceVariant = MutedSand,
    outline = SubtleOutline
)

@Composable
fun IgisoroTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = IgisoroColorScheme,
        typography = Typography,
        content = content
    )
}
