package com.shambasmart.maarifa.chunker

import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk

class SemanticChunker {
    companion object {
        private const val MAX_CHUNK_WORDS = 600
        private const val CONTEXT_WINDOW_WORDS = 50
        private val WS = Regex("\\s+")

        fun chunkDocument(
            documentText: String, sourceTitle: String, sourceType: String,
            sourceCredibility: String, topicTags: String, lastVerified: String? = null
        ): List<KnowledgeChunk> {
            val cleaned = cleanText(documentText)
            if (cleaned.isBlank()) return emptyList()
            val sections = splitIntoSections(cleaned)
            val rawChunks = mutableListOf<RawChunk>()
            for (section in sections) {
                if (section.body.isBlank()) continue
                val words = section.body.split(WS)
                if (words.size <= MAX_CHUNK_WORDS) {
                    rawChunks.add(RawChunk(section.body, section.header))
                } else {
                    val paragraphs = section.body.split("\n\n", "\r\n\r\n")
                    val buffer = StringBuilder()
                    for (para in paragraphs) {
                        val pw = para.trim().split(WS).size
                        val bw = buffer.toString().split(WS).size
                        if (bw + pw > MAX_CHUNK_WORDS && buffer.isNotBlank()) {
                            rawChunks.add(RawChunk(buffer.toString().trim(), section.header))
                            buffer.clear()
                        }
                        if (buffer.isNotEmpty()) buffer.append("\n\n")
                        buffer.append(para.trim())
                    }
                    if (buffer.isNotBlank()) {
                        rawChunks.add(RawChunk(buffer.toString().trim(), section.header))
                    }
                }
            }
            val domainTag = topicTags.split(",").firstOrNull()?.trim() ?: "general"
            return rawChunks.mapIndexed { index, raw ->
                val id = generateChunkId(sourceTitle, index)
                val displayText = raw.text.trim()
                val sectionTag = raw.sectionHeader?.let { " [Section: $it]" } ?: ""
                val embText = "[Source: $sourceTitle] [Tags: $topicTags]$sectionTag [Content follows:] $displayText"
                val prevTail = if (index > 0) getLastWords(rawChunks[index-1].text, CONTEXT_WINDOW_WORDS) else null
                val nextHead = if (index < rawChunks.size-1) getFirstWords(rawChunks[index+1].text, CONTEXT_WINDOW_WORDS) else null
                val keywords = extractKeywords(displayText)
                KnowledgeChunk(
                    id = id,
                    displayText = displayText,
                    embeddingText = embText,
                    sourceDocumentId = "",
                    sourceTitle = sourceTitle,
                    sourceType = sourceType,
                    sourceCredibility = sourceCredibility,
                    domainTag = domainTag,
                    topicTags = topicTags,
                    sectionHeader = raw.sectionHeader,
                    chunkIndex = index,
                    totalChunks = rawChunks.size,
                    prevChunkTail = prevTail,
                    nextChunkHead = nextHead,
                    medicalContent = detectMedicalContent(displayText),
                    language = "en",
                    keywords = keywords,
                    embedding = null,
                    dateAdded = System.currentTimeMillis(),
                    lastVerified = lastVerified?.toLongOrNull()
                )
            }
        }
        private fun splitIntoSections(text: String): List<Section> {
            val lines = text.lines(); val sections = mutableListOf<Section>()
            var header: String? = null; val body = StringBuilder()
            for (line in lines) {
                val t = line.trim()
                val isHeader = t.matches(Regex("^[A-Z][A-Z\\s]{3,}$")) || t.startsWith("#")
                if (isHeader && body.isNotBlank()) {
                    sections.add(Section(header, body.toString().trim())); body.clear()
                    header = t.removePrefix("#").trim()
                } else if (isHeader) { header = t.removePrefix("#").trim() }
                else { if (body.isNotEmpty()) body.append("\n"); body.append(line) }
            }
            if (body.isNotBlank()) sections.add(Section(header, body.toString().trim()))
            return if (sections.isEmpty()) listOf(Section(null, text)) else sections
        }
        private fun detectMedicalContent(text: String): Boolean {
            val indicators = listOf("dose","dosage","mg/kg","ml/kg","injection","withdrawal",
                "oxytetracycline","ivermectin","penicillin","amoxicillin","albendazole",
                "vaccine","vaccination","antibiotic","intramuscular","subcutaneous")
            val lower = text.lowercase()
            return indicators.count { lower.contains(it) } >= 2
        }
        private fun cleanText(t: String) = t.replace("\r\n","\n").replace("\r","\n")
            .replace(Regex("\n{3,}"),"\n\n").trim()
        private fun getLastWords(t: String, n: Int) = t.trim().split(WS).let {
            if(it.size<=n) t.trim() else it.takeLast(n).joinToString(" ") }
        private fun getFirstWords(t: String, n: Int) = t.trim().split(WS).let {
            if(it.size<=n) t.trim() else it.take(n).joinToString(" ") }
        private fun generateChunkId(src: String, idx: Int) =
            "chunk_${src.hashCode().toString().takeLast(6)}_${idx.toString().padStart(4,'0')}"

        private fun extractKeywords(text: String): String {
            val words = text.lowercase().split(Regex("\\s+"))
                .filter { it.length > 3 }
                .distinct()
                .take(20)
            return words.joinToString(",")
        }
    }
    private data class Section(val header: String?, val body: String)
    private data class RawChunk(val text: String, val sectionHeader: String?)
}