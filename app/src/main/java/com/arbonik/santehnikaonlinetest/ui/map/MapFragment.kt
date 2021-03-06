package com.arbonik.santehnikaonlinetest.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.fragment.navArgs
import com.arbonik.santehnikaonlinetest.R
import com.arbonik.santehnikaonlinetest.data.GeoData
import com.arbonik.santehnikaonlinetest.databinding.FragmentMainBinding
import com.arbonik.santehnikaonlinetest.utils.Resource
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import kotlinx.coroutines.flow.collectLatest


class MapFragment : Fragment() {

    private val viewModel: MapViewModel by viewModels()
    private lateinit var binding: FragmentMainBinding
    private lateinit var mapView: MapView
    private val args: MapFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater)
        mapView = binding.mapview
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val point = Point(args.lat.toDouble(), args.lon.toDouble())
        mapView.map.move(
            CameraPosition(
                point, 11f, 0f, 0f
            ),
            Animation(Animation.Type.SMOOTH, 0f),
            null
        )

        mapView.map.addInputListener(viewModel.mapListener)

        lifecycle.coroutineScope.launchWhenStarted {
            viewModel.tapRequestState.collectLatest {
                when (it) {
                    is Resource.Error -> {
                    }
                    is Resource.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        it.data?.let { shortData ->
                            binding.progressBar.visibility = View.GONE
                            mapView.map.mapObjects.clear()
                            mapView.map.mapObjects.addPlacemark(
                                Point(shortData.lat, shortData.lon)
                            )
                            showBottomSheet(shortData)
                            viewModel.clearSearch()
                        }
                    }
                }
            }
        }
    }

    private fun showBottomSheet(geoData: GeoData) {
        val modalBottomSheet = ModalBottomSheet.newInstance(geoData)
        parentFragmentManager.findFragmentByTag(ModalBottomSheet.TAG)?.let { fragment ->
            (fragment as ModalBottomSheet).dismiss()
        }
        modalBottomSheet.show(parentFragmentManager, ModalBottomSheet.TAG)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }
}