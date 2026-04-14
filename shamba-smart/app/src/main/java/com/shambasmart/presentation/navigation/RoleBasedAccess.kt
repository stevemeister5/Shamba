package com.shambasmart.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*

enum class UserRole {
    OWNER, FARM_MANAGER, WORKER
}

data class NavItemConfig(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val requiredRole: UserRole = UserRole.OWNER
)

val allNavigationItems = listOf(
    NavItemConfig("dashboard", "Dashboard", Icons.Outlined.Dashboard, Icons.Filled.Dashboard, UserRole.WORKER),
    NavItemConfig("livestock", "Livestock", Icons.Outlined.Pets, Icons.Filled.Pets, UserRole.WORKER),
    NavItemConfig("crops", "Crops", Icons.Outlined.Grass, Icons.Filled.Grass, UserRole.WORKER),
    NavItemConfig("cheese", "Cheese", Icons.Outlined.Inventory2, Icons.Filled.Inventory2, UserRole.WORKER),
    NavItemConfig("feed", "Feed", Icons.Outlined.Restaurant, Icons.Filled.Restaurant, UserRole.WORKER),
    NavItemConfig("financial", "Financial", Icons.Outlined.AccountBalance, Icons.Filled.AccountBalance, UserRole.FARM_MANAGER),
    NavItemConfig("labour", "Labour", Icons.Outlined.People, Icons.Filled.People, UserRole.FARM_MANAGER),
    NavItemConfig("calendar", "Calendar", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth, UserRole.WORKER),
    NavItemConfig("maps", "Maps", Icons.Outlined.Map, Icons.Filled.Map, UserRole.WORKER),
    NavItemConfig("settings", "Settings", Icons.Outlined.Settings, Icons.Filled.Settings, UserRole.OWNER)
)

fun getNavigationItemsForRole(role: UserRole): List<NavItemConfig> {
    return allNavigationItems.filter { item ->
        when (role) {
            UserRole.OWNER -> true // Owner sees everything
            UserRole.FARM_MANAGER -> item.requiredRole <= UserRole.FARM_MANAGER
            UserRole.WORKER -> item.requiredRole <= UserRole.WORKER
        }
    }
}

fun UserRole.ordinal(): Int = when (this) {
    UserRole.OWNER -> 2
    UserRole.FARM_MANAGER -> 1
    UserRole.WORKER -> 0
}

private operator fun UserRole.compareTo(other: UserRole): Int {
    return this.ordinal() - other.ordinal()
}