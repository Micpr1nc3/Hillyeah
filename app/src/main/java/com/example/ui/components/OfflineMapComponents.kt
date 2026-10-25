package com.example.ui.components

import com.example.ui.theme.ForestGreenPrimary
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OfflineTrailMap
import com.example.data.model.Trail
import com.example.data.model.TrailWaypoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OfflineTrailMapSection(
    trail: Trail,
    offlineMap: OfflineTrailMap?,
    isCached: Boolean,
    isDownloading: Boolean,
    onDownloadClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWaypoint by remember { mutableStateOf<TrailWaypoint?>(null) }
    var showWaypointsList by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCached) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_trail_map_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header with status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Offline Topographic Map",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Room SQLite On-Device Cache",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                if (isCached && offlineMap != null) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF15803D),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sizeMb = String.format(Locale.US, "%.1f MB", offlineMap.cacheSizeBytes / (1024.0 * 1024.0))
                            Text(
                                text = "CACHED ($sizeMb)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isCached && offlineMap != null) {
                val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(offlineMap.downloadedTimestamp))
                Text(
                    text = "Saved for offline navigation on $dateStr. Full GPS waypoints, contour profiles, and water markers stored in Room database. Works with zero cellular reception.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Topographic Canvas
                Text(
                    text = "Topographic Trail Path & Elevation Profile",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                TopographicTrailCanvas(
                    waypoints = offlineMap.waypoints,
                    selectedWaypoint = selectedWaypoint,
                    onSelectWaypoint = { selectedWaypoint = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF3EFE6))
                )

                // Selected Waypoint Popup / Info Card
                if (selectedWaypoint != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "📍 ${selectedWaypoint!!.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Km ${selectedWaypoint!!.kmMark} · ${selectedWaypoint!!.elevationM}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "GPS: ${String.format(Locale.US, "%.4f, %.4f", selectedWaypoint!!.latitude, selectedWaypoint!!.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedWaypoint!!.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Elevation Profile Canvas
                Text(
                    text = "Elevation Gain Profile Across Trail (km)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ElevationProfileCanvas(
                    elevationProfile = offlineMap.elevationProfile,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFEBE6DC))
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Toggle Waypoints List Button
                OutlinedButton(
                    onClick = { showWaypointsList = !showWaypointsList },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (showWaypointsList) "Hide GPS Waypoint Coordinates" else "Show All GPS Waypoint Coordinates (${offlineMap.waypoints.size})")
                }

                AnimatedVisibility(visible = showWaypointsList) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 10.dp)
                    ) {
                        offlineMap.waypoints.forEach { waypoint ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                    .clickable { selectedWaypoint = waypoint }
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = when (waypoint.type) {
                                        "trailhead" -> Icons.Default.Flag
                                        "water" -> Icons.Default.Opacity
                                        "summit" -> Icons.Default.Landscape
                                        "hazard" -> Icons.Default.Warning
                                        else -> Icons.Default.Place
                                    },
                                    contentDescription = null,
                                    tint = when (waypoint.type) {
                                        "water" -> Color(0xFF0284C7)
                                        "summit" -> MaterialTheme.colorScheme.primary
                                        "hazard" -> Color(0xFFDC2626)
                                        else -> MaterialTheme.colorScheme.secondary
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = waypoint.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "GPS: ${waypoint.latitude}, ${waypoint.longitude} · ${waypoint.elevationM}m",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                    Text(
                                        text = waypoint.description,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Remove from offline cache button
                OutlinedButton(
                    onClick = onRemoveClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("remove_offline_cache_btn")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Clear Offline Map Cache", fontSize = 13.sp)
                }

            } else {
                // Not cached state
                Text(
                    text = "Going hiking in areas with weak or zero mobile reception? Cache this trail's topographic map, GPS waypoints, water beacons, and elevation profile into Room database for instant offline access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDownloadClick,
                    enabled = !isDownloading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("download_offline_map_btn")
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Caching Trail Map to Room...")
                    } else {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download & Cache for Offline Use (Room)")
                    }
                }
            }
        }
    }
}

