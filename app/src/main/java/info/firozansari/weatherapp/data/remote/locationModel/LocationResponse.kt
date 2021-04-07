package info.firozansari.weatherapp.data.remote.locationModel

import info.firozansari.weatherapp.data.remote.locationModel.bounds.Bounds
import info.firozansari.weatherapp.data.remote.locationModel.viewport.Viewport

data class LocationResponse(
        val results: List<Result>,
        val status: String //OK
)

data class Result(
        val address_components: List<AddressComponent>,
        val formatted_address: String,
        val geometry: Geometry,
        val place_id: String,
        val types: List<String>
)

data class Geometry(
        val bounds: Bounds,
        val location: Location,
        val location_type: String, //APPROXIMATE
        val viewport: Viewport
)

data class Location(
        val lat: Double, //51.5073509
        val lng: Double //-0.1277583
)

data class AddressComponent(
        val long_name: String, //London
        val short_name: String, //London
        val types: List<String>
)