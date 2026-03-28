package com.shambasmart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shambasmart.presentation.dashboard.DashboardScreen
import com.shambasmart.presentation.livestock.LivestockScreen
import com.shambasmart.presentation.livestock.EggProductionScreen
import com.shambasmart.presentation.livestock.FlockManagementScreen
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
import com.shambasmart.presentation.gps.GPSBoundaryScreen
import com.shambasmart.map.FarmMapScreen
import com.shambasmart.presentation.crops.ScoutingCaptureScreen
import com.shambasmart.presentation.onboarding.OnboardingScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Livestock : Screen("livestock")
    object EggProduction : Screen("egg_production")
    object FlockManagement : Screen("flock_management")
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
    object GPSBoundary : Screen("gps_boundary/{plotId}/{plotName}") {
        fun createRoute(plotId: Long, plotName: String) = "gps_boundary/$plotId/$plotName"
    }
    object FarmMap : Screen("farm_map")
    object ScoutingCapture : Screen("scouting_capture")
}

@Composable
fun ShambaNavGraph(
    navController: NavHostController,
    isOnboardingCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (isOnboardingCompleted) Screen.FarmSetup.route else Screen.Onboarding.route,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.FarmSetup.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Dashboard.route) {
            DashboardScreen()
        }
        composable(Screen.Livestock.route) {
            LivestockScreen()
        }
        composable(Screen.EggProduction.route) {
            EggProductionScreen()
        }
        composable(Screen.FlockManagement.route) {
            FlockManagementScreen()
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
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.FarmSetup.route) { inclusive = true }
                    }
                },
                onNavigateToGPS = { id, name ->
                    navController.navigate(Screen.GPSBoundary.createRoute(id, name))
                },
                onNavigateToInfrastructure = {
                    navController.navigate(Screen.Infrastructure.route)
                },
                onNavigateToPlots = {
                    navController.navigate(Screen.Crops.route)
                },
                onNavigateToLivestock = {
                    navController.navigate(Screen.Livestock.route)
                },
                onNavigateToCrops = {
                    navController.navigate(Screen.Crops.route)
                }
            )
        }
        composable(
            route = Screen.GPSBoundary.route,
            arguments = listOf(
                navArgument("plotId") { type = NavType.LongType },
                navArgument("plotName") { type = NavType.StringType }
            )
        ) {
            GPSBoundaryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.FarmMap.route) {
            FarmMapScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ScoutingCapture.route) {
            ScoutingCaptureScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMap = { navController.navigate(Screen.FarmMap.route) }
            )
        }
    }
}
