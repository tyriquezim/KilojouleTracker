package com.android.personal.kilojouletracker.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.uuid.Uuid

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
@Entity
class Exercise(val exerciseType: String, val exerciseDuration: Double)
{
    @PrimaryKey var exerciseId = Uuid.random().toString()

    init
    {
        if(exerciseType != ANAEROBIC_EXERCISE_STRING && exerciseType != AEROBIC_EXERCISE_STRING)
        {
            throw IllegalArgumentException("Invalid Exercise Type")
        }
    }

    companion object
    {
        const val ANAEROBIC_EXERCISE_STRING = "Anaerobic"
        const val AEROBIC_EXERCISE_STRING = "Aerobic"

    }
}