package com.example.network

import com.example.model.Gif
import com.example.model.TrendingResponse
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val API_KEY = "DJXUa3Hh73Sn4v4ucgH9yWEembvQkR9S"

sealed class GifResult {
    data class Error(val error: Throwable) : GifResult()
    data class Success(val gifs: List<Gif>) : GifResult()
}


interface GifResultReceiver {
    fun receive(result: GifResult)
}

class GiphyService {

    lateinit var service: GiphyApi
    lateinit var service2: GiphyApi
    private lateinit var disposable: Disposable
    private var compositeDisposable = CompositeDisposable()

    fun callNetwork() {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.giphy.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        service = retrofit.create(GiphyApi::class.java)
    }

    fun loadDataTrending(receiver: GifResultReceiver) {

        val observable = service.trending(API_KEY)
        disposable = observable
            .map { it.data
                .map { it.toGif() }
                .take(15) // prende i primi 15 elementi
                .subList(5, 10) // sottolista da 5 a 10
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                receiver.receive(GifResult.Success(it))
            }, {
                receiver.receive(GifResult.Error(error = it))
            })
        compositeDisposable.addAll(disposable)
    }

    // Provate 2 chiamate di rete per utilizzare i metodi di Observable di gestire 2 chiamate differenti
    // tipo merge, concat, zip,
    fun callNetwork2() {
        val interceptor: HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.giphy.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()

        service2 = retrofit.create(GiphyApi::class.java)
    }

    fun mixTwoDifferentCallNetwork(receiver: GifResultReceiver) {

        val observable = service.trending(API_KEY)
        val observable2 = service2.trending(API_KEY)
        val mergeObservable = Observable.merge(observable, observable2)

        disposable = mergeObservable
            .map { it.data
                .map { it.toGif() }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe({
                receiver.receive(GifResult.Success(it))
            }, {
                receiver.receive(GifResult.Error(error = it))
            })
        compositeDisposable.addAll(disposable)
    }

    fun disposeComposite() {
        compositeDisposable.dispose()
    }
}

interface GiphyApi {
    @GET("v1/gifs/trending")
    fun trending(@Query("api_key") api_key: String): Observable<TrendingResponse>
}
