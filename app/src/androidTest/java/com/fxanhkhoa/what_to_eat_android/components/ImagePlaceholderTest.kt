package com.fxanhkhoa.what_to_eat_android.components

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented UI tests for [ImagePlaceholder].
 *
 * Run with: ./gradlew connectedAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class ImagePlaceholderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // -------------------------------------------------------------------------
    // Renders without crashing
    // -------------------------------------------------------------------------

    @Test
    fun imagePlaceholder_rendersWithoutCrash() {
        composeTestRule.setContent {
            ImagePlaceholder()
        }
        // If no exception is thrown the composable inflated successfully.
    }

    // -------------------------------------------------------------------------
    // Restaurant icon is visible
    // -------------------------------------------------------------------------

    @Test
    fun imagePlaceholder_showsRestaurantIcon() {
        composeTestRule.setContent {
            ImagePlaceholder()
        }

        composeTestRule
            .onNodeWithContentDescription("No image")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Accepts a custom Modifier (size) without crashing
    // -------------------------------------------------------------------------

    @Test
    fun imagePlaceholder_acceptsCustomModifier() {
        composeTestRule.setContent {
            ImagePlaceholder(modifier = Modifier.size(120.dp))
        }

        composeTestRule
            .onNodeWithContentDescription("No image")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Renders correctly with a very small size
    // -------------------------------------------------------------------------

    @Test
    fun imagePlaceholder_rendersWithSmallSize() {
        composeTestRule.setContent {
            ImagePlaceholder(modifier = Modifier.size(24.dp))
        }

        composeTestRule
            .onNodeWithContentDescription("No image")
            .assertIsDisplayed()
    }

    // -------------------------------------------------------------------------
    // Renders correctly with a large size
    // -------------------------------------------------------------------------

    @Test
    fun imagePlaceholder_rendersWithLargeSize() {
        composeTestRule.setContent {
            ImagePlaceholder(modifier = Modifier.size(300.dp))
        }

        composeTestRule
            .onNodeWithContentDescription("No image")
            .assertIsDisplayed()
    }
}

