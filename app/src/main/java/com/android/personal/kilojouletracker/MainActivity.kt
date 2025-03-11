package com.android.personal.kilojouletracker

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.android.personal.kilojouletracker.ui.theme.CalorieTrackerTheme

class MainActivity : ComponentActivity()
{
    val logMealViewModel: LogMealViewModel by viewModels()
    val settingsViewModel: SettingsViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent()
        {
            CalorieTrackerTheme()
            {
                Scaffold(topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name), style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)) }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)) }, content = { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues))
                    {
                        NavigationScreen(logMealViewModel, settingsViewModel)
                    }
                })
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, caller: ComponentCaller)
    {
        super.onActivityResult(requestCode, resultCode, data, caller)

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            val capturedImage = data?.extras?.getParcelable("data", Bitmap::class.java) as Bitmap

            setContent()
            {
                CalorieTrackerTheme()
                {
                    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name), style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)) }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)) }, content = { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues))
                        {
                            NavigationScreenForCameraResult(logMealViewModel = logMealViewModel, settingsViewModel = settingsViewModel)
                        }
                    })
                }
            }
        }
    }

    companion object
    {
        const val CAMERA_REQUEST_CODE = 1
    }
}