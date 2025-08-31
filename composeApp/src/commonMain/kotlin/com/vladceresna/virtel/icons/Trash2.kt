package com.vladceresna.virtel.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Trash2: ImageVector
    get() {
        if (_Trash2 != null) return _Trash2!!
        
        _Trash2 = ImageVector.Builder(
            name = "Trash2",
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
                moveTo(3f, 6f)
                horizontalLineToRelative(18f)
                moveToRelative(-2f, 0f)
                verticalLineToRelative(14f)
                curveToRelative(0f, 1f, -1f, 2f, -2f, 2f)
                horizontalLineTo(7f)
                curveToRelative(-1f, 0f, -2f, -1f, -2f, -2f)
                verticalLineTo(6f)
                moveToRelative(3f, 0f)
                verticalLineTo(4f)
                curveToRelative(0f, -1f, 1f, -2f, 2f, -2f)
                horizontalLineToRelative(4f)
                curveToRelative(1f, 0f, 2f, 1f, 2f, 2f)
                verticalLineToRelative(2f)
                moveToRelative(-6f, 5f)
                verticalLineToRelative(6f)
                moveToRelative(4f, -6f)
                verticalLineToRelative(6f)
            }
        }.build()
        
        return _Trash2!!
    }

private var _Trash2: ImageVector? = null

