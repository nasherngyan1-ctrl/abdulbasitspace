package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // "IMAGE", "VIDEO", "APP", "WEB", "PDF", "RESEARCH"
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val payloadJson: String = "",
    val thumbnailUri: String? = null,
    val status: String = "تەواوکراو" // Done / Draft
)
