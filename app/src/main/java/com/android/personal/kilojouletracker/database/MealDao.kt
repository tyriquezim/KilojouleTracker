package com.android.personal.kilojouletracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.personal.kilojouletracker.model.Meal

@Dao
interface MealDao
{
    @Insert
    suspend fun insertMeal(meal: Meal)

    @Query("DELETE FROM Meal WHERE mealId = :targetMealId")
    suspend fun deleteMeal(targetMealId: Int)

    @Query("UPDATE Meal SET mealName = :newMealName, servingWeight = :newServingWeight, numKilojoules = :newNumKilojoules, fatWeight = :newFatWeight, carbohydrateWeight = :newCarbohydrateWeight, proteinWeight = :newProteinWeight WHERE mealId = :targetMealId")
    suspend fun updateMeal(targetMealId: Int, newMealName: String, newServingWeight: Double, newNumKilojoules: Double, newFatWeight: Double, newCarbohydrateWeight: Double, newProteinWeight: Double)

    @Query("SELECT * FROM Meal WHERE mealId = :targetMealId")
    suspend fun getMeal(targetMealId: Int): Meal

    @Query("SELECT * FROM Meal")
    suspend fun getMeals(): List<Meal>

    @Query("SELECT SUM(numKilojoules) AS totalKilojoules FROM Meal")
    suspend fun getTotalKilojoules(): Double

    @Query("SELECT SUM(fatWeight) AS totalFatWeight FROM Meal")
    suspend fun getTotalFatWeight(): Double

    @Query("SELECT SUM(carbohydrateWeight) AS totalCarbohydrateWeight FROM Meal")
    suspend fun getTotalCarbohydrateWeight(): Double

    @Query("SELECT SUM(proteinWeight) AS totalProteinWeight FROM Meal")
    suspend fun getTotalProteinWeight(): Double
}