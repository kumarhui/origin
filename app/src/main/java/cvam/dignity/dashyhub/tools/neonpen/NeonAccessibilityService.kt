package cvam.dignity.dashyhub.tools.neonpen

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Build
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService providing floating control overlay privileges
 * and multi-touch gesture pass-through (2-finger scroll and pinch-zoom)
 * when overlay drawing mode is active.
 */
class NeonAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: NeonAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Intentionally blank to strictly respect user privacy
    }

    override fun onInterrupt() {
        // Handle service interruption
    }

    fun dispatchTwoFingerScroll(startX: Float, startY: Float, endX: Float, endY: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val spacing = 80f
        val path1 = Path().apply {
            moveTo(startX - spacing / 2f, startY)
            lineTo(endX - spacing / 2f, endY)
        }
        val path2 = Path().apply {
            moveTo(startX + spacing / 2f, startY)
            lineTo(endX + spacing / 2f, endY)
        }

        val stroke1 = GestureDescription.StrokeDescription(path1, 0, 100)
        val stroke2 = GestureDescription.StrokeDescription(path2, 0, 100)

        val gesture = GestureDescription.Builder()
            .addStroke(stroke1)
            .addStroke(stroke2)
            .build()

        try {
            dispatchGesture(gesture, null, null)
        } catch (_: Exception) {}
    }

    fun dispatchPinchZoom(centerX: Float, centerY: Float, startDist: Float, endDist: Float) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return

        val halfStart = (startDist / 2f).coerceAtLeast(20f)
        val halfEnd = (endDist / 2f).coerceAtLeast(20f)

        val path1 = Path().apply {
            moveTo(centerX - halfStart, centerY)
            lineTo(centerX - halfEnd, centerY)
        }
        val path2 = Path().apply {
            moveTo(centerX + halfStart, centerY)
            lineTo(centerX + halfEnd, centerY)
        }

        val stroke1 = GestureDescription.StrokeDescription(path1, 0, 120)
        val stroke2 = GestureDescription.StrokeDescription(path2, 0, 120)

        val gesture = GestureDescription.Builder()
            .addStroke(stroke1)
            .addStroke(stroke2)
            .build()

        try {
            dispatchGesture(gesture, null, null)
        } catch (_: Exception) {}
    }
}