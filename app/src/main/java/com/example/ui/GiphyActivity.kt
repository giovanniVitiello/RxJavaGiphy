package com.example.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.adapter.GifsAdapter
import com.example.adapter.GifsViewHolder
import com.example.exhaustive
import com.example.model.Gif
import com.example.rxjavagiphy.*
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar

class GiphyActivity : AppCompatActivity() {

    private lateinit var viewModel: GiphyViewModel

    private lateinit var baseLayout: CoordinatorLayout

    private lateinit var progress: ProgressBar

    private lateinit var gifsRV: RecyclerView

    private lateinit var gifsAdapter: androidx.recyclerview.widget.ListAdapter<Gif, GifsViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider.NewInstanceFactory().create(GiphyViewModel::class.java)
        setupViews()
    }

    private fun setupViews() {
        baseLayout = findViewById(R.id.base_layout)
        progress = findViewById(R.id.progress)

        gifsRV = findViewById(R.id.gifs_list)
        gifsRV.layoutManager = GridLayoutManager(this, 2)
        gifsAdapter = GifsAdapter()
        gifsRV.adapter = gifsAdapter
    }

    override fun onStart() {
        super.onStart()

        viewModel.state.observe(this, Observer { state ->
            when (state) {
                is GiphyState.InProgress -> showProgress()
                is GiphyState.Error -> {
                    hideProgress()
                    showError(state.error)
                }
                is GiphyState.Success -> {
                    hideProgress()
                    showGifs(state.success)
                }
            }.exhaustive
        })

        viewModel.send(GiphyEvent.Load)
    }

    private fun showProgress() {
        progress.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        progress.visibility = View.GONE
    }

    private fun showError(error: Throwable) {
        Log.d("GiphyActivity", "showError")

        Snackbar.make(baseLayout, "error: $error", LENGTH_INDEFINITE)
                .setAction("Retry") {
                    viewModel.send(GiphyEvent.Load)
                }
            .show()
    }

    private fun showGifs(gifs: List<Gif>) {
        Log.d("GiphyActivity", "showGifs: $gifs")

        gifsAdapter.submitList(gifs)
    }

}

