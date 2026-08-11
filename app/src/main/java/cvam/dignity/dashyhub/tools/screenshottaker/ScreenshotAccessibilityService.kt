package cvam.dignity.dashyhub.tools.screenshottaker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RectF
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.view.Display
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import cvam.dignity.dashyhub.tools.common.SharedPermissionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

/**
 * AccessibilityService providing display capture and gesture dispatch
 * alongside floating control launcher and draggable crop overlay windows.
 */
class ScreenshotAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: ScreenshotAccessibilityService? = null
            private set

        fun isServiceEnabled(context: Context): Boolean {
            if (instance != null) return true
            return SharedPermissionManager.isAccessibilityServiceEnabled(context, ScreenshotAccessibilityService::class.java)
        }

        fun showFloatingControl(context: Context): Boolean {
            val service = instance ?: return false
            service.showFloatingOverlay()
            return true
        }

        fun hideFloatingControl() {
            instance?.hideFloatingOverlay()
        }

        fun isFloatingControlVisible(): Boolean {
            return instance?.floatingView?.isAttachedToWindow == true
        }

        fun showCropOverlay(context: Context): Boolean {
            val service = instance ?: return false
            service.showCropWindow()
            return true
        }

        fun hideCropOverlay() {
            instance?.hideCropWindow()
        }

        fun isCropOverlayVisible(): Boolean {
            return instance?.cropView?.isAttachedToWindow == true
        }

        fun getNormalizedCropBounds(): RectF? {
            return instance?.cropView?.getCropRectNormalized()
        }
    }

    private val executor = Executors.newSingleThreadExecutor()
    private val serviceScope = CoroutineScope(Dispatchers.Main)
    private var windowManager: WindowManager? = null

    private var floatingView: FloatingScreenshotView? = null
    private var floatingParams: WindowManager.LayoutParams? = null

    private var cropView: CropOverlayView? = null
    private var cropParams: WindowManager.LayoutParams? = null

    private val collapsedSizeDp = 52
    private val expandedWidthDp = 280
    private val expandedHeightDp = 52

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        hideFloatingOverlay()
        hideCropWindow()
        instance = null
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        hideFloatingOverlay()
        hideCropWindow()
        instance = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    @SuppressLint("ClickableViewAccessibility")
    fun showFloatingOverlay() {
        if (floatingView?.isAttachedToWindow == true) return
        val wm = windowManager ?: (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        windowManager = wm

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val density = resources.displayMetrics.density
        val buttonSizePx = (collapsedSizeDp * density).toInt()

        val params = WindowManager.LayoutParams(
            buttonSizePx,
            buttonSizePx,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 16
            y = (240 * density).toInt()
        }

        floatingParams = params
        val view = FloatingScreenshotView(this)
        floatingView = view

        view.setOnTouchListener { _: View, event: MotionEvent ->
            view.handleTouch(event, wm, params)
        }

        view.onRequestResizeListener = { isExpanded: Boolean ->
            updateFloatingWindowSize(isExpanded)
        }

        view.onRequestHideListener = {
            hideFloatingOverlay()
        }

        try {
            wm.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideFloatingOverlay() {
        val view = floatingView ?: return
        if (view.isAttachedToWindow) {
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        floatingView = null
    }

    fun showCropWindow() {
        if (cropView?.isAttachedToWindow == true) return
        val wm = windowManager ?: (getSystemService(Context.WINDOW_SERVICE) as WindowManager)
        windowManager = wm

        val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        cropParams = params
        val view = CropOverlayView(this)
        cropView = view

        try {
            wm.addView(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun hideCropWindow() {
        val view = cropView ?: return
        if (view.isAttachedToWindow) {
            try {
                windowManager?.removeView(view)
            } catch (_: Exception) {}
        }
        cropView = null
    }

    private fun updateFloatingWindowSize(isExpanded: Boolean) {
        val view = floatingView ?: return
        val params = floatingParams ?: return
        val density = resources.displayMetrics.density

        if (isExpanded) {
            params.width = (expandedWidthDp * density).toInt()
            params.height = (expandedHeightDp * density).toInt()
        } else {
            params.width = (collapsedSizeDp * density).toInt()
            params.height = (collapsedSizeDp * density).toInt()
        }

        try {
            windowManager?.updateViewLayout(view, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun captureScreen(
        onSuccess: (Bitmap) -> Unit,
        onError: (String) -> Unit
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            onError("Screenshot API requires Android 11 (API 30) or higher.")
            return
        }

        val normRect = cropView?.getCropRectNormalized()

        // Temporarily hide overlays before capture to avoid UI artifacts
        cropView?.hideCropUi()
        floatingView?.visibility = View.INVISIBLE

        serviceScope.launch {
            delay(150) // Wait for display buffer update

            takeScreenshot(
                Display.DEFAULT_DISPLAY,
                executor,
                object : TakeScreenshotCallback {
                    override fun onSuccess(screenshotResult: ScreenshotResult) {
                        try {
                            val buffer = screenshotResult.hardwareBuffer
                            val colorSpace = screenshotResult.colorSpace
                            val hwBitmap = Bitmap.wrapHardwareBuffer(buffer, colorSpace)
                            val softwareBitmap = hwBitmap?.copy(Bitmap.Config.ARGB_8888, true)
                            buffer.close()

                            serviceScope.launch(Dispatchers.Main) {
                                cropView?.showCropUi()
                                floatingView?.visibility = View.VISIBLE

                                if (softwareBitmap != null) {
                                    val finalBmp = if (normRect != null && normRect.width() > 0f && normRect.height() > 0f) {
                                        val cropL = (normRect.left * softwareBitmap.width).toInt().coerceIn(0, softwareBitmap.width - 1)
                                        val cropT = (normRect.top * softwareBitmap.height).toInt().coerceIn(0, softwareBitmap.height - 1)
                                        val cropR = (normRect.right * softwareBitmap.width).toInt().coerceIn(cropL + 1, softwareBitmap.width)
                                        val cropB = (normRect.bottom * softwareBitmap.height).toInt().coerceIn(cropT + 1, softwareBitmap.height)

                                        val cropped = Bitmap.createBitmap(softwareBitmap, cropL, cropT, cropR - cropL, cropB - cropT)
                                        softwareBitmap.recycle()
                                        cropped
                                    } else {
                                        softwareBitmap
                                    }

                                    onSuccess(finalBmp)
                                } else {
                                    onError("Failed to convert hardware screenshot buffer.")
                                }
                            }
                        } catch (e: Exception) {
                            serviceScope.launch(Dispatchers.Main) {
                                cropView?.showCropUi()
                                floatingView?.visibility = View.VISIBLE
                                onError("Screenshot error: ${e.localizedMessage}")
                            }
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        serviceScope.launch(Dispatchers.Main) {
                            cropView?.showCropUi()
                            floatingView?.visibility = View.VISIBLE

                            val msg = when (errorCode) {
                                ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT -> "Capture request sent too fast. Please increase delay."
                                ERROR_TAKE_SCREENSHOT_INVALID_DISPLAY -> "Invalid display ID."
                                ERROR_TAKE_SCREENSHOT_NO_ACCESSIBILITY_ACCESS -> "Accessibility screenshot permission unavailable."
                                ERROR_TAKE_SCREENSHOT_INVALID_WINDOW -> "Cannot capture DRM / secure protected content."
                                else -> "Failed to capture screenshot (Error $errorCode)."
                            }
                            onError(msg)
                        }
                    }
                }
            )
        }
    }

    fun dispatchTap(x: Float, y: Float, onComplete: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            onComplete()
            return
        }

        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        val gesture = GestureDescription.Builder().addStroke(stroke).build()

        dispatchGesture(gesture, object : GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                onComplete()
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                onComplete()
            }
        }, null)
    }
}