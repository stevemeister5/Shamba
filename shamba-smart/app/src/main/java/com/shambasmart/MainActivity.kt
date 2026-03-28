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
import com.shambasmart.presentation.navigation.ShambaNavGraph
import com.shambasmart.presentation.common.theme.ShambaSmartTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var onboardingPreferences: OnboardingPreferences
    
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
                    
                    ShambaNavGraph(
                        navController = navController,
                        isOnboardingCompleted = isOnboardingCompleted
                    )
                }
            }
        }
    }
}
