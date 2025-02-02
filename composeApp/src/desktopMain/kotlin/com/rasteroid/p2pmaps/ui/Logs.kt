package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.rasteroid.p2pmaps.vm.LogsViewModel

@Composable
fun LogsScreen(
    viewModel: LogsViewModel
) = Box {
    Text("logs")
}