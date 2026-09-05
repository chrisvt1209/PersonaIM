package dev.compose.messenger.core.designsystem.component

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PersonaTextFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun placeholder_isShown_whenValueEmpty() {
        composeRule.setContent {
            PersonaTextField(value = "", onValueChange = {}, placeholder = "Type here")
        }

        composeRule.onNodeWithText("Type here").assertIsDisplayed()
    }

    @Test
    fun placeholder_isHidden_whenValueNotEmpty() {
        composeRule.setContent {
            PersonaTextField(value = "hi", onValueChange = {}, placeholder = "Type here")
        }

        composeRule.onNodeWithText("Type here").assertDoesNotExist()
    }

    @Test
    fun typing_reportsNewValue_throughOnValueChange() {
        var lastValue = ""
        composeRule.setContent {
            var text by mutableStateOf("")
            PersonaTextField(
                value = text,
                onValueChange = {
                    text = it
                    lastValue = it
                },
                placeholder = "Type here"
            )
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("Joker")

        assertEquals("Joker", lastValue)
    }
}
