package info.firozansari.weatherapp.di

import dagger.Binds
import dagger.Module
import info.firozansari.weatherapp.data.repository.WeatherRepository
import info.firozansari.weatherapp.data.repository.WeatherRepositoryImpl

@Module
abstract class WeatherRepositoryModule {
    @Binds
    abstract fun bindsWeatherRepository(weatherRepositoryImpl: WeatherRepositoryImpl): WeatherRepository
}