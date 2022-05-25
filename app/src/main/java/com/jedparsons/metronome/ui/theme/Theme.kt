package com.jedparsons.metronome.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorPalette = darkColors(
  primary = Amber,
  secondary = DarkAmber,
  background = Color.Black,
  surface = Color.Black,
  error = Color(0xFFCF3639),
  onBackground = Amber,
  onSurface = Amber,
)

@Composable
fun MetronomeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {

  MaterialTheme(
    colors = DarkColorPalette,
    typography = Typography,
    shapes = Shapes,
    content = content
  )
}