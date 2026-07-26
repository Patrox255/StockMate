package com.example.stockmate.data.prediction

import javax.inject.Inject

class PredictionSettings @Inject constructor() {
    var currentModel = PredictionModelType.CONSUMPTION_SMILE_LINEAR_REGRESSION
    // TODO: Saving and loading current model setting from persistent storage & managing
}