package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.translation.TranslatedBubble
import com.example.data.translation.TranslationMode
import com.example.ui.theme.KotatsuTeal

@Composable
fun ComicTranslationOverlay(
    bubbles: List<TranslatedBubble>,
    mode: TranslationMode = TranslationMode.ENGLISH_TYPESETTING,
    onBubbleClick: (TranslatedBubble) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val totalWidth = maxWidth
        val totalHeight = maxHeight

        bubbles.forEach { bubble ->
            val leftPx = totalWidth * bubble.xRatio
            val topPx = totalHeight * bubble.yRatio
            val widthPx = totalWidth * bubble.widthRatio

            Box(
                modifier = Modifier
                    .offset(x = leftPx, y = topPx)
                    .width(widthPx)
                    .shadow(4.dp, RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (mode) {
                            TranslationMode.ORIGINAL_JAPANESE -> Color(0xFFFEF3C7).copy(alpha = 0.95f) // Warm amber for OCR
                            TranslationMode.BILINGUAL -> Color(0xFFF0FDFA).copy(alpha = 0.95f) // Light teal for bilingual
                            TranslationMode.ENGLISH_TYPESETTING -> Color.White.copy(alpha = 0.95f)
                        }
                    )
                    .border(
                        1.5.dp,
                        when (mode) {
                            TranslationMode.ORIGINAL_JAPANESE -> Color(0xFFD97706)
                            TranslationMode.BILINGUAL -> KotatsuTeal
                            TranslationMode.ENGLISH_TYPESETTING -> KotatsuTeal
                        },
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onBubbleClick(bubble) }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                when (mode) {
                    TranslationMode.ENGLISH_TYPESETTING -> {
                        Text(
                            text = bubble.translatedText,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                    }
                    TranslationMode.ORIGINAL_JAPANESE -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "OCR (${(bubble.confidence * 100).toInt()}%)",
                                color = Color(0xFFB45309),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = bubble.originalText,
                                color = Color(0xFF78350F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    TranslationMode.BILINGUAL -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = bubble.translatedText,
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                lineHeight = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = bubble.originalText,
                                color = Color(0xFF6B7280),
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
