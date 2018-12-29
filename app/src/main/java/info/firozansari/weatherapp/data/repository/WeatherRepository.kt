package info.firozansari.weatherapp.data.repository

import info.firozansari.weatherapp.data.room.CityEntity
import info.firozansari.weatherapp.domain.dto.WeatherDetailsDTO
import io.reactivex.Flowable
import io.reactivex.Single


interface WeatherRepository {

    fun getCities(): Flowable<List<CityEntity>>

    fun getWeather(cityName: String): Single<WeatherDetailsDTO>

    fun addCity(cityName: String)
}