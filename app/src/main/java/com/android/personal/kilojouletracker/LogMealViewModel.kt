package com.android.personal.kilojouletracker

import androidx.lifecycle.ViewModel

class LogMealViewModel: ViewModel()
{
    var mealNameText: String = ""
    var mealTypeText: String = ""
    var servingSizeText: Double = 0.0
    var numCaloriesText: Double = 0.0
}