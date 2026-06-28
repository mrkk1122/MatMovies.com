package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.components.AnimatedAppName
import com.example.ui.components.MoviesScaffold
import com.example.ui.navigation.Screen
import com.example.ui.utils.DrawableHelper
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MovieViewModel

@Composable
fun ProfileScreen(
    navController: NavController,
    movieViewModel: MovieViewModel,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        movieViewModel.setActiveUser(currentUser?.id)
    }

    val watchHistory by movieViewModel.watchHistory.collectAsState()
    var showSubscriptionDialog by remember { mutableStateOf(false) }
    var showAppSettingsDialog by remember { mutableStateOf(false) }
    var showHelpSupportDialog by remember { mutableStateOf(false) }

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.Profile.route
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedAppName(fontSize = 26.sp)
                Text(
                    text = "My Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Profile Card Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentUser != null) {
                        val avatarId = DrawableHelper.getDrawableIdByName("img_profile_avatar")
                        Image(
                            painter = painterResource(id = avatarId),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(45.dp))
                                .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(45.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentUser!!.username,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = currentUser!!.email,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Subscription Badge
                        val status = currentUser!!.subscriptionStatus
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (status) {
                                    "VIP" -> Color(0xFFFFD700)
                                    "Premium" -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "$status Plan".uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Guest",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(90.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Guest Viewer",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Sign in to sync watchlist and unlock premium features",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.navigate(Screen.Login.route) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.7f).testTag("profile_login_button")
                        ) {
                            Text("Sign In / Register", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Console for Administrator Roles!
            if (currentUser?.role == "Admin") {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 16.dp)
                        .clickable { navController.navigate(Screen.AdminDashboard.route) }
                        .testTag("admin_dashboard_shortcut"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Admin Console Active", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                            Text("Manage movie titles, view metrics and upload content", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // Watch History Section
            SectionHeader(title = "Recently Watched")
            if (watchHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No watch history found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistory) { movie ->
                        MovieCard(
                            movie = movie,
                            onClick = {
                                navController.navigate(Screen.MovieDetails.createRoute(movie.id))
                            }
                        )
                    }
                }
                
                TextButton(
                    onClick = { movieViewModel.clearWatchHistory() },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(horizontal = 8.dp)
                ) {
                    Text("Clear Watch History", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.padding(vertical = 16.dp))

            // Settings/Management menu list items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileMenuItem(
                    icon = Icons.Default.CardMembership,
                    title = "Manage Subscription Plan",
                    subtitle = "Upgrade to premium or VIP plans",
                    onClick = {
                        if (currentUser != null) {
                            showSubscriptionDialog = true
                        } else {
                            navController.navigate(Screen.Login.route)
                        }
                    }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Settings,
                    title = "App Settings",
                    subtitle = "Stream quality, autoplay preferences",
                    onClick = { showAppSettingsDialog = true }
                )

                ProfileMenuItem(
                    icon = Icons.Default.Help,
                    title = "Help & Support",
                    subtitle = "Terms, FAQs, Contact MatMovies Team",
                    onClick = { showHelpSupportDialog = true }
                )

                if (currentUser != null) {
                    ProfileMenuItem(
                        icon = Icons.Default.Logout,
                        title = "Sign Out",
                        subtitle = "Sign out of your account",
                        tint = MaterialTheme.colorScheme.secondary,
                        onClick = {
                            authViewModel.logout {
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Home.route) { inclusive = true }
                                }
                            }
                        }
                    )
                } else {
                    ProfileMenuItem(
                        icon = Icons.Default.Login,
                        title = "Sign In / Register",
                        subtitle = "Sign in to sync your experience",
                        tint = MaterialTheme.colorScheme.primary,
                        onClick = {
                            navController.navigate(Screen.Login.route)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Interactive Subscription pricing plan upgrade portal dialog!
    if (showSubscriptionDialog) {
        AlertDialog(
            onDismissRequest = { showSubscriptionDialog = false },
            title = {
                Text(
                    text = "Choose Your Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Unlock high definition audio, premium subtitle channels, and VIP streaming content with MatMovies Premium plans.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    PlanCard(
                        title = "MatMovies Premium",
                        price = "$9.99/month",
                        features = "1080p Streaming • Surround Audio • All Genres",
                        isSelected = currentUser?.subscriptionStatus == "Premium",
                        onClick = {
                            authViewModel.upgradeSubscription("Premium")
                            showSubscriptionDialog = false
                        }
                    )

                    PlanCard(
                        title = "MatMovies VIP Gold",
                        price = "$14.99/month",
                        features = "4K HDR Streaming • Multi-Device • Exclusives",
                        isSelected = currentUser?.subscriptionStatus == "VIP",
                        onClick = {
                            authViewModel.upgradeSubscription("VIP")
                            showSubscriptionDialog = false
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSubscriptionDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showAppSettingsDialog) {
        AppSettingsDialog(onDismiss = { showAppSettingsDialog = false })
    }

    if (showHelpSupportDialog) {
        HelpSupportDialog(onDismiss = { showHelpSupportDialog = false })
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = if (tint == Color.White) MaterialTheme.colorScheme.primary else tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = tint)
            Text(text = subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    features: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 0.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text(price, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
            }
            Text(
                features,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun AppSettingsDialog(
    onDismiss: () -> Unit
) {
    var streamQuality by remember { mutableStateOf("Auto") }
    var autoplayEnabled by remember { mutableStateOf(true) }
    var cacheClearedMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "App Settings ⚙️",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Stream Quality selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Video Stream Quality",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    listOf("Auto (Recommended)", "Full HD 1080p", "Data Saver 480p").forEach { quality ->
                        val isSelected = streamQuality == quality || (streamQuality == "Auto" && quality.startsWith("Auto"))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { streamQuality = quality }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(quality, fontSize = 13.sp, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f))
                            RadioButton(
                                selected = isSelected,
                                onClick = { streamQuality = quality },
                                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                // Autoplay Toggle Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Autoplay Next Episode",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Automatically start next episode when current ends",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoplayEnabled,
                        onCheckedChange = { autoplayEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                // Storage Option (Clear cache)
                Button(
                    onClick = {
                        cacheClearedMessage = "Cache cleared successfully! 48.5 MB freed."
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Cache", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Local Playback Cache", color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 12.sp)
                }

                if (cacheClearedMessage.isNotEmpty()) {
                    Text(
                        text = cacheClearedMessage,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Settings", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White.copy(alpha = 0.6f))
            }
        }
    )
}

@Composable
fun HelpSupportDialog(
    onDismiss: () -> Unit
) {
    var expandedFAQIndex by remember { mutableStateOf(-1) }
    var actionFeedback by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Help & Support 💬",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Frequently Asked Questions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )

                val faqs = listOf(
                    "How do offline downloads work?" to "To download videos offline, go to any video details screen and click the 'Download' icon. The movie or series episode will download in full HD and save directly to your in-app simulated storage. You can play downloaded videos anytime from the Downloads tab even without an active internet connection.",
                    "Can I upgrade my plan anytime?" to "Yes! You can choose either Premium or VIP plans anytime on the Profile page. Upgrade payments are processed securely, granting you instant access to HD content.",
                    "Is cast to Smart TV supported?" to "Cast option is available in the media player interface. Make sure your Smart TV and phone are connected to the same network to enjoy MatMovies on the big screen."
                )

                faqs.forEachIndexed { index, (question, answer) ->
                    val isExpanded = expandedFAQIndex == index
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFAQIndex = if (isExpanded) -1 else index },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Toggle Answer",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = answer,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.15f))

                Text(
                    text = "Still need help? Contact Us!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Button(
                    onClick = {
                        actionFeedback = "Email copied to clipboard: support@matmovies.com"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = "Email Support", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Support Email Address", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (actionFeedback.isNotEmpty()) {
                    Text(
                        text = actionFeedback,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onSecondaryContainer, fontWeight = FontWeight.Bold)
            }
        }
    )
}

