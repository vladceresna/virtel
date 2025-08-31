package com.vladceresna.virtel.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Delete: ImageVector
    get() {
        if (_Delete != null) return _Delete!!
        
        _Delete = ImageVector.Builder(
            name = "Delete",
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
                moveTo(10f, 5f)
                arcToRelative(2f, 2f, 0f, false, false, -1.344f, 0.519f)
                lineToRelative(-6.328f, 5.74f)
                arcToRelative(1f, 1f, 0f, false, false, 0f, 1.481f)
                lineToRelative(6.328f, 5.741f)
                arcTo(2f, 2f, 0f, false, false, 10f, 19f)
                horizontalLineToRelative(10f)
                arcToRelative(2f, 2f, 0f, false, false, 2f, -2f)
                verticalLineTo(7f)
                arcToRelative(2f, 2f, 0f, false, false, -2f, -2f)
                close()
                moveToRelative(2f, 4f)
                lineToRelative(6f, 6f)
                moveToRelative(0f, -6f)
                lineToRelative(-6f, 6f)
            }
        }.build()
        
        return _Delete!!
    }

private var _Delete: ImageVector? = null

