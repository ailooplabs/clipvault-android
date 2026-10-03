package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ClipVault", appName)
  }

  @Test
  fun `test clip type detection`() {
    assertEquals(com.example.data.ClipType.COLOR_HEX, com.example.data.ClipItem.detectType("#6366F1"))
    assertEquals(com.example.data.ClipType.URL, com.example.data.ClipItem.detectType("https://ailooplabs.com/clipvault.html"))
    assertEquals(com.example.data.ClipType.SECRET, com.example.data.ClipItem.detectType("const API_KEY = 'sk-12345'"))
    assertEquals(com.example.data.ClipType.CODE, com.example.data.ClipItem.detectType("git log --oneline -n 10"))
    assertEquals(com.example.data.ClipType.TEXT, com.example.data.ClipItem.detectType("Hello world!"))
  }
}
