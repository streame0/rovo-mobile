package com.rovo.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rovo.app.ui.theme.rovo
import com.rovo.app.ui.theme.RovoTokens

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    showAccentBar: Boolean = true
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RovoTokens.Space.s4)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = RovoTokens.Type.titleMd,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.rovo.colors.textPrimary
        )
        if (showAccentBar) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.rovo.colors.primary,
                        shape = RoundedCornerShape(RovoTokens.Space.s2)
                    )
            )
        }
    }
}
