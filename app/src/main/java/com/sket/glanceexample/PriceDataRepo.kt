package com.sket.glanceexample

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.random.Random

object PriceDataRepo {
    var ticker = "GOOGL"
    private var previousPrice = 25.0f
    var change = 0

    private val _currentPrice = MutableStateFlow(25.5f)
    val currentPrice: StateFlow<Float> get() = _currentPrice

    fun update() {
        previousPrice = _currentPrice.value
        _currentPrice.value = Random.nextInt(20, 35) + Random.nextFloat()

        if (previousPrice > 0f) {
            change = (((_currentPrice.value - previousPrice) / previousPrice) * 100).toInt()
        } else {
            change = 0
        }
    }
}