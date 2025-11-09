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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.FileProvider
import com.android.personal.kilojouletracker.model.MealPhoto
import java.io.File
import java.util.Date

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
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture())
    {
        wasPhotoTaken: Boolean -> if(!wasPhotoTaken) currentMealPhoto =  null
    }

    var currentMealPhoto: MealPhoto? = null

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
                        NavigationScreen(logMealViewModel, settingsViewModel, ::requestCameraPermission, ::launchCamera)
                    }
                })
            }
        }
    }

    //From Kilo Loco on Youtube
    private fun requestCameraPermission(): Boolean
    {
        var hasPermissionBeenGranted = false
        when
        {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED ->
                {
                    Log.d("requestCameraPermission", "Previously granted")
                    hasPermissionBeenGranted = true
                }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)  ->
                {
                    Log.d("requestCameraPermission", "Show camera permission dialog")
                    hasPermissionBeenGranted = true
                }
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
        return hasPermissionBeenGranted
    }

    private fun launchCamera()
    {
        val photoName: String = "IMG_${Date()}.jpg"
        val photoFile = File(this.applicationContext.filesDir, photoName)
        val photoUri = FileProvider.getUriForFile(this, "com.android.personal.kilojouletracker.fileprovider", photoFile)
        currentMealPhoto = MealPhoto("", photoName)

        cameraLauncher.launch(photoUri)
    }
}