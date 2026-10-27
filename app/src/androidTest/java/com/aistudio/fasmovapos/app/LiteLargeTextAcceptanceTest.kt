package com.aistudio.fasmovapos.app

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.aistudio.fasmovapos.app.data.db.FasMovaPOSDatabase
import com.aistudio.fasmovapos.app.data.repository.FasMovaPOSRepository
import com.aistudio.fasmovapos.app.ui.language.AppLanguage
import com.aistudio.fasmovapos.app.ui.language.LanguageSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Run after normal acceptance, with the host applying and later restoring the 360dp / 1.5x profile. */
@RunWith(AndroidJUnit4::class)
class LiteLargeTextAcceptanceTest {
  @get:Rule val rule = createEmptyComposeRule()
  private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

  private fun repository() = FasMovaPOSRepository(FasMovaPOSDatabase.getDatabase(context))

  private fun waitFor(tag: String) =
    rule.waitUntil(30_000) {
      try {
        rule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
      } catch (_: IllegalStateException) {
        false
      }
    }

  private fun capture(name: String) {
    rule.waitForIdle()
    val directory = File(context.getExternalFilesDir(null), "review-evidence").apply { mkdirs() }
    InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?.let { bitmap ->
      File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
      bitmap.recycle()
    }
  }

  @Test
  fun quantityCustomerAndReportsRemainUsableWithLargeTextAndKeyboard() {
    val original =
      runBlocking {
        Triple(
          repository().sales.first().size,
          repository().customers.first().size,
          repository().customerPayments.first().size,
        )
      }
    assertEquals(
      "FasMova Lite review",
      runBlocking {
        repository()
          .branches
          .first()
          .single()
          .name
      },
    )
    LanguageSettings.set(context, AppLanguage.ENGLISH)
    val scenario = ActivityScenario.launch(MainActivity::class.java)
    waitFor("simple_nav_pos")
    rule.onNodeWithTag("simple_nav_inventory").performClick()
    rule.onNodeWithText("Beans").performScrollTo().performClick()
    waitFor("simple_edit_quantity")
    rule.onNodeWithTag("simple_edit_quantity").performScrollTo().performClick()
    capture("large-stock-keyboard")
    closeSoftKeyboard()
    rule.onNodeWithText("Cancel").performScrollTo().performClick()
    rule.onNodeWithTag("simple_nav_pos").performClick()
    rule.onNodeWithTag("simple_product_review-fat").performScrollTo().performClick()
    rule.onNodeWithTag("lite_quantity_input").performScrollTo().performTextReplacement("1.75")
    rule.onNodeWithTag("lite_quantity_input").performClick()
    capture("large-quantity-keyboard")
    closeSoftKeyboard()
    rule.onNodeWithTag("lite_add_quantity").assertIsDisplayed()
    rule.onNodeWithText("Cancel").performClick()
    rule.onNodeWithTag("simple_nav_more").performClick()
    rule.onNodeWithTag("simple_more_customers").performClick()
    rule.onNodeWithTag("add_customer_button").performClick()
    rule.onNodeWithTag("customer_name").performScrollTo().performTextReplacement("Temporary unsubmitted name")
    rule.onNodeWithTag("customer_name").performClick()
    capture("large-customer-keyboard")
    closeSoftKeyboard()
    rule.onNodeWithText("Cancel").performScrollTo().performClick()
    rule.onNodeWithTag("simple_nav_today").performClick()
    rule.onNodeWithTag("lite_report_sales").assertIsDisplayed()
    capture("large-report-english")
    rule.onNodeWithText("SW").performClick()
    waitFor("lite_report_sales")
    capture("large-report-swahili")
    rule.onNodeWithText("EN").performClick()
    waitFor("lite_report_sales")
    assertEquals(
      original,
      runBlocking {
        Triple(repository().sales.first().size, repository().customers.first().size, repository().customerPayments.first().size)
      },
    )
    scenario.close()
  }
}
