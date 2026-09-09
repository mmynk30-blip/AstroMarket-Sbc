package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val AstroDarkColorScheme =
  darkColorScheme(
    primary = VedicGold,
    onPrimary = CosmicDeepNavy,
    primaryContainer = VedicGoldDark,
    onPrimaryContainer = VedicGoldLight,
    secondary = CelestialCyan,
    onSecondary = CosmicDeepNavy,
    secondaryContainer = CosmicSurfaceElevated,
    onSecondaryContainer = CelestialCyan,
    tertiary = BullishEmerald,
    onTertiary = CosmicDeepNavy,
    background = CosmicDeepNavy,
    onBackground = TextPrimaryLight,
    surface = CosmicSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CosmicSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = CosmicCardBorder,
  )

private val AstroLightColorScheme = AstroDarkColorScheme // Consistent celestial experience

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Celestial cosmic theme default
  dynamicColor: Boolean = false, // Preserve brand identity
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = AstroDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
