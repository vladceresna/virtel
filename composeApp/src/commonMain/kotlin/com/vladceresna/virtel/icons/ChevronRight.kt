package com.vladceresna.virtel.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val ChevronRight: ImageVector
    get() {
        if (_ChevronRight != null) return _ChevronRight!!
        
        _ChevronRight = ImageVector.Builder(
            name = "ChevronRight",
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
                moveToRelative(9f, 18f)
                lineToRelative(6f, -6f)
                lineToRelative(-6f, -6f)
            }
        }.build()
        
        return _ChevronRight!!
    }

private var _ChevronRight: ImageVector? = null

