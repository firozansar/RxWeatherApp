package info.firozansari.weatherapp

import info.firozansari.weatherapp.data.remote.RemoteGeocodingService
import info.firozansari.weatherapp.data.remote.RemoteWeatherDataSource
import info.firozansari.weatherapp.data.remote.RemoteWeatherService
import info.firozansari.weatherapp.data.remote.locationModel.*
import info.firozansari.weatherapp.data.remote.locationModel.bounds.Bounds
import info.firozansari.weatherapp.data.remote.locationModel.bounds.Northeast
import info.firozansari.weatherapp.data.remote.locationModel.bounds.Southwest
import info.firozansari.weatherapp.data.remote.locationModel.viewport.Viewport
import info.firozansari.weatherapp.data.remote.weatherModel.Alert
import info.firozansari.weatherapp.data.remote.weatherModel.Currently
import info.firozansari.weatherapp.data.remote.weatherModel.Flags
import info.firozansari.weatherapp.data.remote.weatherModel.WeatherResponse
import info.firozansari.weatherapp.data.remote.weatherModel.daily.Daily
import info.firozansari.weatherapp.data.remote.weatherModel.hourly.Hourly
import info.firozansari.weatherapp.data.remote.weatherModel.minutely.Data
import info.firozansari.weatherapp.data.remote.weatherModel.minutely.Minutely
import info.firozansari.weatherapp.data.repository.WeatherRepository
import info.firozansari.weatherapp.data.repository.WeatherRepositoryImpl
import info.firozansari.weatherapp.data.room.CityEntity
import info.firozansari.weatherapp.data.room.RoomDataSource
import info.firozansari.weatherapp.data.room.WeatherCitiesDao
import info.firozansari.weatherapp.data.WeatherDetailsDTO
import info.firozansari.weatherapp.utils.TransformersDTO.transformToWeatherDetailsDTO
import io.reactivex.Flowable
import io.reactivex.Single
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WeatherRepositoryTest {

    @Mock
    private lateinit var roomDataSource: RoomDataSource

    @Mock
    private lateinit var remoteWeatherService: RemoteWeatherService

    @Mock
    private lateinit var weatherSearchCityDao: WeatherCitiesDao

    @Mock
    private lateinit var remoteGeocodingService: RemoteGeocodingService

    @Captor
    private lateinit var stringLatitudeArgumentCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var stringLongitudeArgumentCaptor: ArgumentCaptor<String>

    @Captor
    private lateinit var cityEntityArgumentCaptor: ArgumentCaptor<CityEntity>

    private lateinit var remoteWeatherDataSource: RemoteWeatherDataSource

    private lateinit var weatherRepository: WeatherRepository


    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        remoteWeatherDataSource = RemoteWeatherDataSource(remoteWeatherService, remoteGeocodingService)
        weatherRepository = WeatherRepositoryImpl(remoteWeatherDataSource, roomDataSource)
        Mockito.`when`(roomDataSource.weatherSearchCityDao()).thenReturn(weatherSearchCityDao)
    }


    @Test
    fun testNoCitiesReturned_whenNoCitiesSaved() {
        // Given that the RoomDataSource returns an empty list of cities
        Mockito.`when`(roomDataSource.weatherSearchCityDao().getAllCities()).thenReturn(Flowable.empty<List<CityEntity>>())

        //When getting the active shopping lists
        weatherRepository.getCities()
                .test()
                .assertNoValues()
    }

    @Test
    fun testCitiesReturned_whenCitiesSaved() {
        Mockito.`when`(roomDataSource.weatherSearchCityDao().getAllCities()).thenReturn(Flowable.just(listOf(CityEntity(cityName = "Cracow"), CityEntity(cityName = "Tokyo"))))


        weatherRepository.getCities()
                .test()
                .assertValue { citiesList: List<CityEntity> ->
                    citiesList.size == 2
                            && citiesList[0].cityName.equals("Cracow")
                            && citiesList[1].cityName.equals("Tokyo")
                }
    }

    @Test
    fun testWeatherDetailsDTOReturned_whenRequested() {
        val locationResponse = LocationResponse(listOf(resultMock), "OK")
        val weatherResponse = WeatherResponse(locationResponse.results[0].geometry.location.lat, locationResponse.results[0].geometry.location.lng, "America/NewYork", currentlyMock, minutelyMock, hourlyMock, dailyMock, listOf(alertMock), flagsMock, 2323)
        val searchedCity = "Cracow"

        Mockito.`when`(remoteGeocodingService.requestCityAddressByName(searchedCity)).thenReturn(Single.just(locationResponse))
        Mockito.`when`(remoteWeatherService.requestWeatherForCity(weatherResponse.latitude.toString(), weatherResponse.longitude.toString())).thenReturn(Single.just(weatherResponse))

        weatherRepository.getWeather(searchedCity).test()
                .assertNoErrors()
                .assertValue { weatherDeatailsDTO: WeatherDetailsDTO ->
                    weatherDeatailsDTO == transformToWeatherDetailsDTO(locationResponse.results[0].formatted_address, weatherResponse)
                }

        Mockito.verify<RemoteWeatherService>(remoteWeatherService).requestWeatherForCity(capture(stringLatitudeArgumentCaptor), capture(stringLongitudeArgumentCaptor))

        assertEquals(weatherResponse.latitude.toString(), stringLatitudeArgumentCaptor.value)
        assertEquals(weatherResponse.longitude.toString(), stringLongitudeArgumentCaptor.value)
    }

    @Test
    fun testAddCity_insertsCityEntityToDatabase(){
        val insertedCityName = "Cracow"

        weatherRepository.addCity(insertedCityName)

        //Room is currently not supporting Completable, so there is no other way to make addCity become Completable and use awaitTerminalEvent
        Thread.sleep(1000)

        Mockito.verify<WeatherCitiesDao>(roomDataSource.weatherSearchCityDao()).insertCity(capture(cityEntityArgumentCaptor))

        assertEquals(insertedCityName, cityEntityArgumentCaptor.value.cityName)
    }

    //MOCKS FOR REMOTE API RETURNED OBJECTS
    companion object {
        val currentlyMock = Currently(
                1515707975,
                "Mostly Cloudy ",
                "partly-cloudy-night",
                181,
                315,
                0.0,
                0.0,
                50.65,
                50.65,
                44.9,
                0.81,
                1026.14,
                6.12,
                11.09,
                211,
                0.63,
                0,
                7.52,
                273.79
        )
        val minutelyMock = Minutely(
                "Mostly cloudy for the hour",
                "partly-cloudy-night",
                listOf(Data(2, 3.3, 3.4))
        )

        val hourlyMock = Hourly(
                "Mostly cloudy for the hour",
                "partly-cloudy-night",
                listOf(info.firozansari.weatherapp.data.remote.weatherModel.hourly.Data(
                        1515704400,
                        "Partly Cloudy  ",
                        "partly-cloudy-day   ",
                        2.3, 3.34, 3.34, 2.23, 32.23, 23.23, 23.23, 232.23, 23.23, 23, 23.23, 232, 232.23, 23.23)
                )
        )

        val dailyMock = Daily(
                "3434", "34",
                listOf(info.firozansari.weatherapp.data.remote.weatherModel.daily.Data(23, "232", "232", 34, 34, 34.34, 343.34, 343.3, 34, 343.3, "34", 343.3, 34, 343.3, 34, 343.3, 3434, 343.3, 343, 343.3, 343.3, 343.3, 343.3, 343.3, 343, 343, 343.3, 343, 3434, 343.3, 343.3, 343.3, 3433, 343.3, 343, 343.3, 3434, 343.3, 3434))
        )

        val alertMock = Alert(
                "test", listOf("asd"), "sds", 3434, 3434, "asd", "343"
        )

        val flagsMock = Flags(
                listOf("232"), listOf("343"), "3434", 123234L
        )

        val addressMock = AddressComponent(
                "Warsaw, Poland", "Warsaw", listOf("24234")
        )

        val northEastBoundsMock = Northeast(
                34.34, 334.34
        )

        val southEastBoundMock = Southwest(
                343.34, 34.34
        )

        val nortEastViewportMock = info.firozansari.weatherapp.data.remote.locationModel.viewport.Northeast(
                34.34, 334.34
        )


        val southWestViewportMock = info.firozansari.weatherapp.data.remote.locationModel.viewport.Southwest(
                34.34, 334.34
        )

        val boundsMock = Bounds(
                northEastBoundsMock, southEastBoundMock
        )

        val viewPortMock = Viewport(
                nortEastViewportMock, southWestViewportMock
        )

        val geometryMock = Geometry(
                boundsMock, Location(23.23, 232.23), "asda", viewPortMock
        )

        val resultMock = Result(
                listOf(addressMock), "Address", geometryMock, "34", listOf("34")
        )

    }
}