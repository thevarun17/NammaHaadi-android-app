package com.nammahaadi.app.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.nammahaadi.app.data.model.Alert
import com.nammahaadi.app.viewmodel.AppViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class AlertsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<AppViewModel>(relaxed = true)

    @Test
    fun alertsScreen_showsEmptyState_whenNoAlerts() {
        every { viewModel.alerts } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            AlertsScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("No active alerts").assertIsDisplayed()
        composeTestRule.onNodeWithText("Roads are clear!").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_showsAlerts_whenAlertsPresent() {
        val alerts = listOf(
            Alert(id = "1", title = "Pothole Alert", message = "Big pothole near Main St", severity = "DANGER", area = "Main St"),
            Alert(id = "2", title = "Road Work", message = "Construction ahead", severity = "WARNING", area = "2nd Ave")
        )
        every { viewModel.alerts } returns MutableStateFlow(alerts)

        composeTestRule.setContent {
            AlertsScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Pothole Alert").assertIsDisplayed()
        composeTestRule.onNodeWithText("Big pothole near Main St").assertIsDisplayed()
        composeTestRule.onNodeWithText("Road Work").assertIsDisplayed()
    }

    @Test
    fun alertsScreen_opensAddDialog_onFabClick() {
        every { viewModel.alerts } returns MutableStateFlow(emptyList())
        every { viewModel.currentUser } returns MutableStateFlow(null)

        composeTestRule.setContent {
            AlertsScreen(viewModel = viewModel)
        }

        // Open dialog
        composeTestRule.onNodeWithContentDescription("Add Alert").performClick()

        // Verify dialog is shown
        composeTestRule.onNodeWithText("Broadcast Alert").assertIsDisplayed()
        composeTestRule.onNodeWithText("Title *").assertIsDisplayed()
    }
}
