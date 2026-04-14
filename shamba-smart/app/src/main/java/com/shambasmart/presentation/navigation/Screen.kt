package com.shambasmart.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object LaunchChoice : Screen("launch_choice")
    object FarmSetup : Screen("farm_setup")
    object Dashboard : Screen("dashboard")
    object Livestock : Screen("livestock")
    object AnimalDetail : Screen("animal_detail/{animalId}") {
        fun createRoute(animalId: Long) = "animal_detail/$animalId"
    }
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
    object FarmMap : Screen("farm_map")
    object GPSBoundary : Screen("gps_boundary/{plotId}/{plotName}") {
        fun createRoute(plotId: Long, plotName: String) = "gps_boundary/$plotId/$plotName"
    }
    object ScoutingCapture : Screen("scouting_capture")
}
