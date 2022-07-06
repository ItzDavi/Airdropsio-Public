package com.example.airdropsio

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import java.lang.Math.abs as mAbs

internal open class OnSwipeTouchListener(c: Context?) : OnTouchListener {
    private val gestureDetector: GestureDetector
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    private inner class GestureListener : SimpleOnGestureListener() {
        private val swipeThreshold: Int = 0
        private val swipeVelocityThreshold: Int = 100
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            onClick()
            return super.onSingleTapUp(e)
        }

        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return super.onDoubleTap(e)
        }

        override fun onLongPress(e: MotionEvent) {
            onLongClick()
            super.onLongPress(e)
        }

        override fun onFling(e1: MotionEvent, e2: MotionEvent, velX: Float, velY: Float): Boolean {
            try {
                // Calc swipes events x and y diffs
                val diffY = e2.y - e1.y
                val diffX = e2.x - e1.x

                //If the x swipe length is greater than the y swipe length
                if (mAbs(diffX) > mAbs(diffY)) {

                    // If the x swipe length is greater than the threshold and the
                    // x swipe velocity is greater than the velocity threshold
                    if (mAbs(diffX) > swipeThreshold && mAbs(velX) > swipeVelocityThreshold) {

                        // If the real diff is positive
                        // the swipe if to the right
                        if (diffX > 0) {
                            onSwipeRight()
                        }
                        // If not, the swipe is to the left
                        else {
                            onSwipeLeft()
                        }
                    }
                }
                // If the y swipe length is greater than the x swipe length
                else {

                    // If the
                    if (mAbs(diffY) > swipeThreshold && mAbs(velY) > swipeVelocityThreshold) {
                        if (diffY < 0) {
                            onSwipeUp()
                        }
                        else {
                            onSwipeDown()
                        }
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return false
        }
    }
    open fun onSwipeRight() {}
    open fun onSwipeLeft() {}
    open fun onSwipeUp() {}
    open fun onSwipeDown() {}
    private fun onClick() {}
    private fun onDoubleClick() {}
    private fun onLongClick() {}
    init {
        gestureDetector = GestureDetector(c, GestureListener())
    }
}