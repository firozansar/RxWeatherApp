package info.firozansari.weatherapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = RoomConfig.TABLE_CITIES)
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var cityName: String
)