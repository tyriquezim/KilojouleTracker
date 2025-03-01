package com.android.personal.calorietracker

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navigationController: NavHostController, modifier: Modifier = Modifier)
{
    Box(modifier = modifier)
    {
        Column(modifier = Modifier.align(Alignment.Center))
        {
            Button(onClick = {navigationController.navigate(LOG_MEAL_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text("Log Meal")
            }
            Button(onClick = {navigationController.navigate(VIEW_LOGGED_MEALS_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text("View Logged Meals")
            }
            Button(onClick = {navigationController.navigate(DAILY_PROGRESS_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
                Text("View Daily Progress")
            }
            Button(onClick = {navigationController.navigate(SETTINGS_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.CenterHorizontally))
            {
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
    var mealTypeText: String by remember { mutableStateOf("") }
    var servingSizeText: String by remember { mutableStateOf("0.0") }
    var numCaloriesText: String by remember { mutableStateOf("0.0") }

    Box(modifier = modifier)
    {
        Column(modifier = Modifier.align(Alignment.TopCenter).padding(20.dp))
        {
            TextField(value = mealNameText, label = {Text("Enter the Meal Name")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text: String -> mealNameText = text
                logMealViewModel.mealNameText = text
            })
            TextField(value = mealTypeText, label = {Text("Enter the Meal Type")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text: String -> mealTypeText = text
                logMealViewModel.mealTypeText = mealTypeText
            })
            TextField(value = servingSizeText, label = {Text("Enter the Serving Size (grams)")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text -> servingSizeText = text
                try
                {
                    logMealViewModel.servingSizeText = servingSizeText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "Enter a numerical value!", Toast.LENGTH_LONG).show()
                }
            })
            TextField(value = numCaloriesText, label = {Text("Enter the number of Calories")}, shape = RoundedCornerShape(100), leadingIcon = {Icon(painter = painterResource(id = R.drawable.baseline_fastfood_24), contentDescription = "Food Icon")}, onValueChange =
            {
                text -> numCaloriesText = text
                try
                {
                    logMealViewModel.numCaloriesText = servingSizeText.toDouble()
                }
                catch(e: NumberFormatException)
                {
                    Toast.makeText(context, "Enter a numerical value!", Toast.LENGTH_LONG).show()
                }
            })
        }
        Button(onClick = {navigationController.navigate(HOME_SCREEN_ROUTE)}, modifier = Modifier.align(Alignment.BottomEnd))
        {
            Text("Back")
        }
    }
}

@Composable
fun SettingsScreen()
{

}
