package bd.ac.du.fm.noticetracker.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bd.ac.du.fm.noticetracker.data.model.Notice
import bd.ac.du.fm.noticetracker.ui.theme.FmDuTheme
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : ComponentActivity() {
    private val viewModel: NoticeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FmDuTheme {
                RequestNotificationPermission()
                NoticeScreen(viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* granted */ }

            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(viewModel: NoticeViewModel) {
    val context = LocalContext.current
    val notices by viewModel.notices.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val lastChecked by viewModel.lastChecked.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FM DU Notices",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (lastChecked > 0) "Last checked: ${formatTime(lastChecked)}" else "Ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    if (unreadCount > 0) {
                        IconButton(onClick = { viewModel.markAllAsRead() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Mark all read")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background monitoring banner
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Background Monitor Active",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "WorkManager checks fm.du.ac.bd every 15 min and sends alerts for new notices.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipItem(
                    label = "All (${notices.size})",
                    isSelected = selectedFilter == "All",
                    onClick = { viewModel.setFilter("All") }
                )
                FilterChipItem(
                    label = "Homepage Ticker",
                    isSelected = selectedFilter == "Ticker",
                    onClick = { viewModel.setFilter("Ticker") }
                )
                FilterChipItem(
                    label = "MBBS Category",
                    isSelected = selectedFilter == "MBBS",
                    onClick = { viewModel.setFilter("MBBS") }
                )
            }

            // Notice list
            if (notices.isEmpty() && isRefreshing) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notices.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No notices found.\nTap refresh to check fm.du.ac.bd",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notices, key = { it.id }) { notice ->
                        NoticeCard(
                            notice = notice,
                            onOpen = {
                                viewModel.markAsRead(notice.id)
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(notice.link)).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(intent)
                            },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "${notice.title}\n${notice.link}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Notice"))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) }
    )
}

@Composable
fun NoticeCard(
    notice: Notice,
    onOpen: () -> Unit,
    onShare: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Badges row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Source badge
                Text(
                    text = notice.source,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // Date
                Text(
                    text = notice.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notice Title
            Text(
                text = notice.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (notice.isRead) FontWeight.Normal else FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 21.sp
                ),
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                FilledTonalButton(
                    onClick = onOpen,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Open PDF", fontSize = 13.sp)
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val sdf = SimpleDateFormat("h:mm a, dd MMM", Locale.getDefault())
    return sdf.format(Date(millis))
}
