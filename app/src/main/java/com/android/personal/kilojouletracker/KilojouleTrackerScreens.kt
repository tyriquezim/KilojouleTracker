package com.android.personal.kilojouletracker

import android.app.Activity
import android.app.LocalActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.personal.kilojouletracker.model.Meal
import com.android.personal.kilojouletracker.model.MealPhoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

const val HOME_SCREEN_ROUTE = "HomeScreen"
const val LOG_MEAL_SCREEN_ROUTE = "LogMealScreen"
const val VIEW_LOGGED_MEALS_SCREEN_ROUTE = "ViewLoggedMealsScreen"
const val DAILY_PROGRESS_SCREEN_ROUTE = "DailyProgressScreen"
const val SETTINGS_SCREEN_ROUTE = "Settings"

@Composable
fun NavigationScreen(logMealViewModel: LogMealViewModel, settingsViewModel: SettingsViewModel, cameraLaunchRequestFun: () -> Boolean, cameraLauncherFun: () -> Unit, getBitmap: (fileName: String, destWidth: Int, destHeight: Int) -> Bitmap)
{
    val navigationController: NavHostController = rememberNavController()

    NavHost(navController = navigationController, startDestination = HOME_SCREEN_ROUTE)
    {
        composable(HOME_SCREEN_ROUTE)
        {
            HomeScreen(navigationController = navigationController, Modifier.fillMaxSize())
        }
        composable(LOG_MEAL_SCREEN_ROUTE)
        {
            LogMealScreen(navigationController = navigationController, logMealViewModel = logMealViewModel, cameraLaunchRequestFun, cameraLauncherFun, getBitmap, modifier = Modifier.fillMaxSize().onGloballyPositioned()
            {
                    layoutCoordinates ->
                logMealViewModel.logMealScreenWidth = layoutCoordinates.size.width
                logMealViewModel.logMealScreenHeight = layoutCoordinates.size.height
            })
        }
        composable(VIEW_LOGGED_MEALS_SCREEN_ROUTE)
        {
            ViewLoggedMealsScreen(navigationController = navigationController, getBitmap, modifier = Modifier.fillMaxSize())
        }
        composable(DAILY_PROGRESS_SCREEN_ROUTE)
        {
            DailyProgressScreen(navigationController = navigationController, settingsViewModel = settingsViewModel)
        }
        composable(SETTINGS_SCREEN_ROUTE)
        {
            SettingsScreen(navigationController = navigationController, settingsMealViewModel = settingsViewModel, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun HomeScreen(navigationController: NavHostController, modifier: Modifier = Modifier)
{
    Box(modifier = modifier)
    {
        Column(modifier = Modifier.align(Alignment.Center))
        {
            Button(onClick = { navigationController.navigate(LOG_MEAL_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_lunch_dining_24), contentDescription = "Log Meal Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("Log Meal")
            }
            Button(onClick = { navigationController.navigate(VIEW_LOGGED_MEALS_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_list_24), contentDescription = "View Logged Meal Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("View Logged Meals")
            }
            Button(onClick = { navigationController.navigate(DAILY_PROGRESS_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_stars_24), contentDescription = "Daily Progress Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("View Daily Progress")
            }
            Button(onClick = { navigationController.navigate(SETTINGS_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Icon(painterResource(id = R.drawable.baseline_settings_24), contentDescription = "Settings Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("Settings")
            }
        }
    }
}

@Composable
fun LogMealScreen(navigationController: NavHostController, logMealViewModel: LogMealViewModel, cameraLaunchRequestFun: () -> Boolean, cameraLauncherFun: () -> Unit, getBitmap: (fileName: String, destWidth: Int, destHeight: Int) -> Bitmap, modifier: Modifier = Modifier)
{
    val context = LocalContext.current
    var manualMealLogging: Boolean by remember { mutableStateOf(false) }
    var firstMealHasBeenLogged: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier)
    {
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(20.dp))
        {
            TextField(value = logMealViewModel.mealNameText, label = { Text("Enter the Meal Name") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
            {
                text: String -> logMealViewModel.mealNameText = text
                manualMealLogging = false //So that it reverts back if the user decides to log a different meal after a failed request
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = logMealViewModel.servingWeightText, label = { Text("Enter the Serving Size (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
            {
                text -> logMealViewModel.servingWeightText = text
            }, modifier = Modifier.align(Alignment.CenterHorizontally))

            if(!manualMealLogging) //Initially it will try automatically fill this info out with info from the API
            {
                if(firstMealHasBeenLogged)
                {
                    Row(modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp))
                    {
                        Text("Kilojoules: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(logMealViewModel.numKilojoulesText + "kJ", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Fat: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(logMealViewModel.fatWeightText + "g", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Carbohydrate: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(logMealViewModel.carbohydrateWeightText + "g", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Protein: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(logMealViewModel.proteinWeightText + "g", fontSize = 20.sp)
                    }
                }
            }
            else //If the API request failed, it will need the user to manually enter the information
            {
                TextField(value = logMealViewModel.numKilojoulesText, label = { Text("Enter the number of Kilojoules") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> logMealViewModel.numKilojoulesText = text
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                TextField(value = logMealViewModel.fatWeightText, label = { Text("Enter the amount of Fat (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                { text ->
                    logMealViewModel.fatWeightText = text
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                TextField(value = logMealViewModel.carbohydrateWeightText, label = { Text("Enter the amount of Carbohydrates (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> logMealViewModel.carbohydrateWeightText = text
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                TextField(value = logMealViewModel.proteinWeightText, label = { Text("Enter the amount of Protein (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> logMealViewModel.proteinWeightText = text
                }, modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            if(logMealViewModel.currentMealPhoto != null)
            {
                Image(bitmap = getBitmap(logMealViewModel.currentMealPhoto!!.photoFileName, logMealViewModel.logMealScreenWidth, logMealViewModel.logMealScreenHeight).asImageBitmap(), contentDescription = "Captured Image", modifier = Modifier.clip(RoundedCornerShape(20.dp)))
            }
            Button(onClick =
            {
                var shouldCameraLaunch = cameraLaunchRequestFun()

                if(shouldCameraLaunch)
                {
                    cameraLauncherFun()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 20.dp, 0.dp, 0.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_photo_camera_24), contentDescription = "Photo Launch")
            }
        }
        Button(onClick =
        {
            GlobalScope.launch() //Global Scope because meal logging cannot be interrupted
            {
                withContext(Dispatchers.IO)
                {
                    var loggedMeal: Meal? = null

                    if(logMealViewModel.currentMealPhoto == null)
                    {
                        withContext(Dispatchers.Main)
                        {
                            Toast.makeText(context, "Please take a photo of your meal!", Toast.LENGTH_LONG).show()
                        }
                    }
                    else
                    {
                        if(!manualMealLogging)
                        {
                            try
                            {
                                KilojouleTrackerRepository.get().apiMutex.withLock()
                                {
                                    loggedMeal = KilojouleTrackerRepository.get().getMealFromAPI(logMealViewModel.mealNameText, logMealViewModel.servingWeightText.toDouble())
                                }

                                if(loggedMeal == null)
                                {
                                    manualMealLogging = true
                                    logMealViewModel.numKilojoulesText = ""
                                    logMealViewModel.fatWeightText = ""
                                    logMealViewModel.carbohydrateWeightText = ""
                                    logMealViewModel.proteinWeightText = ""

                                    withContext(Dispatchers.Main)
                                    {
                                        Toast.makeText(context, "Could not log meal. Enter the meal details manually.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                else
                                {
                                    KilojouleTrackerRepository.get().databaseMutex.withLock()
                                    {
                                        logMealViewModel.currentMealPhoto!!.mealOwnerId = loggedMeal!!.mealId
                                        KilojouleTrackerRepository.get().insertMealPhoto(logMealViewModel.currentMealPhoto!!)
                                        KilojouleTrackerRepository.get().insertMeal(loggedMeal!!)
                                        logMealViewModel.numKilojoulesText = String.format("%.2f", loggedMeal!!.numKilojoules)
                                        logMealViewModel.fatWeightText = loggedMeal!!.fatWeight.toString()
                                        logMealViewModel.carbohydrateWeightText = loggedMeal!!.carbohydrateWeight.toString()
                                        logMealViewModel.proteinWeightText = loggedMeal!!.proteinWeight.toString()
                                        firstMealHasBeenLogged = true
                                    }

                                    withContext(Dispatchers.Main)
                                    {
                                        Toast.makeText(context, "Logged!", Toast.LENGTH_LONG).show()
                                    }
                                    Log.d("Log Meal", "Successfully Logged Meal")
                                }
                            }
                            catch(e: NumberFormatException)
                            {
                                withContext(Dispatchers.Main)
                                {
                                    Toast.makeText(context, "The serving weight must be numeric!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        else
                        {
                            try
                            {
                                KilojouleTrackerRepository.get().databaseMutex.withLock()
                                {
                                    loggedMeal = Meal(logMealViewModel.mealNameText, logMealViewModel.servingWeightText.toDouble(), logMealViewModel.numKilojoulesText.toDouble(), logMealViewModel.fatWeightText.toDouble(), logMealViewModel.carbohydrateWeightText.toDouble(), logMealViewModel.proteinWeightText.toDouble())
                                    logMealViewModel.currentMealPhoto!!.mealOwnerId = loggedMeal!!.mealId
                                    KilojouleTrackerRepository.get().insertMealPhoto(logMealViewModel.currentMealPhoto!!)
                                    KilojouleTrackerRepository.get().insertMeal(loggedMeal!!)
                                    manualMealLogging = false
                                }
                                withContext(Dispatchers.Main)
                                {
                                    Toast.makeText(context, "Logged!", Toast.LENGTH_LONG).show()
                                }
                                Log.d("Log Meal", "Successfully Logged Meal")
                            }
                            catch(e: NumberFormatException)
                            {
                                withContext(Dispatchers.Main)
                                {
                                    Toast.makeText(context, "All values, excluding Meal Name, must be numeric!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        }, modifier = Modifier.align(Alignment.BottomCenter)
        )
        {
            Icon(painterResource(id = R.drawable.baseline_add_24), contentDescription = "Log Meal", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Log Meal")
        }
        Button(onClick = { navigationController.navigate(HOME_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }
}

@Composable
fun ViewLoggedMealsScreen(navigationController: NavHostController, getBitmap: (fileName: String, destWidth: Int, destHeight: Int) -> android.graphics.Bitmap, modifier: Modifier = Modifier)
{
    var loggedMeals: List<Meal> by remember { mutableStateOf(ArrayList()) }
    var loggedMealPhotos: List<MealPhoto> by remember {mutableStateOf(ArrayList())}

    Box(modifier = modifier)
    {
        if(loggedMeals.isNotEmpty() && loggedMealPhotos.isNotEmpty())
        {
            LazyColumn(modifier = modifier)
            {
                items(loggedMeals)
                {
                    meal ->
                    lateinit var correspondingMealPhoto: MealPhoto

                    for(loggedMealPhoto in loggedMealPhotos)
                    {
                        if(meal.mealId.equals(loggedMealPhoto.mealOwnerId))
                        {
                            correspondingMealPhoto = loggedMealPhoto
                        }
                    }
                    Row(modifier = Modifier.padding(20.dp))
                    {
                        Image(bitmap = getBitmap(correspondingMealPhoto.photoFileName, 200,200).asImageBitmap(), contentDescription = "Logged Meal Display Photo" , modifier = Modifier.clip(RoundedCornerShape(20.dp)))
                        Column()
                        {

                        }
                    }
                }
            }
        }
        Button(onClick = { navigationController.navigate(HOME_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }

    LaunchedEffect(Unit)
    {
        KilojouleTrackerRepository.get().databaseMutex.withLock()
        {
            loggedMeals = KilojouleTrackerRepository.get().getMeals()
            loggedMealPhotos = KilojouleTrackerRepository.get().getMealPhotos()
        }
    }
}

@Composable
fun DailyProgressScreen(navigationController: NavHostController, settingsViewModel: SettingsViewModel, modifier: Modifier = Modifier)
{
    var kilojouleProgressPercentage by remember { mutableStateOf(0.0f) }
    var fatProgressPercentage by remember { mutableStateOf(0.0f) }
    var carbohydrateProgressPercentage by remember { mutableStateOf(0.0f) }
    var proteinProgressPercentage by remember { mutableStateOf(0.0f) }

    var goalKilojouleIntake = settingsViewModel.kilojouleGoalText.toFloat() //Not these values default to 0 if the user entered an invalid number in the settings or hasn't set anything yet
    var goalFatIntake = settingsViewModel.fatGoalText.toFloat()
    var goalCarbohydrateIntake = settingsViewModel.carbohydrateGoalText.toFloat()
    var goalProteinIntake = settingsViewModel.proteinGoalText.toFloat()

    LaunchedEffect(Unit)
    {
        launch()
        {
            withContext(Dispatchers.IO)
            {
                var totalKilojoules = KilojouleTrackerRepository.get().getTotalKilojoules()
                var totalFat = KilojouleTrackerRepository.get().getTotalFatWeight()
                var totalCarbohydrates = KilojouleTrackerRepository.get().getTotalCarbohydrateWeight()
                var totalProtein = KilojouleTrackerRepository.get().getTotalProteinWeight()

                if(goalKilojouleIntake == 0.0f)
                {
                    kilojouleProgressPercentage = 0.0f
                }
                else
                {
                    kilojouleProgressPercentage = totalKilojoules.toFloat() / goalKilojouleIntake
                }
                if(goalFatIntake == 0.0f)
                {
                    fatProgressPercentage = 0.0f
                }
                else
                {
                    fatProgressPercentage = totalFat.toFloat() / goalFatIntake
                }
                if(goalCarbohydrateIntake == 0.0f)
                {
                    carbohydrateProgressPercentage = 0.0f
                }
                else
                {
                    carbohydrateProgressPercentage = totalCarbohydrates.toFloat() / goalCarbohydrateIntake
                }
                if(goalProteinIntake == 0.0f)
                {
                    proteinProgressPercentage = 0.0f
                }
                else
                {
                    proteinProgressPercentage = totalProtein.toFloat() / goalProteinIntake
                }
            }

        }
    }

    Box(modifier = modifier)
    {
        Text(text = "Progress", fontWeight = FontWeight.Bold, fontSize = 40.sp, modifier = Modifier.align(Alignment.TopCenter))
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize())
        {
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Energy Consumption Progress: ${String.format("%.2f", kilojouleProgressPercentage * 100)}%", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { kilojouleProgressPercentage }, color = Color.Red, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Fat Consumption Progress: ${String.format("%.2f",fatProgressPercentage * 100)}%", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { fatProgressPercentage }, color = Color.Yellow, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Carbohydrate Consumption Progress: ${String.format("%.2f", carbohydrateProgressPercentage * 100)}%", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { carbohydrateProgressPercentage }, color = Color.Green, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f)
                )
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Protein Consumption Progress: ${String.format("%.2f", proteinProgressPercentage * 100)}%", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { proteinProgressPercentage }, color = Color.Cyan, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
        }
        Button(onClick = { navigationController.navigate(HOME_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }
}

@Composable
fun SettingsScreen(navigationController: NavHostController, settingsMealViewModel: SettingsViewModel, modifier: Modifier = Modifier)
{
    val context = LocalContext.current.applicationContext

    Box(modifier = modifier)
    {
        Text("Set Your Goals", fontWeight = FontWeight.Bold, fontSize = 40.sp, modifier = Modifier.align(Alignment.TopCenter).padding(0.dp, 20.dp, 0.dp, 0.dp))
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.align(Alignment.Center).fillMaxSize())
        {
            TextField(value = settingsMealViewModel.kilojouleGoalText, label = { Text("Enter your Daily Kilojoule Goal (kJ)") }, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> settingsMealViewModel.kilojouleGoalText = text

                try
                {
                    settingsMealViewModel.kilojouleGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Kilojoule goal must be numeric!", Toast.LENGTH_LONG).show()
                    settingsMealViewModel.kilojouleGoalText = "0.0"
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            TextField(value = settingsMealViewModel.fatGoalText, label = { Text("Enter your Fat Goal (grams)") }, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> settingsMealViewModel.fatGoalText = text

                try
                {
                    settingsMealViewModel.fatGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Fat goal must be numeric!", Toast.LENGTH_LONG).show()
                    settingsMealViewModel.fatGoalText = "0.0"
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            TextField(value = settingsMealViewModel.carbohydrateGoalText, label = { Text("Enter your Daily Carbohydrate Goal (grams)") }, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> settingsMealViewModel.carbohydrateGoalText = text

                try
                {
                    settingsMealViewModel.carbohydrateGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Carbohydrate goal must be numeric!", Toast.LENGTH_LONG).show()
                    settingsMealViewModel.carbohydrateGoalText = "0.0"
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = settingsMealViewModel.proteinGoalText, label = { Text("Enter your Daily Protein Goal (grams)") }, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> settingsMealViewModel.proteinGoalText = text

                try
                {
                    settingsMealViewModel.proteinGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Protein goal must be numeric!", Toast.LENGTH_LONG).show()
                    settingsMealViewModel.proteinGoalText = "0.0"
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Button(onClick = { navigationController.navigate(HOME_SCREEN_ROUTE) }, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }
}
