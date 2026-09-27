package com.example.data.model

data class SearchFilter(
    val query: String = "",
    val selectedStatus: String = "All", // All, Ongoing, Completed
    val selectedContentRating: String = "All", // All, Safe, Suggestive, NSFW
    val selectedGenres: Set<String> = emptySet(),
    val selectedSourceId: String = "All", // All or specific source ID
    val sortBy: String = "Popularity" // Popularity, Rating, Latest Upload, Title A-Z
)
