package com.android.personal.kilojouletracker

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.android.personal.kilojouletracker.model.Meal
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

@Preview
@Composable
fun PreviewScreen()
{
    NavigationScreen()
}

@Composable
fun NavigationScreen()
{
    val navigationController: NavHostController =  rememberNavController()

    NavHost(navController = navigationController, startDestination = HOME_SCREEN_ROUTE)
    {
        composable(HOME_SCREEN_ROUTE)
        {
            HomeScreen(navigationController = navigationController, Modifier.fillMaxSize())
        }
        composable(LOG_MEAL_SCREEN_ROUTE)
        {
            LogMealScreen(navigationController = navigationController, logMealViewModel = LogMealViewModel(), Modifier.fillMaxSize())
        }
        composable(VIEW_LOGGED_MEALS_SCREEN_ROUTE)
        {
        }
        composable(DAILY_PROGRESS_SCREEN_ROUTE)
        {
            DailyProgressScreen(navigationController = navigationController, settingsViewModel = SettingsViewModel())
        }
        composable(SETTINGS_SCREEN_ROUTE)
        {
            SettingsScreen(navigationController = navigationController, settingsMealViewModel = SettingsViewModel(), modifier = Modifier.fillMaxSize())
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
            Button(onClick = {navigationController.navigate(LOG_MEAL_SCREEN_ROUTE)}, modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_lunch_dining_24), contentDescription = "Log Meal Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("Log Meal")
            }
            Button(onClick = {navigationController.navigate(VIEW_LOGGED_MEALS_SCREEN_ROUTE)}, modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_list_24), contentDescription = "View Logged Meal Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("View Logged Meals")
            }
            Button(onClick = {navigationController.navigate(DAILY_PROGRESS_SCREEN_ROUTE)}, modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(0.dp, 0.dp, 0.dp, 20.dp))
            {
                Icon(painterResource(id = R.drawable.baseline_stars_24), contentDescription = "Daily Progress Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("View Daily Progress")
            }
            Button(onClick = {navigationController.navigate(SETTINGS_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Icon(painterResource(id = R.drawable.baseline_settings_24), contentDescription = "Settings Icon", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
                Text("Settings")
            }
        }
    }
}

@Composable
fun LogMealScreen(navigationController: NavHostController, logMealViewModel: LogMealViewModel, modifier: Modifier = Modifier)
{
    val context = LocalContext.current
    var mealNameText: String by remember { mutableStateOf("") }
    var servingWeightText: String by remember { mutableStateOf("") }
    var numKilojoulesText: String by remember { mutableStateOf("") }
    var fatWeightText: String by remember { mutableStateOf("") }
    var carbohydrateWeightText: String by remember { mutableStateOf("") }
    var proteinWeightText: String by remember { mutableStateOf("") }
    var manualMealLogging: Boolean by remember { mutableStateOf(false) }
    var firstMealHasBeenLogged: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier)
    {
        Column(modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(20.dp))
        {
            TextField(value = logMealViewModel.mealNameText, label = {Text("Enter the Meal Name")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text: String -> mealNameText = text
                logMealViewModel.mealNameText = mealNameText
                manualMealLogging = false //So that it reverts back if the user decides to log a different meal after a failed request
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = logMealViewModel.servingWeightText, label = {Text("Enter the Serving Size (grams)")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text -> servingWeightText = text
                logMealViewModel.servingWeightText = servingWeightText
            }, modifier = Modifier.align(Alignment.CenterHorizontally))

            if(!manualMealLogging) //Initially it will try automatically fill this info out with info from the API
            {
                if(firstMealHasBeenLogged)
                {
                    Row(modifier = Modifier.padding(0.dp, 40.dp, 0.dp, 0.dp))
                    {
                        Text("Kilojoules: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(numKilojoulesText + "kJ", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Fat: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(fatWeightText + "g", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Carbohydrate: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(carbohydrateWeightText + "g", fontSize = 20.sp)
                    }
                    Row()
                    {
                        Text("Protein: ", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(proteinWeightText + "g", fontSize = 20.sp)
                    }
                }
            }
            else //If the API request failed, it will need the user to manually enter the information
            {
                TextField(value = logMealViewModel.numKilojoulesText, label = { Text("Enter the number of Kilojoules") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> numKilojoulesText = text
                    logMealViewModel.numKilojoulesText = numKilojoulesText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = logMealViewModel.fatWeightText, label = { Text("Enter the amount of Fat (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> fatWeightText = text
                    logMealViewModel.fatWeightText = fatWeightText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = logMealViewModel.carbohydrateWeightText, label = { Text("Enter the amount of Carbohydrates (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> carbohydrateWeightText = text
                    logMealViewModel.carbohydrateWeightText = carbohydrateWeightText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = logMealViewModel.proteinWeightText, label = { Text("Enter the amount of Protein (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> proteinWeightText = text
                    logMealViewModel.proteinWeightText = proteinWeightText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
        Button(onClick =
        {
            GlobalScope.launch() //Global Scope because meal logging cannot be interrupted
            {
                withContext(Dispatchers.IO)
                {
                    var loggedMeal: Meal? = null

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
                                numKilojoulesText = ""
                                fatWeightText = ""
                                carbohydrateWeightText = ""
                                proteinWeightText = ""

                                withContext(Dispatchers.Main)
                                {
                                    Toast.makeText(context, "Could not log meal. Enter the meal details manually.", Toast.LENGTH_LONG).show()
                                }
                            }
                            else
                            {
                                KilojouleTrackerRepository.get().insertMeal(loggedMeal!!)
                                numKilojoulesText = loggedMeal!!.numKilojoules.toString()
                                fatWeightText = loggedMeal!!.fatWeight.toString()
                                carbohydrateWeightText = loggedMeal!!.carbohydrateWeight.toString()
                                proteinWeightText = loggedMeal!!.proteinWeight.toString()
                                firstMealHasBeenLogged = true

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
                                KilojouleTrackerRepository.get().insertMeal(loggedMeal!!)
                                manualMealLogging = false
                            }
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
        }, modifier = Modifier.align(Alignment.BottomCenter))
        {
            Icon(painterResource(id = R.drawable.baseline_add_24), contentDescription = "Log Meal", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Log Meal")
        }
        Button(onClick = {navigationController.navigate(HOME_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }
}

@Composable
fun ViewLoggedMealsScreen(navigationController: NavHostController, modifier: Modifier = Modifier)
{
    Box(modifier = modifier)
    {

    }
}

@Composable
fun DailyProgressScreen(navigationController: NavHostController, settingsViewModel: SettingsViewModel, modifier: Modifier = Modifier)
{
    var kilojouleProgressPercentage = 0.4f
    var fatProgressPercentage = 0.8f
    var carbohydrateProgressPercentage = 0.7f
    var proteinProgressPercentage = 0.3f

    Box(modifier = modifier)
    {
        Text(text = "Progress", fontWeight = FontWeight.Bold, fontSize = 40.sp, modifier = Modifier.align(Alignment.TopCenter))
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize())
        {
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Energy Consumption Progress", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { kilojouleProgressPercentage }, color = Color.Red,  modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Fat Consumption Progress", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { fatProgressPercentage }, color = Color.Yellow, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Carbohydrate Consumption Progress", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { carbohydrateProgressPercentage }, color = Color.Green, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
            Column(modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text(text = "Protein Consumption Progress", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
                LinearProgressIndicator(progress = { proteinProgressPercentage }, color = Color.Cyan, modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth(0.8f))
            }
        }
        Button(onClick = {navigationController.navigate(HOME_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.BottomEnd))
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
    var kilojouleGoalText by remember { mutableStateOf("") }
    var fatGoalText by remember {mutableStateOf("")}
    var carbohydrateGoalText by remember {mutableStateOf("")}
    var proteinGoalText by remember {mutableStateOf("")}

    Box(modifier = modifier)
    {
        Text("Set Your Goals", fontWeight = FontWeight.Bold, fontSize = 40.sp, modifier = Modifier
            .align(Alignment.TopCenter)
            .padding(0.dp, 20.dp, 0.dp, 0.dp))
        Column(verticalArrangement = Arrangement.SpaceEvenly, modifier = Modifier
            .align(Alignment.Center)
            .fillMaxSize())
        {
            TextField(value = settingsMealViewModel.kilojouleGoalText, label = {Text("Enter your Daily Kilojoule Goal (kJ)")}, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> kilojouleGoalText = text
                settingsMealViewModel.kilojouleGoalText = kilojouleGoalText

                try
                {
                    settingsMealViewModel.kilojouleGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Kilojoule goal must be numeric!", Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = settingsMealViewModel.fatGoalText, label = { Text("Enter your Fat Goal (grams)")}, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> fatGoalText = text
                settingsMealViewModel.fatGoalText = fatGoalText

                try
                {
                    settingsMealViewModel.fatGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Fat goal must be numeric!", Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = settingsMealViewModel.carbohydrateGoalText, label = {Text("Enter your Daily Carbohydrate Goal (grams)")}, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> carbohydrateGoalText = text
                settingsMealViewModel.carbohydrateGoalText = carbohydrateGoalText

                try
                {
                    settingsMealViewModel.carbohydrateGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Carbohydrate goal must be numeric!", Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = settingsMealViewModel.proteinGoalText, label = {Text("Enter your Daily Protein Goal (grams)")}, shape = RoundedCornerShape(100), onValueChange =
            {
                text -> proteinGoalText = text
                settingsMealViewModel.proteinGoalText = proteinGoalText

                try
                {
                    settingsMealViewModel.proteinGoalText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "The Protein goal must be numeric!", Toast.LENGTH_LONG).show()
                }
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        Button(onClick = {navigationController.navigate(HOME_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Icon(painterResource(id = R.drawable.baseline_arrow_back_24), contentDescription = "Back Arrow", modifier = Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp))
            Text("Back")
        }
    }
}
