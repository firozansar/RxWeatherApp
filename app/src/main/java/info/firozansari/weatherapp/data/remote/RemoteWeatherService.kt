package info.firozansari.weatherapp.data.remote

import info.firozansari.weatherapp.data.remote.weatherModel.WeatherResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path


interface RemoteWeatherService {

    @GET("{latitude},{longitude}")
    fun requestWeatherForCity(
            @Path("latitude") latitude: String,
            @Path("longitude") longitude: String
    ): Single<WeatherResponse>
}