package com.minhtu.firesocialmedia.home.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rickclephas.kmp.observableviewmodel.ViewModel

class SearchViewModel : ViewModel() {
    var query by mutableStateOf("")
    fun updateQuery(input: String) {
        query = input
    }
}