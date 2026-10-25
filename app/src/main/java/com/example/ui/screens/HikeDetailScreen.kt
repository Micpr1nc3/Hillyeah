package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.HillYeahConstants
import com.example.ui.HillYeahViewModel
import com.example.ui.components.DifficultyBadge
import com.example.ui.components.ProvinceBadge
import com.example.ui.navigation.Screen

@Composable
fun HikeDetailScreen(
    viewModel: HillYeahViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hikeWithDetails by viewModel.selectedHikeDetail.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allOfflineTrails by viewModel.allOfflineTrails.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    if (hikeWithDetails == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Loading hike event details...", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val details = hikeWithDetails!!
    val hike = details.hike
    val isCancelled = hike.status == "cancelled"
    val isHost = details.isHost
    val userRsvp = details.userRsvp
    val isGoing = userRsvp?.status == "going"
    var selectedGuests by remember(userRsvp) {
        mutableIntStateOf(userRsvp?.guestsCount ?: 1)
    }

    val hostId = hike.hostId
    val dynamicTrailName = when {
        hike.trailName.isNotBlank() && !hike.trailName.trim().equals("the", ignoreCase = true) -> hike.trailName.trim()
        hike.title.isNotBlank() && !hike.title.trim().equals("the", ignoreCase = true) -> hike.title.trim()
        else -> "Hennops Krokodilberg Trail"
    }
    var showShareFallbackModal by remember { mutableStateOf(false) }
    val cleanBaseUrl = HillYeahConstants.SharedAppBaseUrl.trimEnd('/')
    val shareUrl = "$cleanBaseUrl/#/?hikId=${hike.id}&hostId=$hostId"
    val shareText = "You're invited to join the $dynamicTrailName hike on HillYeah! 🌿\n\n" +
        "🏔️ Hike: \"${hike.title}\"\n" +
        "📍 Trail: $dynamicTrailName (${hike.province})\n" +
        "📅 Date: ${hike.date} at ${hike.time}\n" +
        "🥾 Distance: ${hike.distanceKm} km · Difficulty: ${hike.difficulty}\n\n" +
        "Accept or decline your invite here:\n$shareUrl"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("hike_detail_screen")
    ) {
        // Banner Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            AsyncImage(
                model = hike.imageUrl,
                contentDescription = hike.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            ProvinceBadge(
                province = hike.province,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
            )

            if (isCancelled) {
                Surface(
                    color = Color(0xFFDC2626),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "CANCELLED",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else {
                DifficultyBadge(
                    difficulty = hike.difficulty,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = hike.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Trail: ${hike.trailName} · ${hike.location}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Host Profile Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { viewModel.viewMemberProfile(hike.hostId) }
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                AsyncImage(
                    model = hike.hostAvatar,
                    contentDescription = hike.hostName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Organized by ${hike.hostName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isHost) "You are hosting this hike" else "Community Host • View Profile →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (hike.trailId != null) {
                Spacer(modifier = Modifier.height(12.dp))
                val isTrailMapCached = allOfflineTrails.any { it.trailId == hike.trailId }
                Surface(
                    color = if (isTrailMapCached) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.navigateTo(Screen.TrailDetail(hike.trailId))
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = if (isTrailMapCached) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isTrailMapCached) Color(0xFF15803D) else MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isTrailMapCached) "Offline Map Cached (Room Database)" else "Offline Map Available",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isTrailMapCached) Color(0xFF15803D) else MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isTrailMapCached) "Tap to open GPS waypoints & contours offline" else "Tap to download and cache before heading out",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Text(
                            text = "View Map →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Consolidated Prominent Share Hike Action
            Button(
                onClick = {
                    try {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TITLE, "HillYeah Hike: ${hike.title}")
                            putExtra(Intent.EXTRA_SUBJECT, "Join me on $dynamicTrailName on HillYeah!")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share HillYeah Hike")
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        showShareFallbackModal = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("share_hike_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Hike",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Share Hike",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.navigateTo(Screen.InviteResponse(hike.id, hostId))
                    }
                    .testTag("preview_receiver_invite_link_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Preview Receiver Invite Link",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                text = "Test receiver flow to accept/decline with notes",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    Text(
                        text = "Open Link →",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date, Time, Distance Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = hike.date, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "at ${hike.time}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Straighten,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "${hike.distanceKm} km", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Distance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = if (details.isFull) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${details.attendeesCount}/${hike.capacity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (details.isFull) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (details.isFull) "FULL" else "${details.spotsLeft} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (details.isFull) Color(0xFFDC2626) else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // RSVP & Attendance Management Box
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isGoing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rsvp_container_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isGoing) "You are RSVP'd to this hike!" else "RSVP to Join",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isGoing) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )

                        if (isGoing) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (!isCancelled) {
                        Spacer(modifier = Modifier.height(12.dp))

                        // Guest Count Selector (1 to 4 spots)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total spots (You + Guests):",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                (1..4).forEach { count ->
                                    val isSelected = selectedGuests == count
                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .testTag("guest_option_$count")
                                    ) {
                                        TextButton(
                                            onClick = { selectedGuests = count },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Text(
                                                text = "$count",
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (isGoing) {
                                Button(
                                    onClick = {
                                        viewModel.submitRsvp(hike.id, "going", selectedGuests)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("update_rsvp_btn")
                                ) {
                                    Text("Update Guests (${selectedGuests})")
                                }

                                Button(
                                    onClick = {
                                        viewModel.submitRsvp(hike.id, "not_going", 0)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cancel_rsvp_btn")
                                ) {
                                    Text("Can't Make It")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        viewModel.submitRsvp(hike.id, "going", selectedGuests)
                                    },
                                    enabled = !details.isFull,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("rsvp_going_btn")
                                ) {
                                    Text(
                                        text = if (details.isFull) "Hike is Full (Capacity Reached)" else "RSVP: Going (${selectedGuests} spot${if (selectedGuests > 1) "s" else ""})",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "This hike has been cancelled by the organizer.",
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "Hike Notes & Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hike.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Meeting Point
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Meeting Point",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = hike.meetingPoint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Recommended Gear Checklist
            val gearList = remember(hike.difficulty, hike.distanceKm) {
                val list = mutableListOf(
                    "At least 2 Litres of fresh water",
                    "Sturdy trail running shoes or hiking boots",
                    "Wide-brim sun hat and sunscreen"
                )
                if (hike.difficulty.equals("challenging", ignoreCase = true) || hike.distanceKm >= 8) {
                    list.add("High-energy trail snacks & electrolytes")
                    list.add("Basic first-aid kit & blister plasters")
                }
                if (hike.distanceKm >= 10) {
                    list.add("Lightweight wind/rain shell")
                    list.add("Charged mobile phone / power bank")
                }
                list
            }

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "What to Bring (Recommended Gear)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    gearList.forEach { gearItem ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = gearItem,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Attendees List
            Text(
                text = "Hikers Joining (${details.attendeesCount})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (details.attendees.isEmpty()) {
                Text(
                    text = "No one has RSVP'd yet. Be the first to join!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    details.attendees.forEach { attendee ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.viewMemberProfile(attendee.userId) }
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            AsyncImage(
                                model = attendee.userAvatar,
                                contentDescription = attendee.userName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = attendee.userName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )

                            if (attendee.guestsCount > 1) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "+${attendee.guestsCount - 1} guest${if (attendee.guestsCount > 2) "s" else ""}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Host Actions: Edit & Cancel (if user is host and hike is active)
            if (isHost && !isCancelled) {
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("host_edit_hike_button")
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Hike Details (Host Action)", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { showCancelDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2), contentColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("host_cancel_hike_button")
                ) {
                    Text("Cancel this Hike (Host Action)", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = { viewModel.navigateBack() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Hikes")
            }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Group Hike?") },
            text = { Text("Are you sure you want to cancel this hike? All participants will see that it has been cancelled.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelHike(hike.id)
                        showCancelDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Yes, Cancel Hike")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Hike")
                }
            }
        )
    }

    if (showEditDialog) {
        var editTitle by remember(hike) { mutableStateOf(hike.title) }
        var editTrailName by remember(hike) { mutableStateOf(hike.trailName) }
        var editDate by remember(hike) { mutableStateOf(hike.date) }
        var editTime by remember(hike) { mutableStateOf(hike.time) }
        var editMeetingPoint by remember(hike) { mutableStateOf(hike.meetingPoint) }
        var editDescription by remember(hike) { mutableStateOf(hike.description) }
        var editError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Hike Details", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (editError != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = editError!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = {
                            editTitle = it
                            editError = null
                        },
                        label = { Text("Hike Title *") },
                        placeholder = { Text("e.g. Mindful Sunrise at Hennops") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editTrailName,
                        onValueChange = {
                            editTrailName = it
                            editError = null
                        },
                        label = { Text("Trail / Mountain Name *") },
                        placeholder = { Text("e.g. Hennops Krokodilberg Trail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = editDate,
                            onValueChange = { editDate = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editTime,
                            onValueChange = { editTime = it },
                            label = { Text("Time (HH:MM)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = editMeetingPoint,
                        onValueChange = { editMeetingPoint = it },
                        label = { Text("Meeting Point") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDescription,
                        onValueChange = { editDescription = it },
                        label = { Text("Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedTitle = editTitle.trim()
                        if (trimmedTitle.isBlank() || trimmedTitle.equals("the", ignoreCase = true) || trimmedTitle.length < 3) {
                            editError = "Please enter a proper, full hike title (e.g., 'Mindful Sunrise at Hennops')."
                            return@Button
                        }
                        val trimmedTrail = editTrailName.trim()
                        if (trimmedTrail.isBlank() || trimmedTrail.equals("the", ignoreCase = true)) {
                            editError = "Please specify a valid trail name (e.g., 'Hennops Krokodilberg Trail')."
                            return@Button
                        }

                        viewModel.updateHike(
                            hikeId = hike.id,
                            title = trimmedTitle,
                            trailName = trimmedTrail,
                            location = hike.location,
                            province = hike.province,
                            date = editDate.trim(),
                            time = editTime.trim(),
                            meetingPoint = editMeetingPoint.trim(),
                            difficulty = hike.difficulty,
                            distanceKm = hike.distanceKm,
                            capacity = hike.capacity,
                            description = editDescription.trim(),
                            onSuccess = {
                                showEditDialog = false
                            },
                            onFailure = { err ->
                                editError = err
                            }
                        )
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clean fallback share modal dialog if native share sheet is unavailable
    if (showShareFallbackModal) {
        AlertDialog(
            onDismissRequest = { showShareFallbackModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Share Hike Invite", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Share this invite with friends or group chats so they can RSVP directly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = shareText,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Hike Link", shareUrl)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "🔗 Hike invite link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showShareFallbackModal = false
                    }
                ) {
                    Text("Copy Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareFallbackModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}
