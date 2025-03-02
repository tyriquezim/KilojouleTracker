package com.android.personal.kilojouletracker.model

class Meal
{
    var mealId: Int = totalMealsCreated
    lateinit var mealName: String
    lateinit var mealType: String
    var servingSize = 0.0
    var numKilojoules = 0.0

    constructor(mealName: String, mealType: String, servingSize: Double, numKilojoules: Double)
    {
        this.mealName = mealName
        this.mealType = mealType
        this.servingSize = servingSize
        this.numKilojoules = numKilojoules

        ++totalMealsCreated
    }

    companion object
    {
        var totalMealsCreated: Int = 0
    }
}