package com.sket.glanceexample

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds
import androidx.compose.ui.platform.LocalLocale
import androidx.glance.layout.size

class StockAppWidget : GlanceAppWidget() {

    private var job: Job? = null

    companion object {
        private val smallMode = androidx.compose.ui.unit.DpSize(100.dp, 80.dp)
        private val mediumMode = androidx.compose.ui.unit.DpSize(120.dp, 120.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(smallMode, mediumMode)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        if (job == null) {
            job = startUpdateJob(context)
        }

        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    private fun startUpdateJob(context: Context): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                PriceDataRepo.update()
                StockAppWidget().updateAll(context)
                delay(20.seconds)
            }
        }
    }

    private fun refreshPrice() {
        PriceDataRepo.update()
    }

    @Composable
    fun GlanceContent() {
        val stateCount by PriceDataRepo.currentPrice.collectAsState()
        val size = LocalSize.current

        when {
            size.width <= smallMode.width -> Small(stateCount)
            else -> Medium(stateCount)
        }
    }

    @Composable
    private fun StockDisplay(stateCount: Float) {
        val color = if (PriceDataRepo.change > 0) {
            GlanceTheme.colors.primary
        } else {
            GlanceTheme.colors.error
        }

        Text(
            text = PriceDataRepo.ticker,
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = String.format(LocalLocale.current.platformLocale, "%.2f", stateCount),
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )

        Text(
            text = "${PriceDataRepo.change}%",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }

    @Composable
    private fun Small(stateCount: Float) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
                .clickable { refreshPrice() }
        ) {
            StockDisplay(stateCount)
        }
    }

    @Composable
    private fun Medium(stateCount: Float) {
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(15.dp)
                .background(GlanceTheme.colors.background)
                .padding(8.dp)
                .clickable { refreshPrice() }
        ) {
            StockDisplay(stateCount)

            val arrowRes = if (PriceDataRepo.change > 0)
                android.R.drawable.arrow_up_float
            else
                android.R.drawable.arrow_down_float

            Image(
                provider = ImageProvider(arrowRes),
                contentDescription = if (PriceDataRepo.change > 0) "Up" else "Down",
                modifier = GlanceModifier
                    .size(60.dp)
                    .padding(top = 8.dp)
            )
        }
    }
}