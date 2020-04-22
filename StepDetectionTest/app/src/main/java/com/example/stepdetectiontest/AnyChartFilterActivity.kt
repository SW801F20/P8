package com.example.stepdetectiontest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class AnyChartFilterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_any_chart)
        val anyChartView = findViewById<AnyChartView>(R.id.any_chart_view)
//        anyChartView.setProgressBar(findViewById(R.id.progress_bar))
        val cartesian = AnyChart.line()
        cartesian.animation(true)
        cartesian.padding(10.0, 20.0, 5.0, 20.0)
        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true) // TODO ystroke
            .yStroke(
                null as Stroke?,
                null,
                null,
                null as String?,
                null as String?
            )
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.title("AccelerometerZs")
        cartesian.yAxis(0).title("Accelerometer reading")
        cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)
        val seriesData: MutableList<DataEntry> = ArrayList()
        val accelReadings = intent.getFloatArrayExtra("ACCEL_READINGS")
        val accelTimestamps = intent.getDoubleArrayExtra("ACCEL_TIMESTAMPS")
        val peakTimestamps = intent.getDoubleArrayExtra("PEAK_TIMESTAMPS")

        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.CEILING

        for (i in accelReadings!!.indices) {
            if (peakTimestamps.contains(accelTimestamps[i]))
                seriesData.add(CustomDataEntry(String.format("%.2f",accelTimestamps!![i]), accelReadings!![i], 8))
            else
                seriesData.add(CustomDataEntry(String.format("%.2f",accelTimestamps!![i]), accelReadings!![i], 7))
        }



        val set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series1 = cartesian.line(series1Mapping)
        series1.name("Life")
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)

        val series2 = cartesian.line(series2Mapping)
        series2.name("Peaks")
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4.0)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5.0)
            .offsetY(5.0)
        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13.0)
        cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)
        anyChartView.setChart(cartesian)
    }

    private class CustomDataEntry internal constructor(
        x: String?,
        value: Number?,
        value2: Number?
    ) :
        ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
        }
    }

//    private inner class CustomDataEntry internal constructor(
//        x: String?,
//        value: Float?
//    ) :
//        ValueDataEntry(x, value) {
//        // TODO: setValue?
//    }
}