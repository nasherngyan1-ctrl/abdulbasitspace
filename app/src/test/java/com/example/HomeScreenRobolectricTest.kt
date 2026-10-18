package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class HomeScreenRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_rendersBrandTitleAndTools() {
        var navigatedTo = ""
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    isOnline = true,
                    onNavigate = { navigatedTo = it }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Brand Title node exists
        composeTestRule.onAllNodesWithText("ABDULBASIT SPACE")[0].assertIsDisplayed()

        // Verify Kurdish Section Header
        composeTestRule.onNodeWithText("بەشە سەرەکییەکان (١٢ ئامراز)").assertIsDisplayed()

        // Test Click on AI Chat Tool
        composeTestRule.onNodeWithTag("tool_card_ai_chat").performClick()
        composeTestRule.waitForIdle()
        assertEquals("ai_chat", navigatedTo)

        // Test Click on Image Editor Tool
        composeTestRule.onNodeWithTag("tool_card_image_editor").performClick()
        composeTestRule.waitForIdle()
        assertEquals("image_editor", navigatedTo)
    }

    @Test
    fun homeScreen_offlineModeDisplaysOfflineBadge() {
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeScreen(
                    isOnline = false,
                    onNavigate = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify Offline Badge exists
        composeTestRule.onAllNodesWithText("ئۆفڵاین")[0].assertIsDisplayed()
    }
}
