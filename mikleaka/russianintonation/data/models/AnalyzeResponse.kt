package com.mikleaka.russianintonation.data.models

import com.google.gson.annotations.SerializedName

/**
 * Ответ от сервера после анализа интонации
 */
data class AnalyzeResponse(
    @SerializedName("phrase_probability") val phraseProbability: Double,
    @SerializedName("score") val score: Double,
    @SerializedName("graph_url") val graphUrl: String,
    @SerializedName("details") val details: AnalysisDetails,
    @SerializedName("expected_phrase") val expectedPhrase: String? = null
)

/**
 * Детали анализа
 */
data class AnalysisDetails(
    @SerializedName("f0_similarity") val f0Similarity: Double,
    @SerializedName("energy_similarity") val energySimilarity: Double,
    @SerializedName("contour_similarity") val contourSimilarity: Double,
    @SerializedName("best_match_score") val bestMatchScore: Double,
    @SerializedName("score_variance") val scoreVariance: Double,
    @SerializedName("ik_type") val ikType: String,
    @SerializedName("best_reference_index") val bestReferenceIndex: Int,
    @SerializedName("all_scores") val allScores: List<Double>
) 