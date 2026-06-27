package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.components.MoviesScaffold
import com.example.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    movieViewModel: MovieViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var ratingStr by remember { mutableStateOf("4.5") }
    var yearStr by remember { mutableStateOf("2026") }
    var duration by remember { mutableStateOf("2h 05m") }
    var genre by remember { mutableStateOf("Sci-Fi, Action") }
    var videoUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
    
    var selectedPoster by remember { mutableStateOf("img_poster_cyberpunk") }
    var isFeatured by remember { mutableStateOf(false) }
    var isTrending by remember { mutableStateOf(false) }

    var uploadSuccessMessage by remember { mutableStateOf("") }
    var activeTab by remember { mutableStateOf(0) }

    val movies by movieViewModel.allMovies.collectAsState()

    MoviesScaffold(
        navController = navController,
        currentRoute = Screen.AdminDashboard.route,
        topBar = {
            TopAppBar(
                title = { Text("MatMovies Admin Portal", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: Dashboard Analytics Metrics
            Text(
                text = "Platform Analytics",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Total Movies",
                    value = movies.size.toString(),
                    icon = Icons.Default.Movie,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "System Status",
                    value = "Stable",
                    icon = Icons.Default.HealthAndSafety,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "API Server",
                    value = "Active",
                    icon = Icons.Default.CloudQueue,
                    modifier = Modifier.weight(1f)
                )

                MetricCard(
                    title = "Database",
                    value = "Online",
                    icon = Icons.Default.Storage,
                    modifier = Modifier.weight(1f)
                )
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant)

            // Smart Admin Switcher Tab
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Button(
                    onClick = { activeTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeTab == 0) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(1.5f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = "Upload Mode",
                        tint = if (activeTab == 0) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deploy Content", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent,
                        contentColor = if (activeTab == 1) Color.Black else Color.White
                    ),
                    modifier = Modifier.weight(2f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Ubuntu Mode",
                        tint = if (activeTab == 1) Color.Black else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ubuntu Connection", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (activeTab == 0) {
                // TAB 0: Content Deployment Form
                Text(
                    text = "Deploy New Streaming Content",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (uploadSuccessMessage.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(uploadSuccessMessage, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Movie / TV Show Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_title"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Synopsis / Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("admin_upload_desc"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = ratingStr,
                            onValueChange = { ratingStr = it },
                            label = { Text("Rating (e.g., 4.5)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_upload_rating"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = yearStr,
                            onValueChange = { yearStr = it },
                            label = { Text("Release Year") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_upload_year"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = duration,
                            onValueChange = { duration = it },
                            label = { Text("Duration (e.g., 2h 05m)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_upload_duration"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = genre,
                            onValueChange = { genre = it },
                            label = { Text("Genre (e.g., Action, Sci-Fi)") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_upload_genre"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = videoUrl,
                        onValueChange = { videoUrl = it },
                        label = { Text("Video Streaming URL (MP4 / HLS)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_video_url"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    // Poster Style Selector Dropdown/Buttons
                    Text("Select Key Visual Poster:", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("img_poster_cyberpunk", "img_poster_fantasy", "img_hero_dune").forEach { poster ->
                            val label = when (poster) {
                                "img_poster_cyberpunk" -> "Cyberpunk"
                                "img_poster_fantasy" -> "Fantasy"
                                else -> "Sci-Fi Dune"
                            }
                            val isSel = selectedPoster == poster
                            Button(
                                onClick = { selectedPoster = poster },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(label, fontSize = 10.sp, color = if (isSel) Color.Black else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Toggles for featured/trending properties
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                        Text("Feature on Home Carousel Banner", fontSize = 13.sp, color = Color.White)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isTrending, onCheckedChange = { isTrending = it })
                        Text("Add to Trending Row", fontSize = 13.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                movieViewModel.uploadMovie(
                                    title = title.trim(),
                                    description = description.trim(),
                                    rating = ratingStr.toDoubleOrNull() ?: 4.5,
                                    year = yearStr.toIntOrNull() ?: 2026,
                                    duration = duration.trim(),
                                    genre = genre.trim(),
                                    videoUrl = videoUrl.trim(),
                                    posterDrawableName = selectedPoster,
                                    isFeatured = isFeatured,
                                    isTrending = isTrending,
                                    isLatest = true
                                ) {
                                    uploadSuccessMessage = "deployed '$title' to OTT stream controllers."
                                    // Reset fields
                                    title = ""
                                    description = ""
                                    isFeatured = false
                                    isTrending = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("admin_upload_submit"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload & Deploy Content", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            } else {
                // TAB 1: Ubuntu Connection Guide
                Text(
                    text = "Ubuntu Server Connection Panel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Host your MatMovies FastAPI backend on an Ubuntu Server to handle global requests.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Step 1
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("1", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Install System Dependencies", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Connect via SSH and install python3, pip, venv, and nginx web engine:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        ServerCodeBlock(
                            "ssh user@your_server_ip\n" +
                            "sudo apt update && sudo apt upgrade -y\n" +
                            "sudo apt install python3 python3-pip python3-venv nginx git curl -y"
                        )
                    }
                }

                // Step 2
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("2", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Setup App & Dependencies", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Create directories, upload files, and activate virtual environment:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        ServerCodeBlock(
                            "sudo mkdir -p /var/www/matmovies\n" +
                            "sudo chown -R \$USER:\$USER /var/www/matmovies\n" +
                            "cd /var/www/matmovies\n" +
                            "python3 -m venv venv\n" +
                            "source venv/bin/activate\n" +
                            "pip install -r requirements.txt"
                        )
                    }
                }

                // Step 3
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Daemonize Service (Systemd)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Keep your server running 24/7. Create `/etc/systemd/system/matmovies.service` and configure:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        ServerCodeBlock(
                            "[Service]\n" +
                            "ExecStart=/var/www/matmovies/venv/bin/uvicorn main:app --host 127.0.0.1 --port 8000\n" +
                            "Restart=always\n\n" +
                            "# Commands to control service:\n" +
                            "sudo systemctl enable matmovies\n" +
                            "sudo systemctl start matmovies"
                        )
                    }
                }

                // Step 4
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("4", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Nginx Reverse Proxy & SSL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Configure Nginx proxy block for your domain and secure with Let's Encrypt SSL (HTTPS is required for secure Android API calls):", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        ServerCodeBlock(
                            "# Configure proxy pass to http://127.0.0.1:8000\n" +
                            "sudo apt install certbot python3-certbot-nginx -y\n" +
                            "sudo certbot --nginx -d api.matmovies.com"
                        )
                    }
                }

                // Step 5
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("5", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Connect MatMovies Android App", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("Update your network retrofit module with your custom server URL:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        ServerCodeBlock(
                            "const val BASE_URL = \"https://api.matmovies.com/api/v1/\""
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun ServerCodeBlock(command: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111111)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SelectionContainer {
            Text(
                text = command,
                color = Color(0xFF00FF66),
                fontSize = 11.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}
