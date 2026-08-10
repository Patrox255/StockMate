package com.example.stockmate.data.prediction

class GenericConsumptionPredictor(
    private val trainer: ConsumptionTrainer,
    private val datasetConverter: DatasetConverter
): ConsumptionPredictor {
    private var model: ConsumptionModel? = null

    override suspend fun train(samples: List<ConsumptionTrainingSample>) {
        val df = datasetConverter.convert(samples)
        model = trainer.train(df)
    }

    override fun predict(features: ConsumptionFeatures): Double {
        return model?.predict(datasetConverter.extractFeaturesMap(features)) ?: 0.0
    }
}