package com.android.personal.calorietracker.model

class Meal
{
    var mealId: Int = totalMealsCreated
    lateinit var mealName: String
    lateinit var mealType: String
    var servingSize = 0.0
    var numCalories = 0.0

    constructor(mealName: String, mealType: String, servingSize: Double, numCalories: Double)
    {
        this.mealName = mealName
        this.mealType = mealType
        this.servingSize = servingSize
        this.numCalories = numCalories

        ++totalMealsCreated
    }

    companion object
    {
        var totalMealsCreated: Int = 0
    }
}