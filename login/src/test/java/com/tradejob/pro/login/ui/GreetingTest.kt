package com.tradejob.pro.login.ui

import com.tradejob.pro.login.R
import org.junit.Assert.assertEquals
import org.junit.Test

class GreetingTest {

    @Test
    fun `manana de 6 a 13`() {
        assertEquals(R.string.greeting_morning, greetingForHour(6))
        assertEquals(R.string.greeting_morning, greetingForHour(9))
        assertEquals(R.string.greeting_morning, greetingForHour(13))
    }

    @Test
    fun `tarde de 14 a 20`() {
        assertEquals(R.string.greeting_afternoon, greetingForHour(14))
        assertEquals(R.string.greeting_afternoon, greetingForHour(17))
        assertEquals(R.string.greeting_afternoon, greetingForHour(20))
    }

    @Test
    fun `noche de 21 a 5`() {
        assertEquals(R.string.greeting_night, greetingForHour(21))
        assertEquals(R.string.greeting_night, greetingForHour(0))
        assertEquals(R.string.greeting_night, greetingForHour(5))
    }
}
