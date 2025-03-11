package com.android.personal.kilojouletracker

import android.app.Activity
import android.app.ComponentCaller
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.personal.kilojouletracker.ui.theme.CalorieTrackerTheme
import android.Manifest

class MainActivity : ComponentActivity()
{
    private val logMealViewModel: LogMealViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    {
        isGranted: Boolean ->
        if(isGranted)
        {
            Log.d("Permission Launcher", "Granted")
        }
        else
        {
            Log.d("Permission Launcher", "Denied")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        requestCameraPermission()
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

        Log.d("onActivityResult Called", "Just before camera if statement")

        if(requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK)
        {
            Log.d("onActivityResult Called", "In camera if statement")
            val capturedImage = data?.extras?.getParcelable("data", Bitmap::class.java) as Bitmap

            setContent()
            {
                CalorieTrackerTheme()
                {
                    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(id = R.string.app_name), style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold, color = Color.White)) }, colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primary)) }, content = { paddingValues ->
                        Box(modifier = Modifier.padding(paddingValues))
                        {
                            NavigationScreenForCameraResult(capturedImage, logMealViewModel = logMealViewModel, settingsViewModel = settingsViewModel)
                        }
                    })
                }
            }
        }
    }

    //From Kilo Loco on Youtube
    private fun requestCameraPermission()
    {
        when
        {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {Log.d("requestCameraPermission", "Previously granted")}
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)  -> {Log.d("requestCameraPermission", "Show camera permission dialog")}
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    companion object
    {
        const val CAMERA_REQUEST_CODE = 1
    }
}