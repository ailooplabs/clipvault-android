package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.StarAmber
import com.example.ui.theme.SuccessEmerald

data class TourSlide(
    val step: Int,
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color
)

@Composable
fun FeatureTourDialog(
    onDismiss: () -> Unit
) {
    var currentSlideIndex by remember { mutableIntStateOf(0) }

    val slides = listOf(
        TourSlide(
            step = 1,
            title = "Instant Search",
            description = "Find any clip in milliseconds. Full-text search with real-time highlighting across your entire history.",
            icon = Icons.Default.Search,
            iconColor = PrimaryIndigo
        ),
        TourSlide(
            step = 2,
            title = "Pin Important Clips",
            description = "Star any clip to pin it. Pinned clips stay at the top forever and safely survive 'Clear All'.",
            icon = Icons.Default.Star,
            iconColor = StarAmber
        ),
        TourSlide(
            step = 3,
            title = "Edit Before You Paste",
            description = "Tap the pencil icon to tweak any clip on the fly — fix typos, trim whitespace, or customize a snippet.",
            icon = Icons.Default.Edit,
            iconColor = AccentPurple
        ),
        TourSlide(
            step = 4,
            title = "100% Local. Zero Tracking.",
            description = "Every clip lives in your device's local database. No servers. No cloud sync. Zero telemetry. Your data never leaves your machine.",
            icon = Icons.Default.Lock,
            iconColor = SuccessEmerald
        ),
        TourSlide(
            step = 5,
            title = "Always One Tap Away",
            description = "ClipVault provides instant clipboard capture, one-tap copy, share, and categorized filters for code, links, and secrets.",
            icon = Icons.Default.FlashOn,
            iconColor = AccentCyan
        )
    )

    val currentSlide = slides[currentSlideIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("feature_tour_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with Step Pill & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(currentSlide.iconColor.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Feature ${currentSlide.step} of 5",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentSlide.iconColor
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("close_tour_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedContent(
                    targetState = currentSlide,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "SlideAnimation"
                ) { slide ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(slide.iconColor.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = slide.iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Text(
                                text = slide.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = slide.description,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Visual Interactive Mockup inside slide
                        SlideVisualMockup(slide.step)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Progress dots & Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dots
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        slides.indices.forEach { index ->
                            Box(
                                modifier = Modifier
                                    .size(if (index == currentSlideIndex) 20.dp else 8.dp, 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (index == currentSlideIndex) PrimaryIndigo
                                        else MaterialTheme.colorScheme.outline
                                    )
                            )
                        }
                    }

                    // Prev / Next buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (currentSlideIndex > 0) {
                            OutlinedButton(
                                onClick = { currentSlideIndex-- },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("tour_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Previous",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        if (currentSlideIndex < slides.size - 1) {
                            Button(
                                onClick = { currentSlideIndex++ },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                modifier = Modifier.testTag("tour_next_button")
                            ) {
                                Text("Next")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Next",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else {
                            Button(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessEmerald),
                                modifier = Modifier.testTag("tour_finish_button")
                            ) {
                                Text("Get Started", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SlideVisualMockup(step: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (step) {
                1 -> {
                    // Search simulation demo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = PrimaryIndigo
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("API_KEY", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("|", fontSize = 12.sp, color = PrimaryIndigo, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = "const API_KEY = \"sk-proj-abc...\"",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = StarAmber
                        )
                    }
                }
                2 -> {
                    // Pinned simulation demo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(StarAmber.copy(alpha = 0.15f))
                            .border(1.dp, StarAmber.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = StarAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ssh -i ~/.ssh/id_ed25519 deploy@prod", fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("★ pinned · stays on top & survives Clear All", fontSize = 10.sp, color = StarAmber)
                }
                3 -> {
                    // Edit simulation demo
                    Column {
                        Text("Edit Clip Preview:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Text("Hello, my name is Jordan.|", fontSize = 12.sp, color = Color.White)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(PrimaryIndigo).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("Save & Copy", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                4 -> {
                    // Privacy checklist
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        PrivacyRowItem(icon = Icons.Default.Check, text = "Stored on your device only (Room DB)", isPositive = true)
                        PrivacyRowItem(icon = Icons.Default.Check, text = "Zero external network requests", isPositive = true)
                        PrivacyRowItem(icon = Icons.Default.Check, text = "No account or email required", isPositive = true)
                        PrivacyRowItem(icon = Icons.Default.Close, text = "No cloud backup (by design)", isPositive = false)
                        PrivacyRowItem(icon = Icons.Default.Close, text = "No telemetry or analytics", isPositive = false)
                    }
                }
                5 -> {
                    // Quick access demo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Auto-Sync", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryIndigo)
                            Text("Detects clipboard", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("1-Tap Copy", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentPurple)
                            Text("Fast paste back", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Category Filter", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentCyan)
                            Text("Code, Links, Colors", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isPositive: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isPositive) SuccessEmerald else Color(0xFFEF4444),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isPositive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
