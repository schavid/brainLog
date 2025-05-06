package com.example.brainlog.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.brainlog.model.SearchMedium

@Composable
fun SearchResultsList(results: List<SearchMedium>, onClick: (SearchMedium) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(results) { result ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onClick(result)
                    }
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                color = Color.White,
                shape = RoundedCornerShape(8.dp),


                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (result) {
                        is SearchMedium.SearchMovie -> {
                            result.poster_path?.let { path ->
                                val imageUrl = "https://image.tmdb.org/t/p/w500$path"
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = result.original_title,
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        is SearchMedium.SearchSeries -> {
                            result.poster_path?.let { path ->
                                val imageUrl = "https://image.tmdb.org/t/p/w500$path"
                                Image(
                                    painter = rememberAsyncImagePainter(imageUrl),
                                    contentDescription = result.original_title,
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }


                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        Text(result.original_title, style = MaterialTheme.typography.titleMedium)
                        Text("Release: ${result.release_date}", style = MaterialTheme.typography.bodyMedium)
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Type: ${result.type}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 4.dp, end = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

