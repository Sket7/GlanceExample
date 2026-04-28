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
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.sket.glanceexample.PriceDataRepo.change
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class StockAppWidget : GlanceAppWidget() {

    private var job: Job? = null

    companion object {
        private val smallMode = androidx.compose.ui.unit.DpSize(110.dp, 90.dp)
        private val mediumMode = androidx.compose.ui.unit.DpSize(130.dp, 130.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(smallMode, mediumMode))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        if (job == null) {
            job = startPeriodicUpdate(context)
        }

        provideContent {
            GlanceTheme {
                GlanceContent()
            }
        }
    }

    private fun startPeriodicUpdate(context: Context): Job {
        return CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                PriceDataRepo.update()
                updateAll(context)
                delay(20.seconds)
            }
        }
    }

    private fun refreshPrice(context: Context) {
        PriceDataRepo.update()
        CoroutineScope(Dispatchers.Main).launch {
            updateAll(context)
        }
    }

    @Composable
    fun GlanceContent() {
        val price by PriceDataRepo.currentPrice.collectAsState(initial = 25.5f)
        val size = LocalSize.current

        if (size.width <= smallMode.width) {
            Small(price)
        } else {
            Medium(price)
        }
    }

    @Composable
    private fun StockDisplay(price: Float) {
        val color = if (PriceDataRepo.change > 0)
            GlanceTheme.colors.primary
        else
            GlanceTheme.colors.error

        Column {
            Text(
                text = PriceDataRepo.ticker,
                style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
            )

            Text(
                text = "%.2f".format(price),
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            )

            Text(
                text = "${PriceDataRepo.change}%",
                style = TextStyle(fontSize = 19.sp, fontWeight = FontWeight.Bold, color = color)
            )
        }
    }

    @Composable
    private fun Small(price: Float) {
        val context = LocalContext.current
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
                .clickable { refreshPrice(context) }
        ) {
            StockDisplay(price)
        }
    }

    @Composable
    private fun Medium(price: Float) {
        val context = LocalContext.current
        Column(
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(16.dp)
                .background(GlanceTheme.colors.background)
                .padding(12.dp)
                .clickable { refreshPrice(context) }
        ) {
            StockDisplay(price)

            val arrowColor = if (change > 0) GlanceTheme.colors.primary
            else GlanceTheme.colors.error
            val arrowSymbol = if (change > 0) "▲" else "▼"

            Box(
                modifier = GlanceModifier
                    .size(44.dp)
                    .cornerRadius(22.dp)
                    .background(arrowColor.getColor(context).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = arrowSymbol,
                    modifier = GlanceModifier.padding(bottom = 3.dp),
                    style = TextStyle(
                        fontSize = 26.sp,
                        color = arrowColor,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}