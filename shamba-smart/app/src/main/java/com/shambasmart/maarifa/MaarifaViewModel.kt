package com.shambasmart.maarifa

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shambasmart.data.local.dao.maarifa.KnowledgeChunkDao
import com.shambasmart.data.local.dao.maarifa.OperationalRuleDao
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import com.shambasmart.maarifa.contextbridge.ContextBridge
import com.shambasmart.maarifa.retrieval.*
import com.shambasmart.maarifa.rules.RuleEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Maarifa ViewModel — orchestrates the full retrieval pipeline.
 *
 * Design spec: "A persistent floating tab on the right edge of the tablet
 * display is visible on every screen at all times. Tapping it slides out
 * a side panel without navigating away from the current screen."
 */
@HiltViewModel
class MaarifaViewModel @Inject constructor(
    application: Application,
    private val chunkDao: KnowledgeChunkDao,
    private val ruleDao: OperationalRuleDao,
    private val contextBridge: ContextBridge,
    private val ruleEngine: RuleEngine,
    private val retriever: KnowledgeRetriever
) : AndroidViewModel(application) {

    private val intentClassifier = IntentClassifier()
    private val vectorEngine = VectorSearchEngine(application)

    // UI State
    private val _uiState = MutableStateFlow(MaarifaUiState())
    val uiState: StateFlow<MaarifaUiState> = _uiState.asStateFlow()

    // Conversation history
    private val _conversationHistory = MutableStateFlow<List<ConversationEntry>>(emptyList())
    val conversationHistory: StateFlow<List<ConversationEntry>> = _conversationHistory.asStateFlow()

    // Browse tree
    private val _browseEntries = MutableStateFlow<List<BrowseEntry>>(emptyList())
    val browseEntries: StateFlow<List<BrowseEntry>> = _browseEntries.asStateFlow()

    // Saved/bookmarked entries
    private val _savedEntries = MutableStateFlow<List<KnowledgeChunk>>(emptyList())
    val savedEntries: StateFlow<List<KnowledgeChunk>> = _savedEntries.asStateFlow()

    // Knowledge stats
    private val _knowledgeStats = MutableStateFlow(KnowledgeRetriever.KnowledgeStats(0, 0, 0, 0))
    val knowledgeStats: StateFlow<KnowledgeRetriever.KnowledgeStats> = _knowledgeStats.asStateFlow()

    // Symptom checker state
    private val _symptomState = MutableStateFlow(SymptomCheckerState())
    val symptomState: StateFlow<SymptomCheckerState> = _symptomState.asStateFlow()

    init {
        viewModelScope.launch {
            vectorEngine.initialize()
            loadStats()
            loadBrowseEntries()
        }
    }

    // === ASK TAB ===

    fun submitQuery(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentAnswer = null) }

            // 1. Intent classification + entity extraction
            val classification = intentClassifier.classify(query)

            // 2. Context injection
            val context = contextBridge.buildContext(classification.entities)

            // 3. Knowledge retrieval
            val chunks = retriever.retrieve(
                query = query,
                species = classification.entities.species,
                crop = classification.entities.crop,
                intent = classification.primaryIntent
            )

            // 4. Response assembly
            val answer = ResponseAssembler(ruleEngine, retriever).assembleAnswer(
                query = query,
                chunks = chunks,
                context = context,
                classification = classification
            )

            // 5. Save to conversation history
            val entry = ConversationEntry(
                query = query,
                answer = answer,
                timestamp = System.currentTimeMillis(),
                classification = classification
            )
            _conversationHistory.update { listOf(entry) + it }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    currentAnswer = answer,
                    currentContext = context
                )
            }
        }
    }

    // === BROWSE TAB ===

    fun loadBrowseEntries() {
        viewModelScope.launch {
            val entries = listOf(
                BrowseEntry("Crops", "crops", listOf(
                    BrowseEntry("Maize", "crops/maize", emptyList()),
                    BrowseEntry("Beans", "crops/beans", emptyList()),
                    BrowseEntry("Cassava", "crops/cassava", emptyList()),
                    BrowseEntry("Napier Grass", "crops/napier", emptyList()),
                    BrowseEntry("Sweet Potato", "crops/sweet_potato", emptyList()),
                    BrowseEntry("Tomatoes", "crops/tomatoes", emptyList()),
                    BrowseEntry("Kale (Sukuma Wiki)", "crops/kale", emptyList()),
                    BrowseEntry("Onions", "crops/onion", emptyList()),
                    BrowseEntry("Sorghum", "crops/sorghum", emptyList())
                )),
                BrowseEntry("Livestock", "livestock", listOf(
                    BrowseEntry("Goats", "livestock/goats", listOf(
                        BrowseEntry("Health & Disease", "livestock/goats/health", emptyList()),
                        BrowseEntry("Breeds", "livestock/goats/breeds", emptyList()),
                        BrowseEntry("Reproduction", "livestock/goats/reproduction", emptyList()),
                        BrowseEntry("Nutrition", "livestock/goats/nutrition", emptyList()),
                        BrowseEntry("Kid Management", "livestock/goats/kids", emptyList())
                    )),
                    BrowseEntry("Sheep", "livestock/sheep", listOf(
                        BrowseEntry("Health & Disease", "livestock/sheep/health", emptyList()),
                        BrowseEntry("Breeds", "livestock/sheep/breeds", emptyList()),
                        BrowseEntry("Reproduction", "livestock/sheep/reproduction", emptyList()),
                        BrowseEntry("Nutrition", "livestock/sheep/nutrition", emptyList())
                    ))
                )),
                BrowseEntry("Medicines", "medicines", listOf(
                    BrowseEntry("Antibiotics", "medicines/antibiotics", emptyList()),
                    BrowseEntry("Anthelmintics (Dewormers)", "medicines/dewormers", emptyList()),
                    BrowseEntry("Anti-inflammatories", "medicines/anti_inflammatory", emptyList()),
                    BrowseEntry("Vaccines", "medicines/vaccines", emptyList()),
                    BrowseEntry("Supportive Care", "medicines/supportive", emptyList())
                )),
                BrowseEntry("Cheese & Dairy", "cheese", listOf(
                    BrowseEntry("Fresh Chevre", "cheese/chevre", emptyList()),
                    BrowseEntry("Feta-style", "cheese/feta", emptyList()),
                    BrowseEntry("Queso Fresco", "cheese/queso_fresco", emptyList()),
                    BrowseEntry("Ricotta", "cheese/ricotta", emptyList()),
                    BrowseEntry("Halloumi", "cheese/halloumi", emptyList()),
                    BrowseEntry("Milk Quality", "cheese/milk_quality", emptyList()),
                    BrowseEntry("Defects Reference", "cheese/defects", emptyList())
                )),
                BrowseEntry("Weather & Seasons", "weather", listOf(
                    BrowseEntry("Korogwe Climate Calendar", "weather/calendar", emptyList()),
                    BrowseEntry("Seasonal Management", "weather/seasons", emptyList()),
                    BrowseEntry("Disease Risk Calendar", "weather/disease_risk", emptyList())
                )),
                BrowseEntry("Veterinary Formulary", "formulary", emptyList())
            )
            _browseEntries.value = entries
        }
    }

    fun browseSearch(query: String) {
        viewModelScope.launch {
            val chunks = retriever.retrieve(
                query = query,
                intent = "general_lookup"
            )
            _uiState.update { it.copy(browseSearchResults = chunks.map { sc -> sc.chunk }) }
        }
    }

    fun clearBrowseSearch() {
        _uiState.update { it.copy(browseSearchResults = emptyList()) }
    }

    // === SAVED TAB ===

    fun loadSavedEntries() {
        viewModelScope.launch {
            // Load bookmarked entries from DataStore or Room
            _savedEntries.value = emptyList()
        }
    }

    fun bookmarkEntry(chunk: KnowledgeChunk) {
        viewModelScope.launch {
            _savedEntries.update { it + chunk }
        }
    }

    fun removeBookmark(chunkId: String) {
        viewModelScope.launch {
            _savedEntries.update { it.filter { c -> c.chunkId != chunkId } }
        }
    }

    // === SYMPTOM CHECKER ===

    fun startSymptomChecker() {
        _symptomState.value = SymptomCheckerState(currentStep = 1)
        _uiState.update { it.copy(showSymptomChecker = true) }
    }

    fun symptomCheckerSelectSpecies(species: String) {
        _symptomState.update { it.copy(species = species, currentStep = 2) }
    }

    fun symptomCheckerSelectAnimal(animalId: String?) {
        _symptomState.update { it.copy(animalId = animalId, currentStep = 3) }
    }

    fun symptomCheckerSelectBodySystem(system: String) {
        _symptomState.update { it.copy(bodySystem = system, currentStep = 4) }
    }

    fun symptomCheckerToggleSymptom(symptom: String) {
        _symptomState.update {
            val symptoms = if (symptom in it.selectedSymptoms) {
                it.selectedSymptoms - symptom
            } else {
                it.selectedSymptoms + symptom
            }
            it.copy(selectedSymptoms = symptoms)
        }
    }

    fun symptomCheckerSetDuration(duration: String) {
        _symptomState.update { it.copy(duration = duration, currentStep = 6) }
    }

    fun symptomCheckerSetAffectedCount(count: String) {
        _symptomState.update { it.copy(affectedCount = count, currentStep = 7) }
    }

    fun symptomCheckerSetRecentEvents(events: List<String>) {
        _symptomState.update { it.copy(recentEvents = events, currentStep = 8) }
    }

    fun symptomCheckerSubmit() {
        viewModelScope.launch {
            _symptomState.update { it.copy(isLoading = true) }

            val state = _symptomState.value
            val query = buildString {
                append("${state.species} showing ${state.selectedSymptoms.joinToString(", ")}")
                if (state.bodySystem.isNotEmpty()) append(" — ${state.bodySystem} system")
                if (state.duration.isNotEmpty()) append(" — duration: ${state.duration}")
            }

            val classification = intentClassifier.classify(query, species = state.species)
            val context = contextBridge.buildContext(classification.entities)
            val chunks = retriever.retrieve(
                query = query,
                species = state.species,
                intent = "symptom_query"
            )

            val answer = ResponseAssembler(ruleEngine, retriever).assembleAnswer(
                query = query,
                chunks = chunks,
                context = context,
                classification = classification
            )

            _symptomState.update {
                it.copy(isLoading = false, result = answer)
            }
        }
    }

    fun closeSymptomChecker() {
        _symptomState.value = SymptomCheckerState()
        _uiState.update { it.copy(showSymptomChecker = false) }
    }

    // === PANEL CONTROL ===

    fun togglePanel() {
        _uiState.update { it.copy(isPanelOpen = !it.isPanelOpen) }
    }

    fun openPanel() {
        _uiState.update { it.copy(isPanelOpen = true) }
    }

    fun closePanel() {
        _uiState.update { it.copy(isPanelOpen = false) }
    }

    fun selectTab(tab: MaarifaTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    // === STATS ===

    private suspend fun loadStats() {
        _knowledgeStats.value = retriever.getStats()
    }

    override fun onCleared() {
        super.onCleared()
        vectorEngine.close()
    }
}

// === DATA CLASSES ===

data class MaarifaUiState(
    val isPanelOpen: Boolean = false,
    val selectedTab: MaarifaTab = MaarifaTab.ASK,
    val isLoading: Boolean = false,
    val currentAnswer: ResponseAssembler.MaarifaAnswer? = null,
    val currentContext: ContextBridge.FarmContext? = null,
    val showSymptomChecker: Boolean = false,
    val browseSearchResults: List<com.shambasmart.data.local.entity.maarifa.KnowledgeChunk> = emptyList()
)

enum class MaarifaTab { ASK, BROWSE, SAVED }

data class ConversationEntry(
    val query: String,
    val answer: ResponseAssembler.MaarifaAnswer,
    val timestamp: Long,
    val classification: IntentClassifier.ClassificationResult
)

data class BrowseEntry(
    val name: String,
    val path: String,
    val children: List<BrowseEntry>
)

data class SymptomCheckerState(
    val currentStep: Int = 1,
    val species: String = "",
    val animalId: String? = null,
    val bodySystem: String = "",
    val selectedSymptoms: List<String> = emptyList(),
    val duration: String = "",
    val affectedCount: String = "",
    val recentEvents: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val result: ResponseAssembler.MaarifaAnswer? = null
)