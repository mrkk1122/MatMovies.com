package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpSupportScreen(
    navController: NavController
) {
    val context = LocalContext.current

    var supportSubject by remember { mutableStateOf("") }
    var supportMessage by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var expandedFaqIndex by remember { mutableStateOf(-1) }

    val faqs = listOf(
        "How do I download movies offline?" to "To download movies offline, open the movie details page, and click the 'Download' button. Once completed, your video will be stored securely on your local storage and can be watched on the 'Downloads' tab without an internet connection.",
        "How long does subscription approval take?" to "Our payment specialists verify and approve payment requests as soon as they are received. Usually, verification takes 10 to 30 minutes. Once verified, your status upgrades automatically!",
        "Can I stream in multiple devices?" to "Standard members can stream on one screen. Premium and VIP subscribers can concurrently stream on up to 4 high-definition devices securely at the same time.",
        "What should I do if a video buffers?" to "Make sure your connection is stable (at least 5 Mbps for HD). You can also adjust the Stream Quality from settings to 'Data Saver (480p)' or standard 720p to enjoy seamless, uninterrupted viewing.",
        "How can I contact live chat agents?" to "Simply click the 'Start Live Support Chat' option under the contact section below to instantly connect with a customer satisfaction specialist."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help & Support", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F13))
            )
        },
        containerColor = Color(0xFF0F0F13)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Help Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.15f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("24/7 Live Support Available", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Text("Avg. response time: 2 minutes", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Initializing secure Live Chat channel... Please stand by.", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                        ) {
                            Text("Start Live Support Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // FAQs Section Header
            item {
                Text(
                    text = "Frequently Asked Questions",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // Collapsible FAQ list
            items(faqs.size) { index ->
                val (question, answer) = faqs[index]
                val isExpanded = expandedFaqIndex == index

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedFaqIndex = if (isExpanded) -1 else index },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(question, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            }
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f)
                            )
                        }

                        AnimatedVisibility(
                            visible = isExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                Divider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = answer,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }

            // Support Ticket Header
            item {
                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Submit a Support Ticket",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }

            // Interactive Ticket Submit fields
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Fill up this form to submit your inquiry directly to our support team.",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )

                        OutlinedTextField(
                            value = supportSubject,
                            onValueChange = { supportSubject = it },
                            label = { Text("Subject") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFB300),
                                focusedLabelColor = Color(0xFFFFB300)
                            )
                        )

                        OutlinedTextField(
                            value = supportMessage,
                            onValueChange = { supportMessage = it },
                            label = { Text("Describe your problem") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 5,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFFB300),
                                focusedLabelColor = Color(0xFFFFB300)
                            )
                        )

                        Button(
                            onClick = {
                                if (supportSubject.isBlank() || supportMessage.isBlank()) {
                                    Toast.makeText(context, "Please fill in all ticket fields", Toast.LENGTH_SHORT).show()
                                } else {
                                    isSubmitting = true
                                    Toast.makeText(context, "Support Ticket submitted successfully! Ticket ID: #MB-${(1000..9999).random()}", Toast.LENGTH_LONG).show()
                                    supportSubject = ""
                                    supportMessage = ""
                                    isSubmitting = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Submit Ticket", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
