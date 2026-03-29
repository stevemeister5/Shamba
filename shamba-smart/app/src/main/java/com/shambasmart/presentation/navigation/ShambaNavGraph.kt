package com.shambasmart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.shambasmart.presentation.onboarding.LaunchChoiceScreen
import com.shambasmart.maarifa.MaarifaViewModel
import androidx.hilt.navigation.compose.hiltViewModel

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object LaunchChoice : Screen("launch_choice")
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
    farmName: String = "Shamba Smart",
    weatherSummary: String = "24°C Sunny",
    maarifaViewModel: MaarifaViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val startDestination = if (isOnboardingCompleted) Screen.Dashboard.route else Screen.Onboarding.route
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Onboarding & Setup - No scaffold
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.LaunchChoice.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.LaunchChoice.route) {
            LaunchChoiceScreen(
                onSetupFarm = {
                    navController.navigate(Screen.FarmSetup.route) {
                        popUpTo(Screen.LaunchChoice.route) { inclusive = true }
                    }
                },
                onExploreDemo = {
                    // Set up demo mode and navigate to dashboard
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.LaunchChoice.route) { inclusive = true }
                    }
                }
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
        
        // Main App Screens - With AppScaffold
        composable(Screen.Dashboard.route) {
            AppScaffold(
                currentRoute = "dashboard",
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
                DashboardScreen()
            }
        }
        
        composable(Screen.Livestock.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                LivestockScreen()
            }
        }
        
        composable(Screen.EggProduction.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                EggProductionScreen()
            }
        }
        
        composable(Screen.FlockManagement.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                FlockManagementScreen()
            }
        }
        
        composable(Screen.Crops.route) {
            AppScaffold(
                currentRoute = "crops",
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
                CropsScreen(
                    onNavigateToCropPlanting = {
                        navController.navigate(Screen.ScoutingCapture.route)
                    }
                )
            }
        }
        
        composable(Screen.PlotAnalytics.route) {
            AppScaffold(
                currentRoute = "crops",
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
                PlotAnalyticsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.Cheese.route) {
            AppScaffold(
                currentRoute = "cheese",
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
                CheeseScreen()
            }
        }
        
        composable(Screen.Feed.route) {
            AppScaffold(
                currentRoute = "feed",
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
                FeedScreen()
            }
        }
        
        composable(Screen.Financial.route) {
            AppScaffold(
                currentRoute = "financial",
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
                FinancialScreen()
            }
        }
        
        composable(Screen.Labour.route) {
            AppScaffold(
                currentRoute = "labour",
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
                LabourScreen()
            }
        }
        
        composable(Screen.Calendar.route) {
            AppScaffold(
                currentRoute = "calendar",
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
                CalendarScreen()
            }
        }
        
        composable(Screen.Infrastructure.route) {
            AppScaffold(
                currentRoute = "maps",
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
                InfrastructureScreen()
            }
        }
        
        composable(Screen.Settings.route) {
            AppScaffold(
                currentRoute = "settings",
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
                SettingsScreen()
            }
        }
        
        composable(Screen.LCR.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                LCRScreen()
            }
        }
        
        composable(Screen.VisionGrading.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                VisionGradingScreen()
            }
        }
        
        composable(Screen.WaterOptimizer.route) {
            AppScaffold(
                currentRoute = "infrastructure",
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
                WaterOptimizerScreen()
            }
        }
        
        composable(Screen.AcousticGuard.route) {
            AppScaffold(
                currentRoute = "livestock",
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
                AudioAlertScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.NpuConfig.route) {
            AppScaffold(
                currentRoute = "settings",
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
                NpuConfigScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.Alerts.route) {
            AppScaffold(
                currentRoute = "dashboard",
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
                AlertsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.Maintenance.route) {
            AppScaffold(
                currentRoute = "settings",
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
                MaintenanceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = Screen.GPSBoundary.route,
            arguments = listOf(
                navArgument("plotId") { type = NavType.LongType },
                navArgument("plotName") { type = NavType.StringType }
            )
        ) {
            AppScaffold(
                currentRoute = "maps",
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
                GPSBoundaryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.FarmMap.route) {
            AppScaffold(
                currentRoute = "maps",
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
                FarmMapScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(Screen.ScoutingCapture.route) {
            AppScaffold(
                currentRoute = "crops",
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
                ScoutingCaptureScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMap = { navController.navigate(Screen.FarmMap.route) }
                )
            }
        }
    }
}