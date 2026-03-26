package com.shambasmart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.shambasmart.presentation.dashboard.DashboardScreen
import com.shambasmart.presentation.livestock.LivestockScreen
import com.shambasmart.presentation.crops.CropsScreen
import com.shambasmart.presentation.crops.PlotAnalyticsScreen
import com.shambasmart.presentation.cheese.CheeseScreen
import com.shambasmart.presentation.feed.FeedScreen
import com.shambasmart.presentation.financial.FinancialScreen
import com.shambasmart.presentation.labour.LabourScreen
import com.shambasmart.presentation.calendar.CalendarScreen
import com.shambasmart.presentation.infrastructure.InfrastructureScreen
import com.shambasmart.presentation.settings.SettingsScreen
import com.shambasmart.presentation.ml.lcr.LCRScreen
import com.shambasmart.presentation.ml.vision.VisionGradingScreen
import com.shambasmart.presentation.ml.water.WaterOptimizerScreen
import com.shambasmart.presentation.ml.acoustic.AudioAlertScreen
import com.shambasmart.presentation.ml.npu.NpuConfigScreen
import com.shambasmart.presentation.alerts.AlertsScreen
import com.shambasmart.presentation.maintenance.MaintenanceScreen
import com.shambasmart.presentation.setup.FarmSetupScreen
import com.shambasmart.presentation.ar.ARBoundaryScreen

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Livestock : Screen("livestock")
    object Crops : Screen("crops")
    object PlotAnalytics : Screen("plot_analytics")
    object Cheese : Screen("cheese")
    object Feed : Screen("feed")
    object Financial : Screen("financial")
    object Labour : Screen("labour")
    object Calendar : Screen("calendar")
    object Infrastructure : Screen("infrastructure")
    object Settings : Screen("settings")
    object LCR : Screen("lcr")
    object VisionGrading : Screen("vision_grading")
    object WaterOptimizer : Screen("water_optimizer")
    object AcousticGuard : Screen("acoustic_guard")
    object NpuConfig : Screen("npu_config")
    object Alerts : Screen("alerts")
    object Maintenance : Screen("maintenance")
    object FarmSetup : Screen("farm_setup")
    object ARBoundary : Screen("ar_boundary")
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
        composable(Screen.PlotAnalytics.route) {
            PlotAnalyticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
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
        composable(Screen.LCR.route) {
            LCRScreen()
        }
        composable(Screen.VisionGrading.route) {
            VisionGradingScreen()
        }
        composable(Screen.WaterOptimizer.route) {
            WaterOptimizerScreen()
        }
        composable(Screen.AcousticGuard.route) {
            AudioAlertScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.NpuConfig.route) {
            NpuConfigScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Maintenance.route) {
            MaintenanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FarmSetup.route) {
            FarmSetupScreen(
                onNavigateBack = { navController.popBackStack() },
                onComplete = { navController.navigate(Screen.Dashboard.route) }
            )
        }
        composable(Screen.ARBoundary.route) {
            ARBoundaryScreen(
                onNavigateBack = { navController.popBackStack() },
                onSaveBoundary = { boundaryPoints ->
                    // Save boundary points to database
                    // Navigate back to farm setup or infrastructure
                    navController.popBackStack()
                }
            )
        }
    }
}
