package com.shambasmart.data.local.entity

/**
 * Livestock types supported by Shamba Smart
 */
enum class LivestockType(val displayName: String, val category: String) {
    GOAT("Goat", "small_ruminant"),
    SHEEP("Sheep", "small_ruminant"),
    CATTLE("Cattle", "large_ruminant"),
    CHICKEN_LAYER("Chicken (Layer)", "poultry"),
    CHICKEN_BROILER("Chicken (Broiler)", "poultry"),
    PIG("Pig", "swine"),
    DUCK("Duck", "poultry");

    companion object {
        fun fromSpecies(species: String): LivestockType? {
            return values().find { 
                it.name.equals(species, ignoreCase = true) || 
                it.displayName.equals(species, ignoreCase = true)
            }
        }

        fun getAllSpecies(): List<String> = values().map { it.displayName }

        fun getPoultryTypes(): List<LivestockType> = values().filter { it.category == "poultry" }

        fun getRuminantTypes(): List<LivestockType> = values().filter { 
            it.category == "small_ruminant" || it.category == "large_ruminant" 
        }
    }
}