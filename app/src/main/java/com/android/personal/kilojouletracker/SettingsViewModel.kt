package com.android.personal.kilojouletracker

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel()
{
    var kilojouleGoalText by mutableStateOf("0")
    var fatGoalText by mutableStateOf("0")
    var carbohydrateGoalText by mutableStateOf("0")
    var proteinGoalText by mutableStateOf("0")
}