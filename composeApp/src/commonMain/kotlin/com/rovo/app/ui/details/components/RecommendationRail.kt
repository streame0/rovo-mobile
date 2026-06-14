package com.rovo.app.ui.details.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rovo.app.ui.components.SectionHeader
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.model.stremio.MetaItem

@Composable
fun RecommendationRail(
    recommendations: List<MetaItem>,
    onItemClick: (MetaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SectionHeader(
            title = "Recommendations",
            modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(recommendations) { item ->
                Card(
                    modifier = Modifier
                        .width(140.dp)
                        .aspectRatio(2/3f),
                    shape = MaterialTheme.rovo.shapes.poster,
                    onClick = { onItemClick(item) },
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.rovo.colors.textMuted.copy(alpha = 0.1f)
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    AsyncImage(
                        model = item.poster,
                        contentDescription = item.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
