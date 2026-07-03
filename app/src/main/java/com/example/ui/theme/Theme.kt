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

private val DarkColorScheme =
  darkColorScheme(
    primary = Gold,
    secondary = DeepGreen,
    tertiary = ContainerHigh,
    background = TextDark,
    surface = TextDark,
    onPrimary = White,
    onSecondary = White,
    onTertiary = TextDark,
    onBackground = WarmBackground,
    onSurface = WarmBackground
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Gold,
    secondary = DeepGreen,
    tertiary = ContainerHigh,
    background = WarmBackground,
    surface = WarmBackground,
    onPrimary = White,
    onSecondary = White,
    onTertiary = TextDark,
    onBackground = TextDark,
    onSurface = TextDark
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Disable by default to keep the luxury gold & green theme!
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