@Composable
fun TopographicTrailCanvas(
    waypoints: List<TrailWaypoint>,
    selectedWaypoint: TrailWaypoint?,
    onSelectWaypoint: (TrailWaypoint) -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.testTag("topographic_trail_canvas")) {
        val width = size.width
        val height = size.height

        // 1. Draw decorative topographic contour lines in the background
        val contourColor = Color(0x334F644A)
        for (i in 1..4) {
            val yOffset = height * (i * 0.22f)
            val contourPath = Path().apply {
                moveTo(0f, yOffset)
                cubicTo(
                    width * 0.25f, yOffset - 25f,
                    width * 0.65f, yOffset + 30f,
                    width, yOffset - 10f
                )
            }
            drawPath(
                path = contourPath,
                color = contourColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        if (waypoints.isEmpty()) return@Canvas

        // 2. Map waypoints to canvas coordinates along a serpentine trail
        val points = waypoints.mapIndexed { index, _ ->
            val fraction = index.toFloat() / (waypoints.size - 1).coerceAtLeast(1)
            val x = 40.dp.toPx() + fraction * (width - 80.dp.toPx())
            // Serpentine mountain curve
            val y = height * 0.80f - (fraction * height * 0.65f) + (if (index % 2 == 1) -25f else 15f)
            Offset(x, y)
        }

        // 3. Draw the hiking trail path
        val trailPath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val curr = points[i]
                val midX = (prev.x + curr.x) / 2
                val midY = (prev.y + curr.y) / 2
                quadraticTo(prev.x, prev.y, midX, midY)
                lineTo(curr.x, curr.y)
            }
        }

        // Trail glow
        drawPath(
            path = trailPath,
            color = Color(0x33264634),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Main trail line
        drawPath(
            path = trailPath,
            color = ForestGreenPrimary,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 4. Draw Waypoint nodes on canvas
        points.forEachIndexed { index, pt ->
            val wp = waypoints[index]
            val isSelected = selectedWaypoint?.name == wp.name

            val nodeColor = when (wp.type) {
                "trailhead" -> Color(0xFF38BDF8)
                "water" -> Color(0xFF0284C7)
                "summit" -> Color(0xFF89D680)
                "hazard" -> Color(0xFFEF4444)
                else -> Color(0xFFE5A93C)
            }

            // Outer ring
            drawCircle(
                color = if (isSelected) Color.White else Color(0x66FFFFFF),
                radius = if (isSelected) 12.dp.toPx() else 8.dp.toPx(),
                center = pt
            )
            // Core
            drawCircle(
                color = nodeColor,
                radius = if (isSelected) 8.dp.toPx() else 5.dp.toPx(),
                center = pt
            )
        }
    }
}

@Composable
fun ElevationProfileCanvas(
    elevationProfile: List<com.example.data.model.ElevationPoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.testTag("elevation_profile_canvas")) {
        val width = size.width
        val height = size.height

        if (elevationProfile.size < 2) return@Canvas

        val minElev = elevationProfile.minOf { it.elevationM }.toFloat()
        val maxElev = elevationProfile.maxOf { it.elevationM }.toFloat().coerceAtLeast(minElev + 10f)
        val maxDist = elevationProfile.maxOf { it.distanceKm }.toFloat().coerceAtLeast(1f)

        val paddingX = 30.dp.toPx()
        val paddingY = 20.dp.toPx()
        val chartWidth = width - (paddingX * 2)
        val chartHeight = height - (paddingY * 2)

        val profilePath = Path()
        val fillPath = Path()

        elevationProfile.forEachIndexed { i, pt ->
            val x = paddingX + (pt.distanceKm.toFloat() / maxDist) * chartWidth
            val normalizedElev = (pt.elevationM.toFloat() - minElev) / (maxElev - minElev)
            val y = paddingY + chartHeight - (normalizedElev * chartHeight)

            if (i == 0) {
                profilePath.moveTo(x, y)
                fillPath.moveTo(x, paddingY + chartHeight)
                fillPath.lineTo(x, y)
            } else {
                profilePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (i == elevationProfile.lastIndex) {
                fillPath.lineTo(x, paddingY + chartHeight)
                fillPath.close()
            }
        }

        // Fill under elevation curve
        drawPath(
            path = fillPath,
            color = Color(0x332D5A27)
        )

        // Elevation stroke
        drawPath(
            path = profilePath,
            color = Color(0xFF89D680),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Points
        elevationProfile.forEach { pt ->
            val x = paddingX + (pt.distanceKm.toFloat() / maxDist) * chartWidth
            val normalizedElev = (pt.elevationM.toFloat() - minElev) / (maxElev - minElev)
            val y = paddingY + chartHeight - (normalizedElev * chartHeight)
            drawCircle(
                color = Color(0xFFFBBF24),
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}
