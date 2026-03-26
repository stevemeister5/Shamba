package com.shambasmart.presentation.ml.npu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.ml.DelegateConfig
import com.shambasmart.ml.ModelInfo
import com.shambasmart.ml.ModelOptimizer
import com.shambasmart.ml.NpuCapabilities
import com.shambasmart.ml.PerformanceMetrics
import com.shambasmart.ml.QuantizationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NpuConfigViewModel @Inject constructor(
    private val modelOptimizer: ModelOptimizer
) : ViewModel() {

    // State flows
    private val _npuCapabilities = MutableStateFlow(NpuCapabilities(
        hasNnapi = false,
        hasGpu = false,
        hasQnn = false,
        hasXnnpack = false,
        recommendedDelegate = "CPU"
    ))
    val npuCapabilities: StateFlow<NpuCapabilities> = _npuCapabilities.asStateFlow()

    private val _delegateConfig = MutableStateFlow(DelegateConfig(
        useXnnpack = true,
        useGpu = false,
        useNnapi = false,
        useQnn = false,
        numThreads = 2,
        enableProfiling = false
    ))
    val delegateConfig: StateFlow<DelegateConfig> = _delegateConfig.asStateFlow()

    private val _performanceMetrics = MutableStateFlow<PerformanceMetrics?>(null)
    val performanceMetrics: StateFlow<PerformanceMetrics?> = _performanceMetrics.asStateFlow()

    private val _isProfiling = MutableStateFlow(false)
    val isProfiling: StateFlow<Boolean> = _isProfiling.asStateFlow()

    private val _modelInfo = MutableStateFlow<ModelInfo?>(null)
    val modelInfo: StateFlow<ModelInfo?> = _modelInfo.asStateFlow()

    init {
        checkCapabilities()
        loadModelInfo()
    }

    private fun checkCapabilities() {
        viewModelScope.launch {
            val capabilities = modelOptimizer.checkNpuCapabilities()
            _npuCapabilities.value = capabilities
            
            // Update delegate config based on capabilities
            _delegateConfig.value = _delegateConfig.value.copy(
                useQnn = capabilities.hasQnn,
                useNnapi = capabilities.hasNnapi,
                useGpu = capabilities.hasGpu,
                useXnnpack = capabilities.hasXnnpack
            )
        }
    }

    private fun loadModelInfo() {
        viewModelScope.launch {
            // Load info for a sample model
            val info = modelOptimizer.getModelInfo("audio_classifier.tflite")
            _modelInfo.value = info
        }
    }

    fun updateDelegateConfig(config: DelegateConfig) {
        _delegateConfig.value = config
    }

    fun toggleProfiling() {
        val newValue = !_isProfiling.value
        _isProfiling.value = newValue
        
        if (newValue) {
            // Start performance monitoring
            val startTime = modelOptimizer.startPerformanceMonitoring()
            
            viewModelScope.launch {
                // Simulate some inference work
                delay(1000)
                
                // Stop monitoring and get metrics
                val metrics = modelOptimizer.stopPerformanceMonitoring(startTime)
                _performanceMetrics.value = metrics
            }
        }
    }

    fun quantizeModel(quantizationType: QuantizationType) {
        viewModelScope.launch {
            // In a real implementation, this would trigger model quantization
            val success = modelOptimizer.quantizeModel(
                modelPath = "audio_classifier.tflite",
                outputPath = "audio_classifier_${quantizationType.name.lowercase()}.tflite",
                quantizationType = quantizationType
            )
            
            if (success) {
                // Update model info with new quantization
                val updatedInfo = _modelInfo.value?.copy(
                    quantizationType = quantizationType.name,
                    isOptimized = true
                )
                _modelInfo.value = updatedInfo
            }
        }
    }

    fun runBenchmark() {
        viewModelScope.launch {
            // Run a quick benchmark
            val startTime = modelOptimizer.startPerformanceMonitoring()
            
            // Simulate multiple inference runs
            repeat(10) {
                delay(100) // Simulate inference
            }
            
            val metrics = modelOptimizer.stopPerformanceMonitoring(startTime)
            _performanceMetrics.value = metrics
        }
    }
}