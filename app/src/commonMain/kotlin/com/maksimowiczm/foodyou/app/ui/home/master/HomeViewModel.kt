package com.maksimowiczm.foodyou.app.ui.home.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maksimowiczm.foodyou.common.domain.userpreferences.UserPreferencesRepository
import com.maksimowiczm.foodyou.fooddiary.domain.entity.FoodDiaryEntry
import com.maksimowiczm.foodyou.fooddiary.domain.usecase.ObserveDiaryMealsUseCase
import com.maksimowiczm.foodyou.settings.domain.entity.Settings
import foodyou.app.generated.resources.Res
import foodyou.app.generated.resources.nutriment_carbohydrates
import foodyou.app.generated.resources.nutriment_fats
import foodyou.app.generated.resources.nutriment_proteins
import foodyou.app.generated.resources.unit_gram_short
import foodyou.app.generated.resources.unit_kcal
import foodyou.app.generated.resources.unit_milliliter_short
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.getString

internal class HomeViewModel(
    settingsRepository: UserPreferencesRepository<Settings>,
    private val observeDiaryMealsUseCase: ObserveDiaryMealsUseCase,
) : ViewModel() {

    private val _homeOrder = settingsRepository.observe().map { it.homeCardOrder }
    val homeOrder =
        _homeOrder.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(2_000),
            initialValue = runBlocking { _homeOrder.first() },
        )

    /**
     * Builds a plain-text summary of [date]'s logged meals, one section per non-empty meal (e.g.
     * "Breakfast") followed by its entries (e.g. "- Oatmeal (50 g, 100 kcal, Fats: 3 g, Proteins:
     * 5 g, Carbohydrates: 5 g)"). Meals without any entries are omitted. Returns an empty string
     * if nothing was logged for [date].
     */
    suspend fun buildShareText(date: LocalDate): String {
        val meals = observeDiaryMealsUseCase.observe(date).first()

        val kcal = getString(Res.string.unit_kcal)
        val gram = getString(Res.string.unit_gram_short)
        val milliliter = getString(Res.string.unit_milliliter_short)
        val fats = getString(Res.string.nutriment_fats)
        val proteins = getString(Res.string.nutriment_proteins)
        val carbohydrates = getString(Res.string.nutriment_carbohydrates)

        return buildString {
                meals
                    .filter { it.entries.isNotEmpty() }
                    .forEach { diaryMeal ->
                        appendLine(diaryMeal.meal.name)

                        diaryMeal.entries.forEach { entry ->
                            val parts = mutableListOf<String>()

                            val foodEntry = entry as? FoodDiaryEntry
                            if (foodEntry != null) {
                                val suffix = if (foodEntry.food.isLiquid) milliliter else gram
                                parts += "${foodEntry.weight.roundToInt()} $suffix"
                            }

                            entry.nutritionFacts.energy.value?.roundToInt()?.let {
                                parts += "$it $kcal"
                            }
                            entry.nutritionFacts.fats.value?.roundToInt()?.let {
                                parts += "$fats: $it $gram"
                            }
                            entry.nutritionFacts.proteins.value?.roundToInt()?.let {
                                parts += "$proteins: $it $gram"
                            }
                            entry.nutritionFacts.carbohydrates.value?.roundToInt()?.let {
                                parts += "$carbohydrates: $it $gram"
                            }

                            appendLine("- ${entry.name} (${parts.joinToString(", ")})")
                        }

                        appendLine()
                    }
            }
            .trim()
    }
}
