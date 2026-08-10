package com.example.stockmate.data.prediction.RandomForest

import com.example.stockmate.data.prediction.ConsumptionTrainer
import com.example.stockmate.data.prediction.DatasetConverter
import com.example.stockmate.data.prediction.SettingsRepository
import com.example.stockmate.data.prediction.math.DataFrame
import com.example.stockmate.data.prediction.math.Vector
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class RandomForestTrainer @Inject constructor(
    val appSettingsRepository: SettingsRepository
): ConsumptionTrainer{
    override suspend fun train(df: DataFrame): RandomForestModel {
        val numRows = df.rowCount
        if (numRows == 0)
            return RandomForestModel(emptyList())
        val featureCols = df.columns.keys.filter { it != DatasetConverter.CONSUMED_COLUMN }
        val targetVector = df.column(DatasetConverter.CONSUMED_COLUMN)
        val settings = appSettingsRepository.settings.first()
        val rfSettings = settings.randomForestSettings
        val numTrees = rfSettings.numTrees
        val maxDepth = rfSettings.maxDepth
        val featuresSubsetSizeDivider = rfSettings.featuresSubsetSizeDivider

        val trees = (1..numTrees).map {
            val bootstrapIndices = List(numRows) { (0 until numRows).random() }
            buildTree(
                indices = bootstrapIndices,
                depth = maxDepth,
                df = df,
                features = featureCols,
                target = targetVector,
                featuresSubsetSizeDivider = featuresSubsetSizeDivider
            )
        }
        return RandomForestModel(trees)
    }

    private fun buildTree(
        indices: List<Int>,
        depth: Int,
        df: DataFrame,
        features: List<String>,
        target: Vector,
        featuresSubsetSizeDivider: Int
    ): TreeNode {
        val yValues = indices.map {target[it]}
        val meanY = if (yValues.isNotEmpty()) yValues.average() else 0.0
        fun createLeafNode(): TreeNode.Leaf {
            return TreeNode.Leaf(meanY.takeIf { !it.isNaN() } ?: 0.0)
        }

        if (depth == 0 || indices.size < 2 || yValues.distinct().size == 1) {
            return createLeafNode()
        }

        val subsetSize = maxOf(1, features.size / featuresSubsetSizeDivider)
        val selectedFeatures = features.shuffled().take(subsetSize)
        var bestFeature = ""
        var bestSplitValue = 0.0
        var minError = Double.MAX_VALUE
        var bestLeftIndices = emptyList<Int>()
        var bestRightIndices = emptyList<Int>()

        for (feature in selectedFeatures) {
            val featureVector = df.column(feature)
            val splitCandidates = indices.map {featureVector[it]}.distinct().sorted()
            for (i in 0 until splitCandidates.size - 1) {
                // Split value is the midpoint between two consecutive unique feature values
                val splitVal = (splitCandidates[i] + splitCandidates[i + 1]) / 2
                val leftIdx = indices.filter {featureVector[it] <= splitVal}
                val rightIdx = indices.filter {featureVector[it] > splitVal}

                if (leftIdx.isNotEmpty() && rightIdx.isNotEmpty()) {
                    val leftY = leftIdx.map {target[it]}
                    val rightY = rightIdx.map {target[it]}
                    val leftMean = leftY.average()
                    val rightMean = rightY.average()

                    val mseLeft = leftY.sumOf { (it - leftMean).pow(2) }
                    val mseRight = rightY.sumOf { (it - rightMean).pow(2) }
                    val totalError = mseLeft + mseRight
                    if (totalError < minError) {
                        minError = totalError
                        bestFeature = feature
                        bestSplitValue = splitVal
                        bestLeftIndices = leftIdx
                        bestRightIndices = rightIdx
                    }
                }
            }
        }

        if (minError == Double.MAX_VALUE) {
            return createLeafNode()
        }
        return TreeNode.Split(
            featureName = bestFeature,
            splitValue = bestSplitValue,
            left = buildTree(bestLeftIndices, depth - 1, df, features, target, featuresSubsetSizeDivider),
            right = buildTree(bestRightIndices, depth - 1, df, features, target, featuresSubsetSizeDivider)
        )
    }
}