package com.rasteroid.p2pmaps.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rasteroid.p2pmaps.vm.LogsViewModel

@Composable
fun LogsScreen(
    viewModel: LogsViewModel
) {
    Box(modifier = Modifier
        .fillMaxSize()
    ) {
        val logsState = rememberLazyListState()

        LazyColumn(Modifier.fillMaxSize().padding(end = 12.dp), logsState) {
            items(viewModel.logs) { log ->
                val date = log.time.toLocalDate()
                val time = log.time.toLocalTime().format(viewModel.timeFormat)
                Text("$date $time [${log.severity}] (${log.tag}): ${log.message}")
            }
        }
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(
                scrollState = logsState
            )
        )
    }
}