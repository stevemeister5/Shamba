package com.shambasmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.shambasmart.presentation.common.theme.ShambaSmartTheme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                    ShambaSmartApp()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun ShambaSmartApp() {
    // TODO: Implement navigation and main app composable
    // This will be expanded in subsequent tasks
    androidx.compose.material3.Text(
        text = "Shamba Smart - Farm Management",
        style = MaterialTheme.typography.headlineMedium
    )
}