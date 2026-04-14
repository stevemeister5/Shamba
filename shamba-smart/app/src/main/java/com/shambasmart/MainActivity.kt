package com.shambasmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.shambasmart.data.preferences.OnboardingPreferences
import com.shambasmart.data.preferences.FarmProfilePreferences
import com.shambasmart.presentation.navigation.ShambaNavGraph
import com.shambasmart.presentation.common.theme.ShambaSmartTheme
import com.shambasmart.maarifa.MaarifaViewModel
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import javax.inject.Inject
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences
    
    @Inject
    lateinit var farmProfilePreferences: FarmProfilePreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure OSMDroid
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(this, getPreferences(MODE_PRIVATE))
        
        enableEdgeToEdge()
        setContent {
            ShambaSmartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val isOnboardingCompleted by onboardingPreferences.isOnboardingCompleted
                        .collectAsState(initial = false)
                    val isLaunchChoiceMade by onboardingPreferences.isLaunchChoiceMade
                        .collectAsState(initial = false)
                    val farmName by farmProfilePreferences.farmName
                        .collectAsState(initial = "Shamba Smart")
                    val maarifaViewModel: MaarifaViewModel = hiltViewModel()
                    
                    ShambaNavGraph(
                        navController = navController,
                        isOnboardingCompleted = isOnboardingCompleted,
                        isLaunchChoiceMade = isLaunchChoiceMade,
                        farmName = farmName.ifEmpty { "Shamba Smart" },
                        weatherSummary = "24°C Sunny",
                        maarifaViewModel = maarifaViewModel
                    )
                }
            }
        }
    }
}
