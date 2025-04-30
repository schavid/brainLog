package com.example.brainlog.view


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.example.brainlog.viewmodel.SearchViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class)
@Composable
fun SearchLogic(viewModel: SearchViewModel, content: @Composable (isSearchExpanded: Boolean, searchText: String, onSearchExpandedChange: (Boolean) -> Unit, onSearchTextChange: (String) -> Unit) -> Unit) {
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        snapshotFlow { searchText }
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.length >= 2) {
                    viewModel.search(query)
                } else if (query.isEmpty()) {
                    viewModel.reset()
                }
            }
    }

    content(isSearchExpanded, searchText, { isSearchExpanded = it }, { searchText = it })
}