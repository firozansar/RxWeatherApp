package com.konradszewczuk.weatherapp

import org.mockito.ArgumentCaptor


fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()