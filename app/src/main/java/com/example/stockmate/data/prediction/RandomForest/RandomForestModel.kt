package com.example.stockmate.data.prediction.RandomForest

import com.example.stockmate.data.prediction.ConsumptionModel

class RandomForestModel(
    private val trees: List<TreeNode>
): ConsumptionModel {
    override fun predict(features: Map<String, Double>): Double {
        return trees.map { it.predict(features) }.average()
    }
}

sealed class TreeNode {
    data class Leaf(val value: Double): TreeNode()
    data class Split(
        val featureName: String,
        val splitValue: Double,
        val left: TreeNode,
        val right: TreeNode
    ): TreeNode()

    fun predict(features: Map<String, Double>): Double = when (this) {
        is Leaf -> value
        is Split -> {
            val featureValue = features[featureName]
            if (featureValue == null) {
                throw IllegalArgumentException("Feature '$featureName' is missing")
            }
            if (featureValue <= splitValue) {
                left.predict(features)
            } else {
                right.predict(features)
            }
        }
    }
}

