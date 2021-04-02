package info.firozansari.weatherapp.ui

import androidx.lifecycle.ViewModelProviders
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View


import android.widget.ArrayAdapter
import android.widget.Toast
import com.jakewharton.rxbinding2.widget.RxTextView
import info.firozansari.weatherapp.data.room.CityEntity

import info.firozansari.weatherapp.utils.InputValidator.isValidCityInput
import io.reactivex.Observable
import org.parceler.Parcels
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import info.firozansari.weatherapp.R
import info.firozansari.weatherapp.data.WeatherDetailsDTO
import info.firozansari.weatherapp.databinding.ActivityWeatherCitySearchBinding
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class WeatherCitySearchActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: WeatherViewModelFactory
    private lateinit var viewModel: WeatherViewModel
    private lateinit var binding: ActivityWeatherCitySearchBinding
    private var isConnectedToInternet: Boolean = false
    private var searchedCityNames = ArrayList<String>()
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        WeatherApplication.appComponent.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityWeatherCitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WeatherViewModel::class.java)

        val itemInputNameObservable = RxTextView.textChanges(binding.autocompleteTextView)
                .map { inputText: CharSequence -> inputText.isEmpty() || !isValidCityInput(inputText.toString()) }
                .distinctUntilChanged()

        compositeDisposable.add(setupTextInputObserver(itemInputNameObservable))

        setupSearchedCityClickedListener()
    }

    private fun setupSearchedCityClickedListener() {
        binding.cityButton.setOnClickListener {
            if (!isConnectedToInternet) {
                Toast.makeText(this, getString(R.string.user_has_not_internet_connection_message), Toast.LENGTH_SHORT).show()
            }
            else{
                processRequestStartUI()
                val searchedCityName = binding.autocompleteTextView.text.toString()
                setupWeatherDetailsObserver(searchedCityName)?.let { it -> compositeDisposable.add(it) }
            }
        }
    }

    private fun setupTextInputObserver(itemInputNameObservable: Observable<Boolean>): Disposable {
        return itemInputNameObservable.subscribe { inputIsEmpty: Boolean ->
            binding.cityTextInputLayout.error = getString(R.string.invalid_input_message)
            binding.cityTextInputLayout.isErrorEnabled = inputIsEmpty
            binding.cityButton.isEnabled = !inputIsEmpty
        }
    }

    private fun setupWeatherDetailsObserver(searchedCityName: String): Disposable? {
        return viewModel.getWeather(searchedCityName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { weatherResponse: WeatherDetailsDTO? ->
                            resolveRequestEndUI()
                            navigateToDetailsActivity(weatherResponse)

                            if (!(searchedCityNames.contains(searchedCityName)))
                                viewModel.addCity(searchedCityName)
                        },
                        { throwable: Throwable? ->
                            resolveRequestEndUI()
                            if (!isConnectedToInternet) {
                                Toast.makeText(this, getString(R.string.user_has_not_internet_connection_message), Toast.LENGTH_SHORT).show()
                            } else {
                                throwable?.printStackTrace()
                                Toast.makeText(this, getString(R.string.error_with_fetching_weather_details_message), Toast.LENGTH_SHORT).show()
                            }
                        }
                )
    }

    private fun processRequestStartUI() {
        binding.inputLinearLayout.isEnabled = false
        binding.inputLinearLayout.alpha = 0.5f
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun resolveRequestEndUI() {
        binding.inputLinearLayout.isEnabled = true
        binding.inputLinearLayout.alpha = 1f
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun navigateToDetailsActivity(weatherResponse: WeatherDetailsDTO?) {
        val intent = Intent(this, WeatherDetailsActivity::class.java)
        intent.putExtra(getString(R.string.intentWeatherDetailsParcelerBundleName), Parcels.wrap(weatherResponse))
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(setupSearchedCitiesObserver())
        compositeDisposable.add(setupInternetConnectionObserver())

    }

    private fun setupSearchedCitiesObserver(): Disposable {
        return viewModel.getCities()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { citiesList: List<CityEntity> ->
                    searchedCityNames.clear()
                    citiesList.forEach { searchedCityNames.add(it.cityName) }

                    val adapter = ArrayAdapter(this,
                            android.R.layout.simple_dropdown_item_1line, searchedCityNames)

                    binding.autocompleteTextView.setAdapter(adapter)
                    binding.autocompleteTextView.threshold = 0
                }
    }

    private fun setupInternetConnectionObserver(): Disposable {
        return ReactiveNetwork.observeInternetConnectivity()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { isConnected: Boolean? ->
                            isConnected?.let {
                                    if (!isConnected)
                                        Toast.makeText(this, getString(R.string.user_has_lost_internet_connection_message), Toast.LENGTH_SHORT).show()
                                isConnectedToInternet = isConnected
                            }
                        },
                        { t: Throwable? ->
                            Log.v("ReactiveNetwork", "error")
                        }
                )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
    }
}
