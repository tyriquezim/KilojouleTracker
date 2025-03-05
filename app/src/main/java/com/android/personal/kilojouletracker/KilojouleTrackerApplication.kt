package com.android.personal.kilojouletracker

import android.app.Application

class KilojouleTrackerApplication: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        KilojouleTrackerRepository.initialise(this)
    }
}