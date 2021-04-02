package info.firozansari.weatherapp.di

import android.content.Context
import info.firozansari.weatherapp.data.repository.WeatherRepository
import info.firozansari.weatherapp.ui.WeatherViewModelFactory
import dagger.Module
import dagger.Provides
import info.firozansari.weatherapp.ui.WeatherApplication
import javax.inject.Singleton


@Module
class AppModule(private val weatherApplication: WeatherApplication) {

    @Provides
    @Singleton
    fun provideContext(): Context = weatherApplication

    @Provides
    @Singleton
    fun provideViewModelFactory(weatherRepository: WeatherRepository): WeatherViewModelFactory{
        return WeatherViewModelFactory(weatherRepository)
    }

}
