package com.android.personal.kilojouletracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.android.personal.kilojouletracker.model.Meal
import com.android.personal.kilojouletracker.model.MealPhoto

@Dao
interface MealDao
{
    @Insert
    suspend fun insertMeal(meal: Meal)

    @Query("DELETE FROM Meal WHERE mealId = :targetMealId")
    suspend fun deleteMeal(targetMealId: String)

    @Query("UPDATE Meal SET mealName = :newMealName, servingWeight = :newServingWeight, numKilojoules = :newNumKilojoules, fatWeight = :newFatWeight, carbohydrateWeight = :newCarbohydrateWeight, proteinWeight = :newProteinWeight WHERE mealId = :targetMealId")
    suspend fun updateMeal(targetMealId: String, newMealName: String, newServingWeight: Double, newNumKilojoules: Double, newFatWeight: Double, newCarbohydrateWeight: Double, newProteinWeight: Double)

    @Query("SELECT * FROM Meal WHERE mealId = :targetMealId")
    suspend fun getMeal(targetMealId: String): Meal

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

    @Insert
    suspend fun insertMealPhoto(mealPhoto: MealPhoto)

    @Query("DELETE FROM MealPhoto WHERE mealOwnerId = :targetMealPhotoOwnerId")
    suspend fun deleteMealPhoto(targetMealPhotoOwnerId: String)

    @Query("SELECT * FROM MealPhoto WHERE mealOwnerId = :targetMealPhotoOwnerId")
    suspend fun getMealPhoto(targetMealPhotoOwnerId: String): MealPhoto

    @Query("SELECT * FROM MealPhoto")
    suspend fun getMealPhotos(): List<MealPhoto>
}