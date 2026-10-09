package com.maksimowiczm.foodyou.app.testutil.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.maksimowiczm.foodyou.app.testutil.FoodYouComposeTest
import com.maksimowiczm.foodyou.common.compose.utility.TestTags
import com.maksimowiczm.foodyou.common.domain.food.NutrientValue
import com.maksimowiczm.foodyou.common.domain.food.NutritionFacts
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers the Home overflow "Share" action: it opens a bottom sheet with the full text, a Copy
 * button and a text box that both copy to the clipboard, and a Share button. An empty day shows a
 * snackbar instead of the sheet.
 */
class HomeShareSheetTest : FoodYouComposeTest() {

    private val suffix = System.currentTimeMillis() % 1_000_000
    private val mealName = "Share Meal $suffix"
    private val entryName = "Share Entry $suffix"

    private var mealId: Long = -1

    private val today by lazy { dateProvider.now().date }

    @Before
    fun seed() {
        mealId = createTestMeal(mealName)
    }

    @After
    fun cleanUp() {
        runBlocking { if (mealId != -1L) mealRepository.deleteMeal(mealId) }
    }

    private fun seedEntry() {
        runBlocking {
            manualDiaryEntryRepository.insert(
                name = entryName,
                mealId = mealId,
                date = today,
                nutritionFacts =
                    NutritionFacts(
                        energy = NutrientValue.from(111.0),
                        proteins = NutrientValue.from(11.0),
                        carbohydrates = NutrientValue.from(11.0),
                        fats = NutrientValue.from(1.0),
                    ),
                createdAt = dateProvider.now(),
            )
        }
    }

    private fun openShareSheet() {
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.HomeOverflowMenuButton).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TestTags.HomeShareMenuItem).performClick()
        composeRule.waitForIdle()
    }

    private val clipboard: ClipboardManager
        get() =
            InstrumentationRegistry.getInstrumentation()
                .targetContext
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private fun setClipboard(text: String) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            clipboard.setPrimaryClip(ClipData.newPlainText("test", text))
        }
    }

    private fun readClipboard(): String {
        var result = ""
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            result = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
        }
        return result
    }

    @Test
    fun share_opens_a_sheet_with_the_full_text_and_both_buttons() {
        seedEntry()
        openShareSheet()

        composeRule.onNodeWithTag(TestTags.HomeShareSheet).assertExists()
        composeRule.onNodeWithTag(TestTags.HomeShareSheetText).assertExists()
        composeRule.onNodeWithText(mealName, substring = true).assertExists()
        composeRule.onNodeWithText(entryName, substring = true).assertExists()
        composeRule.onNodeWithTag(TestTags.HomeShareSheetCopyButton).assertExists()
        composeRule.onNodeWithTag(TestTags.HomeShareSheetShareButton).assertExists()
    }

    @Test
    fun copy_button_copies_the_text_to_the_clipboard() {
        seedEntry()
        setClipboard("placeholder")
        openShareSheet()

        composeRule.onNodeWithTag(TestTags.HomeShareSheetCopyButton).performClick()
        composeRule.waitForIdle()

        val copied = readClipboard()
        assertTrue("Expected clipboard to contain the meal name, was: $copied", mealName in copied)
        assertTrue("Expected clipboard to contain the entry name, was: $copied", entryName in copied)
    }

    @Test
    fun tapping_the_text_box_copies_the_text_to_the_clipboard() {
        seedEntry()
        setClipboard("placeholder")
        openShareSheet()

        composeRule.onNodeWithTag(TestTags.HomeShareSheetText).performClick()
        composeRule.waitForIdle()

        val copied = readClipboard()
        assertTrue("Expected clipboard to contain the entry name, was: $copied", entryName in copied)
    }

    @Test
    fun share_on_a_day_without_entries_shows_a_snackbar_and_no_sheet() {
        openShareSheet()

        composeRule.onNodeWithTag(TestTags.HomeShareSheet).assertDoesNotExist()
        composeRule.onNodeWithText("No food found").assertExists()
    }
}
