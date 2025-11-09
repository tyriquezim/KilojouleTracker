package com.android.personal.kilojouletracker.model

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class MealPhoto
{
    @PrimaryKey var mealOwnerId: String
    val photoFileName: String

    constructor(mealOwnerId: String = "", photoFileName: String)
    {
        this.mealOwnerId = mealOwnerId
        this.photoFileName = photoFileName
    }
}