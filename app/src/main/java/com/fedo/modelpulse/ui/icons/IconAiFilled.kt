package com.fedo.modelpulse.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconAiFilled: ImageVector
    get() {
        if (_AiFilled != null) {
            return _AiFilled!!
        }
        _AiFilled = ImageVector.Builder(
            name = "AiFilled",
            defaultWidth = 100.dp,
            defaultHeight = 100.dp,
            viewportWidth = 100f,
            viewportHeight = 100f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(35f, 15f)
                curveTo(35f, 30f, 40f, 45f, 70f, 50f)
                curveTo(40f, 55f, 35f, 70f, 35f, 85f)
                curveTo(35f, 70f, 30f, 55f, 0f, 50f)
                curveTo(30f, 45f, 35f, 30f, 35f, 15f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(75f, 10f)
                curveTo(75f, 17f, 77.5f, 22.5f, 90f, 25f)
                curveTo(77.5f, 27.5f, 75f, 33f, 75f, 40f)
                curveTo(75f, 33f, 72.5f, 27.5f, 60f, 25f)
                curveTo(72.5f, 22.5f, 75f, 17f, 75f, 10f)
                close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(75f, 60f)
                curveTo(75f, 67f, 77.5f, 72.5f, 90f, 75f)
                curveTo(77.5f, 77.5f, 75f, 83f, 75f, 90f)
                curveTo(75f, 83f, 72.5f, 77.5f, 60f, 75f)
                curveTo(72.5f, 72.5f, 75f, 67f, 75f, 60f)
                close()
            }
        }.build()

        return _AiFilled!!
    }

@Suppress("ObjectPropertyName")
private var _AiFilled: ImageVector? = null
