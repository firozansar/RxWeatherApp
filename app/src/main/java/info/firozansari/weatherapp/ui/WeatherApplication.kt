package info.firozansari.weatherapp.ui

import android.app.Application
import info.firozansari.weatherapp.di.AppComponent
import info.firozansari.weatherapp.di.AppModule
import info.firozansari.weatherapp.di.DaggerAppComponent
import info.firozansari.weatherapp.di.RemoteModule
import info.firozansari.weatherapp.di.RoomModule

class WeatherApplication : Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        initializeDagger()
    }

    fun initializeDagger() {

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .roomModule(RoomModule())
            .remoteModule(RemoteModule()).build()
    }
}