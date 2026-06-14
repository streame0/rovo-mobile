package com.rovo.app.ui.details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.rovo.app.ui.theme.RovoGradients
import com.rovo.app.ui.theme.rovo
import com.rovo.app.ui.theme.RovoTokens
import com.rovo.shared.model.stremio.MetaItem

@Composable
fun DetailHero(
    meta: MetaItem,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth().height(540.dp)) {
        AsyncImage(
            model = meta.background ?: meta.poster,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RovoGradients.HeroOverlay)
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = RovoTokens.Space.s32)
                .padding(horizontal = RovoTokens.Space.s24),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (meta.logo != null) {
                AsyncImage(
                    model = meta.logo,
                    contentDescription = meta.name,
                    modifier = Modifier
                        .height(110.dp)
                        .fillMaxWidth(0.75f),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(
                    text = meta.name.uppercase(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(RovoTokens.Space.s16))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(RovoTokens.Space.s8)
            ) {
                Text(
                    text = meta.type.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.rovo.colors.textSecondary
                )
                Text("•", color = MaterialTheme.rovo.colors.textMuted)
                Text(
                    text = meta.releaseInfo ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.rovo.colors.textSecondary
                )
                if (meta.imdbRating != null) {
                    Text("•", color = MaterialTheme.rovo.colors.textMuted)
                    Text(
                        text = "⭐ ${meta.imdbRating}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.rovo.colors.textSecondary
                    )
                }
            }
        }
    }
}
