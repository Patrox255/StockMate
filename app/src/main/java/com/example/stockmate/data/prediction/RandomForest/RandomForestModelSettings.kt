package com.example.stockmate.data.prediction.RandomForest

data class RandomForestModelSettings(
    val numTrees: Int = 10,
    val maxDepth: Int = 3,
    val minSamplesSplit: Int = 2,
    val featuresSubsetSizeDivider: Int = 3
)