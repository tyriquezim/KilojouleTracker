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

    NavHost(navController = navigationController, startDestination = "HomeScreen")
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

        }
        composable(SETTINGS_SCREEN_ROUTE)
        {
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
    var servingSizeText: String by remember { mutableStateOf("") }
    var numKilojoulesText: String by remember { mutableStateOf("") }
    var fatWeightText: String by remember { mutableStateOf("") }
    var carbohydrateWeightText: String by remember { mutableStateOf("") }
    var proteinWeightText: String by remember { mutableStateOf("") }
    var manualMealLogging: Boolean by remember { mutableStateOf(false) }
    var firstMealHasBeenLogged: Boolean by remember { mutableStateOf(false) }

    Box(modifier = modifier)
    {
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(20.dp))
        {
            TextField(value = mealNameText, label = {Text("Enter the Meal Name")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text: String -> mealNameText = text
                logMealViewModel.mealNameText = text
                manualMealLogging = false //So that it reverts back if the user decides to log a different meal after a failed request
            }, modifier = Modifier.align(Alignment.CenterHorizontally))
            TextField(value = servingSizeText, label = {Text("Enter the Serving Size (grams)")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text -> servingSizeText = text
                logMealViewModel.servingWeightText = servingSizeText
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
                TextField(value = numKilojoulesText, label = { Text("Enter the number of Kilojoules") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text ->
                    numKilojoulesText = text
                    logMealViewModel.numKilojoulesText = numKilojoulesText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = fatWeightText, label = { Text("Enter the amount of Fat (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> fatWeightText = text
                    logMealViewModel.fatWeightText = fatWeightText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = carbohydrateWeightText, label = { Text("Enter the amount of Carbohydrates (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> carbohydrateWeightText = text
                    logMealViewModel.carbohydrateWeightText = carbohydrateWeightText
                }, modifier = Modifier.align(Alignment.CenterHorizontally))
                TextField(value = proteinWeightText, label = { Text("Enter the amount of Protein (grams)") }, shape = RoundedCornerShape(100), leadingIcon = { Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon") }, onValueChange =
                {
                    text -> proteinWeightText = text
                    logMealViewModel.proteinWeightText = servingSizeText
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
fun SettingsScreen(navigationController: NavHostController, logMealViewModel: LogMealViewModel, modifier: Modifier = Modifier)
{
    Box(modifier = modifier)
    {
        TextField(label = "E")
    }
}
