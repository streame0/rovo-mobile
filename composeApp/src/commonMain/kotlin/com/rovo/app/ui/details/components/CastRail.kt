package com.rovo.app.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.rovo.app.ui.components.SectionHeader
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.app.ui.theme.rovo
import com.rovo.shared.model.stremio.MetaCast

@Composable
fun CastRail(
    cast: List<MetaCast>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(
            title = "Cast",
            modifier = Modifier.padding(horizontal = RovoTokens.Space.s20)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = RovoTokens.Space.s20),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(cast) { person ->
                CastItem(person = person)
            }
        }
    }
}

@Composable
private fun CastItem(
    person: MetaCast,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(86.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (person.profilePath != null) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                AsyncImage(
                    model = person.profilePath,
                    contentDescription = person.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.rovo.colors.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = person.name.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.rovo.colors.textMuted,
                    fontWeight = FontWeight.Black
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = person.name,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.rovo.colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            if (person.character != null) {
                Text(
                    text = person.character ?: "",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.rovo.colors.textMuted,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
