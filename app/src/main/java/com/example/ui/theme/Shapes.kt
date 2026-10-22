package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * iOS 26/27 shape ladder: pills for controls, 8 dp for small marks, 16 dp for
 * standard surfaces and inputs, 24 dp for large cards, sheets and dialogs.
 * Nothing above 24 dp except actual capsules.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = CircleShape,            // pill buttons (Apple HIG)
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

// Semantic iOS 26/27 shape tokens
val PillShape = CircleShape
val CardExpressiveShape = RoundedCornerShape(24.dp)
val ImmersiveHeaderShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
val CardSubtleShape = RoundedCornerShape(16.dp)
val ListRowShape = RoundedCornerShape(16.dp)
val DialogExpressiveShape = RoundedCornerShape(24.dp)
val SheetExpressiveShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
val InputExpressiveShape = RoundedCornerShape(16.dp)
val ButtonExpressiveShape = CircleShape
val ChipExpressiveShape = CircleShape
val SegmentedControlShape = CircleShape