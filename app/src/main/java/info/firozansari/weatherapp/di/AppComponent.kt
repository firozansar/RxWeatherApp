package info.firozansari.weatherapp.di

import info.firozansari.weatherapp.ui.WeatherCitySearchActivity
import dagger.Component
import javax.inject.Singleton


@Component(modules = arrayOf(AppModule::class, RemoteModule::class, RoomModule::class, WeatherRepositoryModule::class))
@Singleton
interface AppComponent {

    fun inject(weatherCitySearchActivity: WeatherCitySearchActivity)
}