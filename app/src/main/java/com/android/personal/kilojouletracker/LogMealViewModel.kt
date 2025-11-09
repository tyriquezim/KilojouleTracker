package com.android.personal.kilojouletracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.android.personal.kilojouletracker.model.MealPhoto

class LogMealViewModel: ViewModel()
{
    var mealNameText: String by mutableStateOf("")
    var servingWeightText: String by mutableStateOf("")
    var numKilojoulesText: String by mutableStateOf("")
    var fatWeightText: String by mutableStateOf("")
    var carbohydrateWeightText: String by mutableStateOf("")
    var proteinWeightText: String by mutableStateOf("")
    var currentMealPhoto: MealPhoto? by mutableStateOf(null)
    var logMealScreenWidth by mutableStateOf(0)
    var logMealScreenHeight by mutableStateOf(0)
}