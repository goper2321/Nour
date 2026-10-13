package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape Scale
 * Features distinctive, expressive shapes from tight pills to deeply rounded containers.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

// Semantic Expressive Shape Tokens
val PillShape = CircleShape
val CardExpressiveShape = RoundedCornerShape(28.dp)
// Immersive hero header: square at the top so the sky bleeds under the system
// status bar, rounded at the bottom where it meets the scrolling content.
val ImmersiveHeaderShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
val CardSubtleShape = RoundedCornerShape(14.dp)
val ListRowShape = RoundedCornerShape(22.dp)
val DialogExpressiveShape = RoundedCornerShape(28.dp)
val SheetExpressiveShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
val InputExpressiveShape = RoundedCornerShape(16.dp)
val ButtonExpressiveShape = CircleShape
val ChipExpressiveShape = RoundedCornerShape(16.dp)

