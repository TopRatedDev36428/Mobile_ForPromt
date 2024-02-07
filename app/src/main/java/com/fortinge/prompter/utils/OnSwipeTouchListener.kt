package com.fortinge.prompter.utils

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import kotlin.math.abs
internal open class OnSwipeTouchListener(c: Context?) :
        OnTouchListener {
    private val gestureDetector: GestureDetector
    private val scaleDetector: ScaleGestureDetector
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(motionEvent)
        scaleDetector.onTouchEvent(motionEvent)
        return true
    }
    private inner class GestureScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (detector != null) {
                scaleFactor = detector!!.scaleFactor
//                println ("previous= "+ detector!!.previousSpan)
                println(" nowspan  " + scaleFactor)


                println(scaleFactor)
            }
            onScale()
            return super.onScale(detector)
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            if (detector != null) {
                beginScaleFactor = detector.scaleFactor
                println("begin: $beginScaleFactor")
            }

            onScaleBegin()
            return super.onScaleBegin(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            onScaleEnd()
            super.onScaleEnd(detector)
        }
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD: Int = 100
        private val SWIPE_VELOCITY_THRESHOLD: Int = 100

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onSingleTapUp()
            return super.onSingleTapUp(e)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onSingleTapConfirmed()
            return super.onSingleTapConfirmed(e)
        }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }
        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }
        override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
        ): Boolean {
            try {
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x
                if (abs(diffX) > abs(diffY)) {
                    if (abs(diffX) > SWIPE_THRESHOLD && abs(
                                    velocityX
                            ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        else {
                            onSwipeLeft()
                        }
                    }
                }
                else {
                    if (abs(diffY) > SWIPE_THRESHOLD && abs(
                                    velocityY
                            ) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffY < 0) {
                            onSwipeUp(diffY)

                        }
                        else {
                            onSwipeDown(diffY)
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }

    }
    open var scaleFactor = 1f
    open var beginScaleFactor = 1f
    open fun onScaleBegin() {}
    open fun onScale() {}
    open fun onScaleEnd() {}
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp(diffY: Float) {}
    open fun onSwipeDown(diffY: Float) {}
    open fun onClick() {}
    open fun onDoubleClick() {}
    open fun onSingleTapUp() {}
    open fun onSingleTapConfirmed() {}
    private fun onLongClick() {}
    init {
        gestureDetector = GestureDetector(c, GestureListener())
        scaleDetector = c?.let { ScaleGestureDetector(it,GestureScaleListener() ) }!!
    }


}