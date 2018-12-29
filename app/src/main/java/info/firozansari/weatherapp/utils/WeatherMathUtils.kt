package info.firozansari.weatherapp.utils


object WeatherMathUtils {

    fun convertFahrenheitToCelsius(temperatureFahrenheit: Double?): Double? =
            if (temperatureFahrenheit != null)
                ((temperatureFahrenheit - 32) * 5) / 9
            else null
}