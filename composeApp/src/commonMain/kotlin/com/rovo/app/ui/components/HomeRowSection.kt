package com.rovo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.shared.domain.HomeRow
import com.rovo.shared.model.stremio.MetaItem

@Composable
fun HomeRowSection(
    homeRow: HomeRow,
    onItemClick: (MetaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val rememberedOnItemClick = remember(onItemClick) { onItemClick }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s12)
    ) {
        Column(modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)) {
            SectionHeader(title = homeRow.title)
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
            horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s12),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(
                items = homeRow.items,
                key = { it.id },
                contentType = { "meta_item" }
            ) { item ->
                MetaItemCard(
                    item = item,
                    onClick = { rememberedOnItemClick(item) }
                )
            }
        }
    }
}
