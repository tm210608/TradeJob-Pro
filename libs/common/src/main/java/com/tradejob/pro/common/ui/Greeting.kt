package com.tradejob.pro.common.ui

import androidx.annotation.StringRes
import com.tradejob.pro.common.R

@StringRes
fun greetingForHour(hour: Int): Int = when (hour) {
    in 6..13 -> R.string.greeting_morning
    in 14..20 -> R.string.greeting_afternoon
    else -> R.string.greeting_night
}
