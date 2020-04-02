package com.example.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.exhaustive
import com.example.model.Gif
import com.example.network.GifResult
import com.example.network.GifResultReceiver
import com.example.network.GiphyService

private const val API_KEY = "DJXUa3Hh73Sn4v4ucgH9yWEembvQkR9S"

sealed class GiphyEvent {
    object Load : GiphyEvent()
}

sealed class GiphyState {
    object InProgress : GiphyState()
    data class Error(val error: Throwable) : GiphyState()
    data class Success(val success: List<Gif>) : GiphyState()
}

class GiphyViewModel : ViewModel() {

    var state = MutableLiveData<GiphyState>()
    private var giphyService = GiphyService()

    init {
        giphyService.callNetwork()
//        giphyService.callNetwork2()
    }

    fun send(event: GiphyEvent) {
        when (event) {
            GiphyEvent.Load -> loadData()
        }
    }

    private fun loadData() {
        state.value = GiphyState.InProgress

        giphyService.loadDataTrending(object :
            GifResultReceiver {
            override fun receive(result: GifResult) {
                when (result) {
                    is GifResult.Error -> state.value =
                        GiphyState.Error(result.error)
                    is GifResult.Success -> state.value =
                        GiphyState.Success(result.gifs)
                }.exhaustive
            }
        })
    }

    override fun onCleared() {
        super.onCleared()
        giphyService.disposeComposite()
    }
}
