package com.android.personal.kilojouletracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.personal.kilojouletracker.model.Meal
import com.android.personal.kilojouletracker.model.MealPhoto

@Database(entities = [Meal::class, MealPhoto::class], version = 1)
abstract class KilojouleTrackerDatabase: RoomDatabase()
{
    abstract fun mealDao(): MealDao
}