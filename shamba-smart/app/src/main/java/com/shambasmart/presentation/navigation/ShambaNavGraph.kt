package com.shambasmart.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.shambasmart.presentation.dashboard.DashboardScreen
import com.shambasmart.presentation.livestock.LivestockScreen
import com.shambasmart.presentation.livestock.AnimalDetailScreen
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
import com.shambasmart.presentation.onboarding.LaunchChoiceScreen
import com.shambasmart.presentation.onboarding.OnboardingViewModel
import com.shambasmart.maarifa.MaarifaViewModel
import com.shambasmart.demo.DemoModeManager
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ShambaNavGraph(
    navController: NavHostController,
    isOnboardingCompleted: Boolean,
    isLaunchChoiceMade: Boolean = false,
    farmName: String = "Shamba Smart",
    weatherSummary: String = "24°C Sunny",
    maarifaViewModel: MaarifaViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""

    // Screens that should NOT show the scaffold (onboarding, setup, etc.)
    val noScaffoldRoutes = listOf(
        Screen.Onboarding.route,
        Screen.LaunchChoice.route,
        Screen.FarmSetup.route
    )

    val showScaffold = !noScaffoldRoutes.contains(currentRoute)

    if (showScaffold) {
        AppScaffold(
            currentRoute = currentRoute,
            farmName = farmName,
            weatherSummary = weatherSummary,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(Screen.Dashboard.route) { saveState = true }
                    launchSingleTop = true
                    restoreState = true
                }
            },
            maarifaViewModel = maarifaViewModel
        ) {
            NavContent(navController, isOnboardingCompleted, isLaunchChoiceMade, maarifaViewModel)
        }
    } else {
        NavContent(navController, isOnboardingCompleted, isLaunchChoiceMade, maarifaViewModel)
    }
}

@Composable
private fun NavContent(
    navController: NavHostController,
    isOnboardingCompleted: Boolean,
    isLaunchChoiceMade: Boolean,
    maarifaViewModel: MaarifaViewModel
) {
    val startDestination = when {
        !isOnboardingCompleted -> Screen.Onboarding.route
        !isLaunchChoiceMade -> Screen.LaunchChoice.route
        else -> Screen.Dashboard.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onComplete = {
                navController.navigate(Screen.LaunchChoice.route) {
                    popUpTo(Screen.Onboarding.route) { inclusive = true }
                }
            })
        }
        
        composable(Screen.LaunchChoice.route) {
            val context = LocalContext.current
            val demoModeManager = DemoModeManager.fromAppContext(context)
            val onboardingViewModel = hiltViewModel<OnboardingViewModel>()
            LaunchChoiceScreen(
                onSetupFarm = {
                    navController.navigate(Screen.FarmSetup.route) {
                        popUpTo(Screen.LaunchChoice.route) { inclusive = true }
                    }
                },
                onLaunchDemo = {
                    demoModeManager.launchDemo {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.LaunchChoice.route) { inclusive = true }
                        }
                    }
                },
                demoModeManager = demoModeManager,
                onChoiceMade = { onboardingViewModel.setLaunchChoiceMade() }
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
                onNavigateToGPS = { id, name -> navController.navigate(Screen.GPSBoundary.createRoute(id, name)) },
                onNavigateToInfrastructure = { navController.navigate(Screen.Infrastructure.route) },
                onNavigateToPlots = { navController.navigate(Screen.Crops.route) },
                onNavigateToLivestock = { navController.navigate(Screen.Livestock.route) },
                onNavigateToCrops = { navController.navigate(Screen.Crops.route) }
            )
        }
        
        composable(Screen.Dashboard.route) { DashboardScreen() }
        
        composable(Screen.Livestock.route) {
            LivestockScreen(onNavigateToAnimalDetail = { id -> navController.navigate(Screen.AnimalDetail.createRoute(id)) })
        }

        composable(
            route = Screen.AnimalDetail.route,
            arguments = listOf(navArgument("animalId") { type = NavType.LongType })
        ) {
            AnimalDetailScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(Screen.Crops.route) {
            CropsScreen(onNavigateToCropPlanting = { navController.navigate(Screen.ScoutingCapture.route) })
        }
        
        composable(Screen.PlotAnalytics.route) { PlotAnalyticsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Cheese.route) { CheeseScreen() }
        composable(Screen.Feed.route) { FeedScreen() }
        composable(Screen.Financial.route) { FinancialScreen() }
        composable(Screen.Labour.route) { LabourScreen() }
        composable(Screen.Calendar.route) { CalendarScreen() }
        composable(Screen.Infrastructure.route) { InfrastructureScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
        composable(Screen.LCR.route) { LCRScreen() }
        composable(Screen.VisionGrading.route) { VisionGradingScreen() }
        composable(Screen.WaterOptimizer.route) { WaterOptimizerScreen() }
        composable(Screen.AcousticGuard.route) { AudioAlertScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.NpuConfig.route) { NpuConfigScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Alerts.route) { AlertsScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.Maintenance.route) { MaintenanceScreen(onNavigateBack = { navController.popBackStack() }) }
        composable(Screen.FarmMap.route) { FarmMapScreen(onNavigateBack = { navController.popBackStack() }) }
        
        composable(
            route = Screen.GPSBoundary.route,
            arguments = listOf(navArgument("plotId") { type = NavType.LongType }, navArgument("plotName") { type = NavType.StringType })
        ) {
            GPSBoundaryScreen(onNavigateBack = { navController.popBackStack() })
        }
        
        composable(Screen.ScoutingCapture.route) {
            ScoutingCaptureScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToMap = { navController.navigate(Screen.FarmMap.route) })
        }
    }
}
