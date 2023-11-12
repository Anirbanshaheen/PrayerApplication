package com.example.prayerapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prayerapplication.model.PrayersTime
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrayerViewModel @Inject constructor() : ViewModel() {

    private var _prayersTime = MutableSharedFlow<ArrayList<PrayersTime>>()
    val prayersTime = _prayersTime

    fun setPrayerTimes(prayersTime: ArrayList<PrayersTime>) = viewModelScope.launch {
        _prayersTime.emit(prayersTime)
    }

}