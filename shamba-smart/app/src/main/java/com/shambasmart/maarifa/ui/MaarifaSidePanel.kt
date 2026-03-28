package com.shambasmart.maarifa.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.maarifa.*
import com.shambasmart.maarifa.retrieval.ResponseAssembler

/**
 * Maarifa Side Panel — slides out from right edge without navigating away.
 *
 * Design spec: "The panel has three tabs: Ask tab, Browse tab, Saved tab."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaarifaSidePanel(
    isOpen: Boolean,
    selectedTab: MaarifaTab,
    onTabSelected: (MaarifaTab) -> Unit,
    onClose: () -> Unit,
    viewModel: MaarifaViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val conversationHistory by viewModel.conversationHistory.collectAsState()
    val browseEntries by viewModel.browseEntries.collectAsState()
    val savedEntries by viewModel.savedEntries.collectAsState()

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }),
        exit = slideOutHorizontally(targetOffsetX = { it })
    ) {
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .width(420.dp),
            shadowElevation = 16.dp,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Maarifa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Tab bar
                TabRow(selectedTabIndex = selectedTab.ordinal) {
                    MaarifaTab.values().forEach { tab ->
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { onTabSelected(tab) },
                            text = {
                                Text(
                                    when (tab) {
                                        MaarifaTab.ASK -> "Ask"
                                        MaarifaTab.BROWSE -> "Browse"
                                        MaarifaTab.SAVED -> "Saved"
                                    }
                                )
                            },
                            icon = {
                                Icon(
                                    when (tab) {
                                        MaarifaTab.ASK -> Icons.Default.Chat
                                        MaarifaTab.BROWSE -> Icons.Default.MenuBook
                                        MaarifaTab.SAVED -> Icons.Default.Bookmark
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                // Tab content
                when (selectedTab) {
                    MaarifaTab.ASK -> AskTabContent(
                        viewModel = viewModel,
                        uiState = uiState,
                        conversationHistory = conversationHistory
                    )
                    MaarifaTab.BROWSE -> BrowseTabContent(
                        viewModel = viewModel,
                        browseEntries = browseEntries,
                        searchResults = uiState.browseSearchResults
                    )
                    MaarifaTab.SAVED -> SavedTabContent(
                        viewModel = viewModel,
                        savedEntries = savedEntries
                    )
                }
            }
        }
    }
}

// === ASK TAB ===

@Composable
private fun AskTabContent(
    viewModel: MaarifaViewModel,
    uiState: MaarifaUiState,
    conversationHistory: List<ConversationEntry>
) {
    var queryText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Conversation history (scrollable)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            reverseLayout = true
        ) {
            items(conversationHistory) { entry ->
                ConversationEntryCard(entry = entry)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        // Symptom checker button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.startSymptomChecker() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MedicalServices, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Open Symptom Checker")
            }
        }

        // Query input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                placeholder = { Text("Ask Maarifa anything...") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    if (queryText.isNotBlank()) {
                        viewModel.submitQuery(queryText.trim())
                        queryText = ""
                    }
                },
                enabled = queryText.isNotBlank() && !uiState.isLoading
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ConversationEntryCard(entry: ConversationEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Query
            Text(
                text = entry.query,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Answer summary
            Text(
                text = entry.answer.summary,
                style = MaterialTheme.typography.bodyMedium
            )

            // Confidence indicator
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = when (entry.answer.confidenceTier) {
                        1 -> Icons.Default.Verified
                        2 -> Icons.Default.CheckCircle
                        3 -> Icons.Default.Warning
                        else -> Icons.Default.ErrorOutline
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = when (entry.answer.confidenceTier) {
                        1 -> MaterialTheme.colorScheme.primary
                        2 -> MaterialTheme.colorScheme.tertiary
                        3 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.outline
                    }
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = entry.answer.confidenceLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Warnings
            entry.answer.warnings.forEach { warning ->
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (warning.type) {
                            ResponseAssembler.WarningType.NOTIFIABLE_DISEASE,
                            ResponseAssembler.WarningType.OUTBREAK_ALERT ->
                                MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.tertiaryContainer
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠ ${warning.message}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = when (warning.type) {
                            ResponseAssembler.WarningType.NOTIFIABLE_DISEASE,
                            ResponseAssembler.WarningType.OUTBREAK_ALERT ->
                                MaterialTheme.colorScheme.onErrorContainer
                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                        }
                    )
                }
            }

            // Sources
            if (entry.answer.sources.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sources: ${entry.answer.sources.joinToString(", ") { it.title }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

// === BROWSE TAB ===

@Composable
private fun BrowseTabContent(
    viewModel: MaarifaViewModel,
    browseEntries: List<BrowseEntry>,
    searchResults: List<com.shambasmart.data.local.entity.maarifa.KnowledgeChunk>
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                if (it.length >= 2) viewModel.browseSearch(it)
                else viewModel.clearBrowseSearch()
            },
            placeholder = { Text("Search knowledge base...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (searchResults.isNotEmpty()) {
            // Show search results
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(searchResults) { chunk ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* navigate to chunk detail */ }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = chunk.sourceTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = chunk.text.take(150) + if (chunk.text.length > 150) "..." else "",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = chunk.topicTags,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        } else {
            // Show browse tree
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(browseEntries) { entry ->
                    BrowseEntryItem(entry = entry, onSearch = { viewModel.browseSearch(it) })
                }
            }
        }
    }
}

@Composable
private fun BrowseEntryItem(
    entry: BrowseEntry,
    depth: Int = 0,
    onSearch: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    if (entry.children.isNotEmpty()) expanded = !expanded
                    else onSearch(entry.name)
                }
                .padding(start = (depth * 16).dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.children.isNotEmpty()) {
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Spacer(modifier = Modifier.width(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = entry.name,
                style = if (depth == 0) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.bodyMedium,
                fontWeight = if (depth == 0) FontWeight.Bold else FontWeight.Normal
            )
        }

        if (expanded) {
            entry.children.forEach { child ->
                BrowseEntryItem(entry = child, depth = depth + 1, onSearch = onSearch)
            }
        }
    }
}

// === SAVED TAB ===

@Composable
private fun SavedTabContent(
    viewModel: MaarifaViewModel,
    savedEntries: List<com.shambasmart.data.local.entity.maarifa.KnowledgeChunk>
) {
    if (savedEntries.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No saved entries yet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    "Bookmark knowledge entries from answers or Browse tab",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    } else {
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(savedEntries) { chunk ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = chunk.sourceTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.removeBookmark(chunk.chunkId) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.BookmarkRemove, contentDescription = "Remove")
                            }
                        }
                        Text(
                            text = chunk.text.take(120) + "...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}