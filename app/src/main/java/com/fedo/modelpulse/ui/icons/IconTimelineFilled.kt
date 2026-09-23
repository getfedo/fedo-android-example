package com.fedo.modelpulse.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val IconTimeline: ImageVector
    get() {
        if (_Timeline != null) {
            return _Timeline!!
        }
        _Timeline = ImageVector.Builder(
            name = "Timeline",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(120f, 720f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(40f, 640f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(120f, 560f)
                horizontalLineToRelative(10.5f)
                quadToRelative(4.5f, 0f, 9.5f, 2f)
                lineToRelative(182f, -182f)
                quadToRelative(-2f, -5f, -2f, -9.5f)
                lineTo(320f, 360f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(400f, 280f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(480f, 360f)
                quadToRelative(0f, 2f, -2f, 20f)
                lineToRelative(102f, 102f)
                quadToRelative(5f, -2f, 9.5f, -2f)
                horizontalLineToRelative(21f)
                quadToRelative(4.5f, 0f, 9.5f, 2f)
                lineToRelative(142f, -142f)
                quadToRelative(-2f, -5f, -2f, -9.5f)
                lineTo(760f, 320f)
                quadToRelative(0f, -33f, 23.5f, -56.5f)
                reflectiveQuadTo(840f, 240f)
                quadToRelative(33f, 0f, 56.5f, 23.5f)
                reflectiveQuadTo(920f, 320f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(840f, 400f)
                horizontalLineToRelative(-10.5f)
                quadToRelative(-4.5f, 0f, -9.5f, -2f)
                lineTo(678f, 540f)
                quadToRelative(2f, 5f, 2f, 9.5f)
                verticalLineToRelative(10.5f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(600f, 640f)
                quadToRelative(-33f, 0f, -56.5f, -23.5f)
                reflectiveQuadTo(520f, 560f)
                verticalLineToRelative(-10.5f)
                quadToRelative(0f, -4.5f, 2f, -9.5f)
                lineTo(420f, 438f)
                quadToRelative(-5f, 2f, -9.5f, 2f)
                lineTo(400f, 440f)
                quadToRelative(-2f, 0f, -20f, -2f)
                lineTo(198f, 620f)
                quadToRelative(2f, 5f, 2f, 9.5f)
                verticalLineToRelative(10.5f)
                quadToRelative(0f, 33f, -23.5f, 56.5f)
                reflectiveQuadTo(120f, 720f)
                close()
            }
        }.build()

        return _Timeline!!
    }

@Suppress("ObjectPropertyName")
private var _Timeline: ImageVector? = null
