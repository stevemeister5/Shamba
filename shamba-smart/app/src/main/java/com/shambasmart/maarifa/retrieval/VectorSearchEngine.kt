package com.shambasmart.maarifa.retrieval

import android.content.Context
import com.shambasmart.data.local.entity.maarifa.KnowledgeChunk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.nio.FloatBuffer
import kotlin.math.sqrt

/**
 * Maarifa Vector Search Engine.
 *
 * Uses all-MiniLM-L6-v2 via ONNX Runtime for 384-dimension text embeddings.
 * CPU only, no GPU required. ~23MB model.
 *
 * Design spec: "The only model bundled is all-MiniLM-L6-v2 in ONNX format (~23MB)
 * used exclusively for generating text embeddings at document ingestion time and query time."
 */
class VectorSearchEngine(private val context: Context) {

    private var ortEnv: OrtEnvironment? = null
    private var session: OrtSession? = null
    private val embeddingDim = 384

    data class VectorMatch(
        val chunk: KnowledgeChunk,
        val cosineSimilarity: Float
    )

    suspend fun initialize() = withContext(Dispatchers.IO) {
        try {
            ortEnv = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setIntraOpNumThreads(4)
            sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT)

            val modelBytes = loadModelFile()
            if (modelBytes != null) {
                session = ortEnv!!.createSession(modelBytes, sessionOptions)
            } else {
                android.util.Log.w("VectorSearchEngine", "Model file is null")
            }
        } catch (e: Exception) {
            // Model not available — vector search disabled, BM25 only
            android.util.Log.w("VectorSearchEngine", "ONNX model not loaded: ${e.message}")
        }
    }

    private fun loadModelFile(): ByteArray? {
        return try {
            context.assets.open("models/all_minilm_l6_v2.onnx").use { it.readBytes() }
        } catch (e: Exception) {
            android.util.Log.w("VectorSearchEngine", "Model file not found in assets")
            null
        }
    }

    suspend fun generateEmbedding(text: String): FloatArray? = withContext(Dispatchers.IO) {
        val sess = session ?: return@withContext null
        try {
            val tokenizer = SimpleTokenizer()
            val (inputIds, attentionMask, tokenTypeIds) = tokenizer.tokenize(text)

            val inputIdsArray = inputIds.map { it.toLong() }.toLongArray()
            val attentionMaskArray = attentionMask.map { it.toLong() }.toLongArray()
            val tokenTypeIdsArray = tokenTypeIds.map { it.toLong() }.toLongArray()

            val shape = longArrayOf(1, inputIdsArray.size.toLong())

            val inputIdsTensor = OnnxTensor.createTensor(ortEnv, reshape(inputIdsArray, shape))
            val attentionMaskTensor = OnnxTensor.createTensor(ortEnv, reshape(attentionMaskArray, shape))
            val tokenTypeIdsTensor = OnnxTensor.createTensor(ortEnv, reshape(tokenTypeIdsArray, shape))

            val inputs = mapOf(
                "input_ids" to inputIdsTensor,
                "attention_mask" to attentionMaskTensor,
                "token_type_ids" to tokenTypeIdsTensor
            )

            val output = sess.run(inputs)
            val result = output.get(0) as OnnxTensor
            val floatBuffer = result.value as Array<Array<FloatArray>>

            // Mean pooling across tokens
            val tokenCount = inputIdsArray.size.toFloat()
            val embedding = FloatArray(embeddingDim)
            for (j in 0 until embeddingDim) {
                var sum = 0f
                for (i in 0 until inputIdsArray.size) {
                    sum += floatBuffer[0][i][j]
                }
                embedding[j] = sum / tokenCount
            }

            // Normalize
            var norm = 0f
            for (v in embedding) norm += v * v
            norm = sqrt(norm)
            if (norm > 0) {
                for (i in embedding.indices) embedding[i] /= norm
            }

            inputIdsTensor.close()
            attentionMaskTensor.close()
            tokenTypeIdsTensor.close()
            output.close()

            embedding
        } catch (e: Exception) {
            android.util.Log.e("VectorSearchEngine", "Embedding generation failed: ${e.message}")
            null
        }
    }

    fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        if (a.size != b.size) return 0f
        var dot = 0f; var normA = 0f; var normB = 0f
        for (i in a.indices) {
            dot += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        val denom = sqrt(normA) * sqrt(normB)
        return if (denom > 0) dot / denom else 0f
    }

    suspend fun search(
        queryEmbedding: FloatArray,
        chunks: List<KnowledgeChunk>,
        topK: Int = 10
    ): List<VectorMatch> = withContext(Dispatchers.Default) {
        chunks.mapNotNull { chunk ->
            val vec = chunk.getVectorArray() ?: return@mapNotNull null
            val sim = cosineSimilarity(queryEmbedding, vec)
            if (sim > 0.2f) VectorMatch(chunk, sim) else null
        }.sortedByDescending { it.cosineSimilarity }.take(topK)
    }

    fun isAvailable(): Boolean = session != null

    fun close() {
        session?.close()
        ortEnv?.close()
    }

    private fun reshape(array: LongArray, shape: LongArray): Array<LongArray> {
        val batchSize = shape[0].toInt()
        val seqLen = shape[1].toInt()
        return Array(batchSize) { b ->
            LongArray(seqLen) { i -> array[b * seqLen + i] }
        }
    }

    /**
     * Simple tokenizer for MiniLM — converts text to token IDs.
     * Uses a basic word-to-id mapping loaded from assets.
     * For production, use a proper BPE tokenizer.
     */
    private class SimpleTokenizer {
        private val maxLength = 128
        private val padTokenId = 0
        private val clsTokenId = 101
        private val sepTokenId = 102
        private val unkTokenId = 100

        // Simplified vocab — in production, load full vocab from assets
        private val vocab = buildVocab()

        private fun buildVocab(): Map<String, Int> {
            val map = mutableMapOf<String, Int>()
            map["[PAD]"] = 0
            map["[UNK]"] = 100
            map["[CLS]"] = 101
            map["[SEP]"] = 102
            map["[MASK]"] = 103
            // Common words get IDs starting from 1000
            var id = 1000
            val commonWords = listOf(
                "the", "a", "an", "is", "are", "was", "were", "be", "been", "being",
                "have", "has", "had", "do", "does", "did", "will", "would", "shall",
                "should", "may", "might", "must", "can", "could", "to", "of", "in",
                "for", "on", "with", "at", "by", "from", "as", "into", "through",
                "during", "before", "after", "above", "below", "between", "and", "but",
                "or", "not", "no", "nor", "so", "yet", "both", "either", "neither",
                "each", "every", "all", "any", "few", "more", "most", "other", "some",
                "such", "only", "own", "same", "than", "too", "very", "just", "because",
                "if", "when", "where", "how", "what", "which", "who", "whom", "this",
                "that", "these", "those", "i", "me", "my", "we", "our", "you", "your",
                "he", "him", "his", "she", "her", "it", "its", "they", "them", "their",
                "goat", "goats", "sheep", "cow", "pig", "animal", "animals", "farm",
                "disease", "treatment", "vaccine", "vaccination", "dose", "dosage",
                "drug", "medicine", "inject", "injection", "oral", "milk", "meat",
                "withdrawal", "safe", "period", "days", "mg", "ml", "kg", "weight",
                "body", "fever", "cough", "diarrhea", "lameness", "swelling",
                "symptom", "symptoms", "sign", "signs", "cause", "treat", "treats",
                "prevent", "prevention", "control", "infection", "bacteria", "virus",
                "viral", "bacterial", "parasite", "parasitic", "fungal", "fungal",
                "crop", "crops", "plant", "planting", "seed", "seeds", "fertilizer",
                "fertiliser", "soil", "water", "irrigation", "rain", "rainfall",
                "season", "harvest", "yield", "acre", "hectare", "plot", "field",
                "maize", "beans", "cassava", "sorghum", "wheat", "rice", "tomato",
                "onion", "kale", "spinach", "napier", "silage", "hay", "feed",
                "nutrition", "deficiency", "protein", "energy", "mineral", "vitamin",
                "breed", "breeding", "mating", "heat", "oestrus", "pregnancy",
                "gestation", "birth", "kidding", "lambing", "kid", "lamb",
                "cheese", "curd", "rennet", "culture", "pasteurization", "brine",
                "weather", "temperature", "humidity", "drought", "flood",
                "dose", "route", "frequency", "duration", "twice", "daily",
                "oxytetracycline", "ivermectin", "penicillin", "amoxicillin",
                "albendazole", "levamisole", "fenbendazole", "enrofloxacin"
            )
            for (word in commonWords) {
                map[word] = id++
            }
            return map
        }

        data class TokenIds(
            val inputIds: IntArray,
            val attentionMask: IntArray,
            val tokenTypeIds: IntArray
        )

        fun tokenize(text: String): TokenIds {
            val words = text.lowercase().replace(Regex("[^a-z0-9\\s]"), "")
                .split(Regex("\\s+")).filter { it.isNotEmpty() }

            val tokens = mutableListOf<Int>(clsTokenId)
            for (word in words.take(maxLength - 2)) {
                tokens.add(vocab[word] ?: unkTokenId)
            }
            tokens.add(sepTokenId)

            val padLength = maxLength - tokens.size
            val inputIds = tokens + List(padLength) { padTokenId }
            val attentionMask = List(tokens.size) { 1 } + List(padLength) { 0 }
            val tokenTypeIds = List(maxLength) { 0 }

            return TokenIds(inputIds.toIntArray(), attentionMask.toIntArray(), tokenTypeIds.toIntArray())
        }

        private fun LongArray.reshape(shape: LongArray): Array<LongArray> {
            val batchSize = shape[0].toInt()
            val seqLen = shape[1].toInt()
            return Array(batchSize) { b ->
                LongArray(seqLen) { i -> this[b * seqLen + i] }
            }
        }
    }
}