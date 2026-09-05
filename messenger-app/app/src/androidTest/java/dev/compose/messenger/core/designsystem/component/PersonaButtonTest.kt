package dev.compose.messenger.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class PersonaButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun click_invokesOnClick_whenEnabled() {
        var clicked = false
        composeRule.setContent {
            PersonaButton(text = "send", onClick = { clicked = true })
        }

        composeRule.onNodeWithText("SEND").assertIsDisplayed().performClick()

        assertTrue(clicked)
    }

    @Test
    fun click_doesNothing_whenDisabled() {
        var clicked = false
        composeRule.setContent {
            PersonaButton(text = "send", onClick = { clicked = true }, enabled = false)
        }

        composeRule.onNodeWithText("SEND").performClick()

        assertFalse(clicked)
    }

    @Test
    fun label_isUppercased() {
        composeRule.setContent {
            PersonaButton(text = "send", onClick = {})
        }

        composeRule.onNodeWithText("SEND").assertIsDisplayed()
    }
}
