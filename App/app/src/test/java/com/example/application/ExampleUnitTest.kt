package com.example.application

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun failing_test1() {
        assertEquals(4, 1)
    }

    @Test
    fun failing_test2() {
        assertEquals(4, 3)
    }

}
