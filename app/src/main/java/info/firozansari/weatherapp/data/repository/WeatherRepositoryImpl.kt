package info.firozansari.weatherapp.data.repository

import info.firozansari.weatherapp.data.remote.RemoteWeatherDataSource
import info.firozansari.weatherapp.data.remote.locationModel.LocationResponse
import info.firozansari.weatherapp.data.remote.weatherModel.WeatherResponse
import info.firozansari.weatherapp.data.room.CityEntity
import info.firozansari.weatherapp.data.room.RoomDataSource
import info.firozansari.weatherapp.domain.dto.WeatherDetailsDTO
import info.firozansari.weatherapp.utils.TransformersDTO
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
        private val remoteWeatherDataSource: RemoteWeatherDataSource,
        private val roomDataSource: RoomDataSource
) : WeatherRepository {

    override fun getWeather(cityName: String): Single<WeatherDetailsDTO> {

        return remoteWeatherDataSource.requestCityAddressByName(cityName)
                .flatMap { locationResponse: LocationResponse ->
                    remoteWeatherDataSource.requestWeatherForCity(
                            locationResponse.results[0].geometry.location.lat.toString(),
                            locationResponse.results[0].geometry.location.lng.toString()
                    )
                            .map { weatherResponse: WeatherResponse ->
                                TransformersDTO.transformToWeatherDetailsDTO(
                                        locationResponse.results[0].formatted_address,
                                        weatherResponse
                                )
                            }
                }
                .retry(1)

    }


    override fun getCities(): Flowable<List<CityEntity>> {
        return roomDataSource.weatherSearchCityDao().getAllCities()
    }

    override fun addCity(cityName: String) {
        Completable.fromCallable { roomDataSource.weatherSearchCityDao().insertCity(CityEntity(cityName = cityName)) }.subscribeOn(Schedulers.io()).subscribe()
    }

}