package com.example.listingapp

import com.example.listingapp.util.Helper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Test

import org.junit.Assert.assertEquals
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    lateinit var helper: Helper

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Before
    fun setUp() {
        helper = Helper()
        println("Before every test case")
    }

    @After
    fun tearDown() {
        println("After every test case")
    }

    @Test
    fun isPalindromeAssertTrue() {
        //Arrange
        //Act
        val result = helper.palindrome("level")
        //Assert
        assertEquals(true, result)
        println(result)
    }

    @Test
    fun `calling suspend functions in parallel using launch`() = runBlocking {
        println("Test Started ${Thread.currentThread().name}")
        launch {
            threeSecDelay()
        }
        launch {
            twoSecDelay()
        }
        println("Test Ended ${Thread.currentThread().name}")
    }


    private suspend fun threeSecDelay(): Boolean {
        println("Start threeSecDelay ${Thread.currentThread().name}")
        delay(3000)
        println("End threeSecDelay")
        return true
    }

    private suspend fun twoSecDelay(): Boolean {
        println("Start twoSecDelay ${Thread.currentThread().name}")
        delay(2000)
        println("End twoSecDelay")
        return true
    }


}