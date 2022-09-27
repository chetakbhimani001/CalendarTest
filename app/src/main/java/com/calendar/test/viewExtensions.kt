package com.calendar.test

import android.view.View

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun Float.roundToFloat() : Float {
    return "%.${2}f".format(this).toFloat()
}

fun Float.roundToInt() : Float {
    return "%.${0}f".format(this).toFloat()
}

fun Double.roundTo() : Double {
    return "%.${2}f".format(this).toDouble()
}