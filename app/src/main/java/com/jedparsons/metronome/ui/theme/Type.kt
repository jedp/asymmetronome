package com.jedparsons.metronome.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.jedparsons.metronome.R

// Set of Material typography styles to start with
val Typography = Typography(
  h1 = TextStyle(
    fontFamily = FontFamily(Font(R.font.lexend_exa_light)),
    fontSize = 16.sp
  ),

  body1 = TextStyle(
    fontFamily = FontFamily(Font(R.font.lexend_exa_thin)),
    fontSize = 12.sp
  )
)
