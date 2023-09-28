package com.padi.warehouse.models

import kotlinx.serialization.Serializable

@Serializable
data class FileMetadata(
    val version: Int,
    val artifactType: ArtifactType,
    val applicationId: String,
    val variantName: String,
    val elements: List<Elements>,
    val elementType: String
)

@Serializable
data class ArtifactType(
    val type: String,
    val kind: String,
)

@Serializable
data class Elements(
    val type: String,
    val filters: List<String>,
    val attributes: List<String>,
    val versionCode: Int,
    val versionName: String,
    val outputFile: String
)
