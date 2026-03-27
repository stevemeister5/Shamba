package com.shambasmart.map

/**
 * 35 Map Marker Types for Farm Management
 * 
 * Each marker type has an icon, color, and category
 * for visual organization on the farm map.
 */
enum class MapMarkerType(
    val displayName: String,
    val icon: String,
    val color: String,
    val category: MarkerCategory
) {
    // 🌾 CROP MARKERS (8 types)
    MAIZE_PLOT("Maize Plot", "🌽", "#FFC107", MarkerCategory.CROP),
    BEAN_PLOT("Bean Plot", "🫘", "#4CAF50", MarkerCategory.CROP),
    TOMATO_PLOT("Tomato Plot", "🍅", "#F44336", MarkerCategory.CROP),
    KALE_PLOT("Kale Plot", "🥬", "#8BC34A", MarkerCategory.CROP),
    ONION_PLOT("Onion Plot", "🧅", "#FF9800", MarkerCategory.CROP),
    NAPIER_GRASS("Napier Grass", "🌿", "#689F38", MarkerCategory.CROP),
    CASSAVA_PLOT("Cassava Plot", "🍠", "#795548", MarkerCategory.CROP),
    SWEET_POTATO("Sweet Potato", "🍠", "#E91E63", MarkerCategory.CROP),

    // 🏠 INFRASTRUCTURE MARKERS (8 types)
    MAIN_HOUSE("Main House", "🏠", "#795548", MarkerCategory.INFRASTRUCTURE),
    WORKER_QUARTERS("Worker Quarters", "🏘️", "#9E9E9E", MarkerCategory.INFRASTRUCTURE),
    EQUIPMENT_SHED("Equipment Shed", "🔧", "#607D8B", MarkerCategory.INFRASTRUCTURE),
    FEED_STORAGE("Feed Storage", "📦", "#FF5722", MarkerCategory.INFRASTRUCTURE),
    SEED_STORAGE("Seed Storage", "🌱", "#4CAF50", MarkerCategory.INFRASTRUCTURE),
    CHEESE_ROOM("Cheese Room", "🧀", "#FFEB3B", MarkerCategory.INFRASTRUCTURE),
    MILK_COLLECTION("Milk Collection Point", "🥛", "#03A9F4", MarkerCategory.INFRASTRUCTURE),
    DIPPING_TANK("Dipping Tank", "🛁", "#00BCD4", MarkerCategory.INFRASTRUCTURE),

    // 💧 WATER & ENERGY (5 types)
    BOREHOLE("Borehole", "💧", "#2196F3", MarkerCategory.WATER),
    WATER_TROUGH("Water Trough", "🚰", "#03A9F4", MarkerCategory.WATER),
    IRRIGATION_POINT("Irrigation Point", "🌊", "#00BCD4", MarkerCategory.WATER),
    RAINWATER_TANK("Rainwater Tank", "🪣", "#4FC3F7", MarkerCategory.WATER),
    SOLAR_PANEL("Solar Panel", "☀️", "#FFC107", MarkerCategory.WATER),

    // 🐐 LIVESTOCK MARKERS (5 types)
    GOAT_PEN("Goat Pen", "🐐", "#9C27B0", MarkerCategory.LIVESTOCK),
    SHEEP_PEN("Sheep Pen", "🐑", "#E1BEE7", MarkerCategory.LIVESTOCK),
    POULTRY_HOUSE("Poultry House", "🐔", "#FFD54F", MarkerCategory.LIVESTOCK),
    ISOLATION_PEN("Isolation Pen", "🏥", "#F44336", MarkerCategory.LIVESTOCK),
    BREEDING_PEN("Breeding Pen", "💕", "#E91E63", MarkerCategory.LIVESTOCK),

    // 🌿 WASTE & COMPOST (3 types)
    COMPOST_PIT_A("Compost Pit A", "♻️", "#795548", MarkerCategory.WASTE),
    COMPOST_PIT_B("Compost Pit B", "♻️", "#8D6E63", MarkerCategory.WASTE),
    MANURE_STORAGE("Manure Storage", "💩", "#5D4037", MarkerCategory.WASTE),

    // 🛡️ SAFETY & BOUNDARIES (3 types)
    GATE_ENTRANCE("Gate/Entrance", "🚪", "#455A64", MarkerCategory.SAFETY),
    FENCE_POST("Fence Post", "📌", "#78909C", MarkerCategory.SAFETY),
    SECURITY_POINT("Security Point", "🛡️", "#37474F", MarkerCategory.SAFETY),

    // 📍 CUSTOM MARKERS (3 types)
    MEETING_POINT("Meeting Point", "📍", "#E91E63", MarkerCategory.CUSTOM),
    PROBLEM_AREA("Problem Area", "⚠️", "#FF5722", MarkerCategory.CUSTOM),
    NOTE_BOOKMARK("Note/Bookmark", "📝", "#9E9E9E", MarkerCategory.CUSTOM);

    companion object {
        fun fromDisplayName(name: String): MapMarkerType? =
            values().find { it.displayName == name }

        fun getByCategory(category: MarkerCategory): List<MapMarkerType> =
            values().filter { it.category == category }
    }
}

enum class MarkerCategory {
    CROP,
    INFRASTRUCTURE,
    WATER,
    LIVESTOCK,
    WASTE,
    SAFETY,
    CUSTOM
}

/**
 * Heatmap overlay types
 */
enum class HeatmapType(
    val displayName: String,
    val description: String,
    val colorStart: String,
    val colorEnd: String
) {
    YIELD_PER_ACRE("Yield/Acre", "Crop yield per acre by plot", "#FFFFEB3B", "#FF2196F3"),
    COST_PER_ACRE("Cost/Acre", "Expenses per acre by plot", "#FF4CAF50", "#FFF44336"),
    ANIMAL_DENSITY("Animal Density", "Animals per area", "#FF2196F3", "#FF9C27B0"),
    SOIL_MOISTURE("Soil Moisture", "Soil moisture levels", "#FF795548", "#FF03A9F4"),
    CROP_HEALTH("Crop Health", "Health score by plot", "#FFF44336", "#FF4CAF50"),
    WATER_USAGE("Water Usage", "Water consumption by plot", "#FFE3F2FD", "#FF1565C0"),
    FEED_CONSUMPTION("Feed Consumption", "Feed usage by pen", "#FFFFF3E0", "#FFE65100"),
    REVENUE_PER_ACRE("Revenue/Acre", "Income per acre by plot", "#FFE8F5E9", "#FF2E7D32")
}