package info.firozansari.weatherapp.di

import info.firozansari.weatherapp.data.repository.WeatherRepository
import info.firozansari.weatherapp.data.repository.WeatherRepositoryImpl
import dagger.Module
import dagger.Binds


@Module
abstract class WeatherRepositoryModule {
    @Binds
    abstract fun bindsWeatherRepository(weatherRepositoryImpl: WeatherRepositoryImpl): WeatherRepository
}