package info.firozansari.weatherapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(CityEntity::class), version = 1)
abstract class RoomDataSource : RoomDatabase() {

    abstract fun weatherSearchCityDao(): WeatherCitiesDao

    companion object {

        @Volatile
        private var INSTANCE: RoomDataSource? = null

        fun getInstance(context: Context): RoomDataSource =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                RoomDataSource::class.java, RoomConfig.DATABASE_WEATHER
            )
                .build()
    }
}