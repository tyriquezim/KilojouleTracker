package com.android.personal.kilojouletracker

import android.app.Application
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class KilojouleTrackerApplication: Application()
{
    override fun onCreate()
    {
        super.onCreate()
        KilojouleTrackerRepository.initialise(this)

        GlobalScope.launch()
        {
        }
    }
}