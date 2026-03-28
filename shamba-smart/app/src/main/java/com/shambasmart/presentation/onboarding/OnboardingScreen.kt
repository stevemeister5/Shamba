package com.shambasmart.presentation.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.shambasmart.presentation.common.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0) { uiState.totalPages }
    val scope = rememberCoroutineScope()

    // Permission launcher
    val permissionsToRequest = mutableListOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }.toTypedArray()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        viewModel.setPermissionsGranted(allGranted)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setPage(pagerState.currentPage)
    }

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = { },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.completeOnboarding()
                            onComplete()
                        }
                    ) {
                        Text(
                            text = "Skip",
                            color = Neutral600
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceBase
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Space.space8)
                    .padding(bottom = Space.space8)
            ) {
                // Page indicators with 120ms animation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(uiState.totalPages) { index ->
                        val isSelected = index == pagerState.currentPage
                        val color by animateColorAsState(
                            targetValue = if (isSelected) Green500 else Neutral300,
                            animationSpec = tween(durationMillis = Timing.tabSwitch),
                            label = "page_indicator"
                        )
                        val width by animateDpAsState(
                            targetValue = if (isSelected) 24.dp else 8.dp,
                            animationSpec = tween(durationMillis = Timing.tabSwitch),
                            label = "page_indicator_width"
                        )
                        Box(
                            modifier = Modifier
                                .width(width)
                                .height(8.dp)
                                .padding(horizontal = 2.dp)
                        ) {
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                colors = CardDefaults.cardColors(containerColor = color),
                                shape = androidx.compose.foundation.shape.CircleShape
                            ) {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Space.space6))

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (pagerState.currentPage > 0) {
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage - 1,
                                        animationSpec = tween(durationMillis = Timing.pageTransition)
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(Dimensions.buttonHeight),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                modifier = Modifier.size(IconSize.button)
                            )
                            Spacer(modifier = Modifier.width(Space.space2))
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(Space.space4))
                    }

                    Button(
                        onClick = {
                            if (pagerState.currentPage < uiState.totalPages - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage + 1,
                                        animationSpec = tween(durationMillis = Timing.pageTransition)
                                    )
                                }
                            } else {
                                viewModel.completeOnboarding()
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(Dimensions.buttonHeight),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Green500
                        ),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(Radius.md)
                    ) {
                        Text(
                            text = if (pagerState.currentPage == uiState.totalPages - 1) "Get Started" else "Next",
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(Space.space2))
                        Icon(
                            imageVector = if (pagerState.currentPage == uiState.totalPages - 1) 
                                Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.button)
                        )
                    }
                }
            }
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { page ->
            when (page) {
                0 -> WelcomeScreen()
                1 -> FeaturesScreen()
                2 -> PermissionsScreen(
                    onRequestPermissions = {
                        permissionLauncher.launch(permissionsToRequest)
                    },
                    permissionsGranted = uiState.permissionsGranted
                )
            }
        }
    }
}
