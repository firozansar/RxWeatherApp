package info.firozansari.weatherapp.ui

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import info.firozansari.weatherapp.R
import info.firozansari.weatherapp.ui.adapters.WeeklyWeatherAdapter
import info.firozansari.weatherapp.data.WeatherDetailsDTO
import info.firozansari.weatherapp.data.WeeklyWeatherDTO
import info.firozansari.weatherapp.databinding.ActivityWeatherDetailsBinding
import info.firozansari.weatherapp.utils.ChartFormatter
import info.firozansari.weatherapp.utils.StringFormatter.convertTimestampToDayAndHourFormat
import info.firozansari.weatherapp.utils.StringFormatter.convertToValueWithUnit
import info.firozansari.weatherapp.utils.StringFormatter.unitDegreesCelsius
import info.firozansari.weatherapp.utils.StringFormatter.unitPercentage
import info.firozansari.weatherapp.utils.StringFormatter.unitsMetresPerSecond
import info.firozansari.weatherapp.utils.WeatherMathUtils.convertFahrenheitToCelsius
import org.parceler.Parcels
import java.util.*

class WeatherDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWeatherDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val weatherDetails = Parcels.unwrap<WeatherDetailsDTO>(intent.getParcelableExtra(getString(R.string.intentWeatherDetailsParcelerBundleName)))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = weatherDetails.cityName

        setupMainWeatherDetailsInfo(weatherDetails)
        setupRecyclerView(weatherDetails)
        setupHourlyChart(binding.chartHourlyWeather, weatherDetails)

        weatherDetails.temperature?.let { setupTemperatureTextColor(it) }
    }

    private fun setupMainWeatherDetailsInfo(weatherDetails: WeatherDetailsDTO) {
        binding.textViewCurrentTime.text = convertTimestampToDayAndHourFormat(Date().time)
        binding.textViewCurrentTemperature.text = convertToValueWithUnit(2, unitDegreesCelsius, weatherDetails.temperature)
        binding.textViewWeatherSummary.text = weatherDetails.weatherSummary
        binding.textViewHumidityValue.text = convertToValueWithUnit(2, unitPercentage, weatherDetails.humidity)
        binding.textViewWindSpeedValue.text = convertToValueWithUnit(2, unitsMetresPerSecond, weatherDetails.windSpeed)
        binding.textViewCloudCoverageValue.text = convertToValueWithUnit(2, unitPercentage, weatherDetails.cloudsPercentage)
    }

    private fun setupRecyclerView(weatherDetails: WeatherDetailsDTO) {
        val weeklyWeatherList = weatherDetails.weeklyDayWeahterList as ArrayList<WeeklyWeatherDTO>
        val adapter: WeeklyWeatherAdapter? = WeeklyWeatherAdapter(weeklyWeatherList)
        val mLayoutManager = LinearLayoutManager(applicationContext)
        binding.recyclerViewWeeklyWeather.layoutManager = mLayoutManager
        binding.recyclerViewWeeklyWeather.itemAnimator = DefaultItemAnimator()
        binding.recyclerViewWeeklyWeather.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.recyclerViewWeeklyWeather.adapter = adapter
    }

    private fun setupTemperatureTextColor(temperature: Double) {
        when {
            temperature < 10 -> binding.textViewCurrentTemperature.setTextColor(Color.BLUE)
            10 >= temperature && temperature <= 20 -> binding.textViewCurrentTemperature.setTextColor(Color.BLACK)
            temperature > 20 -> binding.textViewCurrentTemperature.setTextColor(Color.RED)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupHourlyChart(lineChart: LineChart, weatherDetailsDTO: WeatherDetailsDTO) {

        val entries = ArrayList<Entry>()
        val temperatureList = ArrayList<Int>()

        for (i in 0..24) {
            convertFahrenheitToCelsius(weatherDetailsDTO.hourlyWeatherList?.get(i)?.temperature)?.let {
                temperatureList.add(it.toInt())
                entries.add(Entry(i.toFloat(), it.toFloat()))
            }
        }

        val lineDataSet = LineDataSet(entries, "Label")
        customizeLineDataSet(lineDataSet)

        val leftAxis = lineChart.axisLeft
        setYAxis(leftAxis, temperatureList)

        val rightAxis = lineChart.axisRight
        setYAxis(rightAxis, temperatureList)

        val downAxis = lineChart.xAxis
        weatherDetailsDTO.hourlyWeatherStringFormatedHoursList?.let {
            setXAxis(downAxis, weatherDetailsDTO.hourlyWeatherStringFormatedHoursList)
        }

        val lineData = LineData(lineDataSet)
        lineDataSet.valueFormatter = ChartFormatter.ValueFormatter()
        customizeLineChart(lineChart, lineData)
    }

    private fun setXAxis(xAxis: XAxis, values: ArrayList<String>) {
        xAxis.labelCount = 25
        xAxis.setDrawGridLines(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = ChartFormatter.AxisValueFormatter(values)
    }

    private fun customizeLineDataSet(lineDataSet: LineDataSet) {
        lineDataSet.valueTextSize = 12f
        lineDataSet.circleHoleRadius = 2.5f
        lineDataSet.circleRadius = 4f
        lineDataSet.valueFormatter = ChartFormatter.ValueFormatter()
        lineDataSet.color = R.color.colorAccent
        lineDataSet.valueTextColor = R.color.colorPrimary
    }

    private fun customizeLineChart(lineChart: LineChart, lineData: LineData) {
        val description = Description()
        description.text = ""
        lineChart.data = lineData
        lineChart.legend.isEnabled = false
        lineChart.setTouchEnabled(false)
        lineChart.description = description
        lineChart.canScrollHorizontally(1)
        lineChart.invalidate()
        lineChart.notifyDataSetChanged()
    }

    private fun setYAxis(axis: YAxis, temp: ArrayList<Int>) {
        axis.setDrawGridLines(false)
        axis.setDrawLabels(false)
        axis.axisMinimum = (Collections.min(temp) - 2).toFloat()
        axis.axisMaximum = (Collections.max(temp) + 2).toFloat()
    }
}
