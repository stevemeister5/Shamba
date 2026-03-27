package com.shambasmart.maarifa.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shambasmart.maarifa.ingestion.KnowledgeIngestionPipeline

/**
 * Knowledge Inbox — manage ingested documents.
 *
 * Design spec: "Knowledge Inbox management screen shows:
 * Library list, processing status bar, delete document option,
 * source visibility in every Maarifa answer."
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeInboxScreen(
    documents: List<KnowledgeIngestionPipeline.DocumentInfo>,
    isLoading: Boolean,
    ingestionResult: KnowledgeIngestionPipeline.IngestionResult?,
    onDeleteDocument: (String) -> Unit,
    onIngestText: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var showIngestDialog by remember { mutableStateOf(false) }
    var ingestTitle by remember { mutableStateOf("") }
    var ingestText by remember { mutableStateOf("") }
    var ingestDomain by remember { mutableStateOf("general") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Inbox") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showIngestDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add document")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Knowledge Library", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${documents.size} documents ingested")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Processing indicator
            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("Processing document...", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Last ingestion result
            ingestionResult?.let { result ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.success) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            if (result.success) "✓ ${result.chunksCreated} chunks created from '${result.documentTitle}'"
                            else "✗ Failed: ${result.errors.joinToString()}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (result.warnings.isNotEmpty()) {
                            result.warnings.forEach { warning ->
                                Text("⚠ $warning", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Document list
            if (documents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.LibraryBooks, contentDescription = null,
                            modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No documents ingested yet", color = MaterialTheme.colorScheme.outline)
                        Text("Tap + to add a document or paste text", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            } else {
                LazyColumn {
                    items(documents) { doc ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(doc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text("${doc.chunkCount} chunks • ${doc.sourceType} • Added ${doc.dateAdded}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline)
                                }
                                IconButton(onClick = { onDeleteDocument(doc.title) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Ingest dialog
    if (showIngestDialog) {
        AlertDialog(
            onDismissRequest = { showIngestDialog = false },
            title = { Text("Add Knowledge") },
            text = {
                Column {
                    OutlinedTextField(
                        value = ingestTitle,
                        onValueChange = { ingestTitle = it },
                        label = { Text("Document Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Domain selector
                    Text("Domain", style = MaterialTheme.typography.labelMedium)
                    val domains = listOf("general", "crops", "livestock", "medicines", "cheese", "weather")
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        domains.forEach { domain ->
                            FilterChip(
                                selected = ingestDomain == domain,
                                onClick = { ingestDomain = domain },
                                label = { Text(domain, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = ingestText,
                        onValueChange = { ingestText = it },
                        label = { Text("Paste text content here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Minimum 100 words. English only.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ingestTitle.isNotBlank() && ingestText.isNotBlank()) {
                            onIngestText(ingestTitle, ingestText, ingestDomain)
                            showIngestDialog = false
                            ingestTitle = ""
                            ingestText = ""
                        }
                    },
                    enabled = ingestTitle.isNotBlank() && ingestText.isNotBlank()
                ) {
                    Text("Ingest")
                }
            },
            dismissButton = {
                TextButton(onClick = { showIngestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}