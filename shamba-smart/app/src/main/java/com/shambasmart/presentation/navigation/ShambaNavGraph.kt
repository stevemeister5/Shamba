package com.shambasmart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shambasmart.presentation.dashboard.DashboardScreen
import com.shambasmart.presentation.livestock.LivestockScreen
import com.shambasmart.presentation.crops.CropsScreen
import com.shambasmart.presentation.cheese.CheeseScreen
import com.shambasmart.presentation.feed.FeedScreen
import com.shambasmart.presentation.financial.FinancialScreen
import com.shambasmart.presentation.labour.LabourScreen
import com.shambasmart.presentation.calendar.CalendarScreen
import com.shambasmart.presentation.infrastructure.InfrastructureScreen
import com.shambasmart.presentation.settings.SettingsScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Livestock : Screen("livestock")
    object Crops : Screen("crops")
    object Cheese : Screen("cheese")
    object Feed : Screen("feed")
    object Financial : Screen("financial")
    object Labour : Screen("labour")
    object Calendar : Screen("calendar")
    object Infrastructure : Screen("infrastructure")
    object Settings : Screen("settings")
}

@Composable
fun ShambaNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Livestock.route) {
            LivestockScreen()
        }
        composable(Screen.Crops.route) {
            CropsScreen()
        }
        composable(Screen.Cheese.route) {
            CheeseScreen()
        }
        composable(Screen.Feed.route) {
            FeedScreen()
        }
        composable(Screen.Financial.route) {
            FinancialScreen()
        }
        composable(Screen.Labour.route) {
            LabourScreen()
        }
        composable(Screen.Calendar.route) {
            CalendarScreen()
        }
        composable(Screen.Infrastructure.route) {
            InfrastructureScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}