package com.vladceresna.virtel.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Expand: ImageVector
    get() {
        if (_Expand != null) return _Expand!!
        
        _Expand = ImageVector.Builder(
            name = "Expand",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveToRelative(21f, 21f)
                lineToRelative(-6f, -6f)
                moveToRelative(6f, 6f)
                verticalLineToRelative(-4.8f)
                moveToRelative(0f, 4.8f)
                horizontalLineToRelative(-4.8f)
                moveTo(3f, 16.2f)
                verticalLineTo(21f)
                moveToRelative(0f, 0f)
                horizontalLineToRelative(4.8f)
                moveTo(3f, 21f)
                lineToRelative(6f, -6f)
                moveToRelative(12f, -7.2f)
                verticalLineTo(3f)
                moveToRelative(0f, 0f)
                horizontalLineToRelative(-4.8f)
                moveTo(21f, 3f)
                lineToRelative(-6f, 6f)
                moveTo(3f, 7.8f)
                verticalLineTo(3f)
                moveToRelative(0f, 0f)
                horizontalLineToRelative(4.8f)
                moveTo(3f, 3f)
                lineToRelative(6f, 6f)
            }
        }.build()
        
        return _Expand!!
    }

private var _Expand: ImageVector? = null

