package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipItem
import com.example.data.ClipType
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SuccessEmerald
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClipCard(
    clip: ClipItem,
    searchQuery: String,
    onCopy: () -> Unit,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMaskToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopiedAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(isCopiedAnimation) {
        if (isCopiedAnimation) {
            delay(1200)
            isCopiedAnimation = false
        }
    }

    val cardBorderColor by animateColorAsState(
        targetValue = when {
            isCopiedAnimation -> SuccessEmerald
            clip.isPinned -> StarAmber.copy(alpha = 0.6f)
            else -> MaterialTheme.colorScheme.outline
        },
        animationSpec = tween(300),
        label = "cardBorderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("clip_card_${clip.id}")
            .border(
                width = if (clip.isPinned || isCopiedAnimation) 1.5.dp else 1.dp,
                color = cardBorderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (clip.isPinned) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (clip.isPinned) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Type Badge, Pinned Star, and Relative Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    TypeBadge(clip.type)
                    if (clip.isPinned) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StarAmber.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = StarAmber,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "PINNED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = StarAmber
                                )
                            }
                        }
                    }
                }

                Text(
                    text = formatTimestamp(clip.createdAt),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body content with smart representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (clip.type == ClipType.CODE) MaterialTheme.colorScheme.background.copy(alpha = 0.7f)
                        else Color.Transparent
                    )
                    .clickable {
                        onCopy()
                        isCopiedAnimation = true
                    }
                    .padding(if (clip.type == ClipType.CODE) 10.dp else 2.dp)
            ) {
                if (clip.type == ClipType.COLOR_HEX) {
                    // Color Preview + Hex text
                    val parsedColor = try {
                        val hex = clip.content.trim().removePrefix("#")
                        val colorLong = when (hex.length) {
                            3 -> "FF" + hex.map { "$it$it" }.joinToString("")
                            6 -> "FF$hex"
                            8 -> hex
                            else -> "FF000000"
                        }.toLong(16)
                        Color(colorLong)
                    } catch (_: Exception) {
                        Color.Gray
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(parsedColor)
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Column {
                            HighlightedText(
                                fullText = clip.content,
                                query = searchQuery,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Color Swatch",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (clip.type == ClipType.SECRET && clip.isMasked) {
                    // Masked text representation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "••••••••••••••••••••••••••••",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(
                            onClick = onMaskToggle,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Reveal secret",
                                tint = AccentPurple,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Normal or Code or URL text with highlighting
                    HighlightedText(
                        fullText = clip.content,
                        query = searchQuery,
                        style = if (clip.type == ClipType.CODE) {
                            MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        } else {
                            MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        },
                        maxLines = 6
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer metadata & action tools
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info: Chars count and Copy stats
                Text(
                    text = "${clip.characterCount} chars" + if (clip.copyCount > 0) " · copied ${clip.copyCount}x" else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Actions: Link open (if URL), Mask toggle, Pin, Edit, Share, Copy, Delete
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (clip.type == ClipType.URL) {
                        IconButton(
                            onClick = {
                                try {
                                    val uri = Uri.parse(
                                        if (!clip.content.startsWith("http://") && !clip.content.startsWith("https://")) {
                                            "https://${clip.content}"
                                        } else {
                                            clip.content
                                        }
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("open_link_button_${clip.id}")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open Link",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    if (clip.type == ClipType.SECRET && !clip.isMasked) {
                        IconButton(
                            onClick = onMaskToggle,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Mask secret",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Pin toggle
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("pin_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = if (clip.isPinned) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (clip.isPinned) "Unpin" else "Pin",
                            tint = if (clip.isPinned) StarAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Edit
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("edit_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Clip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, clip.content)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Share Clip via")
                            context.startActivity(shareIntent)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("share_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Copy
                    IconButton(
                        onClick = {
                            onCopy()
                            isCopiedAnimation = true
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("copy_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = if (isCopiedAnimation) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = if (isCopiedAnimation) SuccessEmerald else PrimaryIndigo,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    // Delete
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("delete_button_${clip.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TypeBadge(type: ClipType) {
    val (label, icon, color) = when (type) {
        ClipType.URL -> Triple("LINK", Icons.Default.Link, PrimaryIndigo)
        ClipType.CODE -> Triple("CODE", Icons.Default.Code, AccentCyan)
        ClipType.COLOR_HEX -> Triple("COLOR", Icons.Default.Palette, AccentPurple)
        ClipType.SECRET -> Triple("SECRET", Icons.Default.Key, StarAmber)
        ClipType.TEXT -> Triple("TEXT", Icons.Default.ContentCopy, Color(0xFF94A3B8))
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
    }
}

@Composable
fun HighlightedText(
    fullText: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    maxLines: Int = Int.MAX_VALUE
) {
    val annotatedString = remember(fullText, query) {
        buildAnnotatedHighlight(fullText, query)
    }

    Text(
        text = annotatedString,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis
    )
}

fun buildAnnotatedHighlight(text: String, query: String): AnnotatedString {
    if (query.isBlank()) {
        return AnnotatedString(text)
    }

    return buildAnnotatedString {
        var startIndex = 0
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()

        while (startIndex < text.length) {
            val foundIndex = lowerText.indexOf(lowerQuery, startIndex)
            if (foundIndex == -1) {
                append(text.substring(startIndex))
                break
            }

            // Append text before match
            if (foundIndex > startIndex) {
                append(text.substring(startIndex, foundIndex))
            }

            // Highlight match
            val endIndex = foundIndex + query.length
            pushStyle(
                SpanStyle(
                    background = Color(0x66F59E0B),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            )
            append(text.substring(foundIndex, endIndex))
            pop()

            startIndex = endIndex
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60 * 1000 -> "Just now"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}m ago"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}h ago"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}d ago"
        else -> {
            val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
