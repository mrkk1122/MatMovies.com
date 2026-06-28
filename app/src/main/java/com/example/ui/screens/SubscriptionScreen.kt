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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.viewmodel.AuthViewModel
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()

    val sharedPrefs = remember { context.getSharedPreferences("moviesbox_subscriptions", android.content.Context.MODE_PRIVATE) }
    
    var selectedPlanDays by remember { mutableStateOf(30) }
    var selectedPlanName by remember { mutableStateOf("Premium HD") }
    var planPrice by remember { mutableStateOf(199) } // Bangla Taka or USD

    var paymentMethod by remember { mutableStateOf("bKash") }
    var transactionId by remember { mutableStateOf("") }
    var senderNumber by remember { mutableStateOf("") }
    
    var requestsList by remember { mutableStateOf(loadSubscriptionRequests(sharedPrefs, currentUser?.email ?: "")) }

    // Update prices based on duration
    LaunchedEffect(selectedPlanDays) {
        when (selectedPlanDays) {
            30 -> {
                selectedPlanName = "Premium HD"
                planPrice = 199
            }
            90 -> {
                selectedPlanName = "Super Saver"
                planPrice = 499
            }
            180 -> {
                selectedPlanName = "Mega Stream"
                planPrice = 899
            }
            365 -> {
                selectedPlanName = "VIP Cinema Yearly"
                planPrice = 1499
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Portal", fontWeight = FontWeight.Bold, color = Color.White) },
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
        if (currentUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Please Sign In to Subscribe", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { navController.navigate("login") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                    ) {
                        Text("Sign In Now", color = Color.Black)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Header Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB300).copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardMembership,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Active Subscription",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = currentUser?.subscriptionStatus ?: "Free Member",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser?.subscriptionStatus == "Free") Color.LightGray else Color(0xFFFFB300)
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "1. Select Subscription Duration",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val plans = listOf(30, 90, 180, 365)
                        plans.forEach { days ->
                            val isSelected = selectedPlanDays == days
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPlanDays = days },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFFFFB300).copy(alpha = 0.15f) else Color(0xFF141419)
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.1f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$days Days",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = if (isSelected) Color(0xFFFFB300) else Color.White
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when(days) {
                                            30 -> "৳199"
                                            90 -> "৳499"
                                            180 -> "৳899"
                                            else -> "৳1499"
                                        },
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    // Selected Plan Info Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Selected Plan:", color = Color.White.copy(alpha = 0.6f))
                                Text(selectedPlanName, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Validity:", color = Color.White.copy(alpha = 0.6f))
                                Text("$selectedPlanDays Days Access", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Amount:", color = Color.White.copy(alpha = 0.6f))
                                Text("৳$planPrice BDT", fontWeight = FontWeight.Black, color = Color(0xFFFFB300), fontSize = 18.sp)
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "2. Make Payment & Fill details",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Please send ৳$planPrice BDT to either bKash or Nagad Personal number: 01700-000000 and enter the payment sender mobile number and Transaction ID below.",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment Method Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("bKash", "Nagad").forEach { method ->
                                    val isSelected = paymentMethod == method
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { paymentMethod = method },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) Color(0xFFFFB300).copy(alpha = 0.1f) else Color.Transparent
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) Color(0xFFFFB300) else Color.White.copy(alpha = 0.1f)
                                        )
                                    ) {
                                        Text(
                                            text = method,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            textAlign = TextAlign.Center,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFFFFB300) else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = senderNumber,
                                onValueChange = { senderNumber = it },
                                label = { Text("Sender Mobile Number") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFFFFB300)) },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFB300),
                                    focusedLabelColor = Color(0xFFFFB300)
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = transactionId,
                                onValueChange = { transactionId = it },
                                label = { Text("Transaction ID (TrxID)") },
                                leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, tint = Color(0xFFFFB300)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFB300),
                                    focusedLabelColor = Color(0xFFFFB300)
                                )
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            if (senderNumber.isBlank() || transactionId.isBlank()) {
                                Toast.makeText(context, "Please enter sender number and transaction ID", Toast.LENGTH_SHORT).show()
                            } else {
                                saveSubscriptionRequest(
                                    sharedPrefs = sharedPrefs,
                                    email = currentUser?.email ?: "",
                                    planName = selectedPlanName,
                                    days = selectedPlanDays,
                                    price = planPrice,
                                    method = paymentMethod,
                                    number = senderNumber,
                                    trxId = transactionId
                                )
                                Toast.makeText(context, "Payment Request Sent to Admin successfully!", Toast.LENGTH_LONG).show()
                                senderNumber = ""
                                transactionId = ""
                                requestsList = loadSubscriptionRequests(sharedPrefs, currentUser?.email ?: "")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Request To Admin", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 16.sp)
                    }
                }

                if (requestsList.isNotEmpty()) {
                    item {
                        Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(vertical = 8.dp))
                        Text(
                            text = "Your Recent Subscription Requests",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    items(requestsList) { req ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = req.planName, fontWeight = FontWeight.Bold, color = Color.White)
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (req.status) {
                                                "Approved" -> Color(0xFF34A853).copy(alpha = 0.2f)
                                                "Declined" -> Color(0xFFEA4335).copy(alpha = 0.2f)
                                                else -> Color(0xFFFFB300).copy(alpha = 0.2f)
                                            }
                                        )
                                    ) {
                                        Text(
                                            text = req.status,
                                            color = when (req.status) {
                                                "Approved" -> Color(0xFF34A853)
                                                "Declined" -> Color(0xFFEA4335)
                                                else -> Color(0xFFFFB300)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("TrxID: ${req.trxId} • Sender: ${req.number}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.6f))
                                Text("Requested: ${req.days} Days for ৳${req.price} via ${req.method}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}

data class SubscriptionRequest(
    val email: String,
    val planName: String,
    val days: Int,
    val price: Int,
    val method: String,
    val number: String,
    val trxId: String,
    val status: String,
    val timestamp: Long
)

fun loadSubscriptionRequests(sharedPrefs: android.content.SharedPreferences, email: String): List<SubscriptionRequest> {
    val list = mutableListOf<SubscriptionRequest>()
    val jsonString = sharedPrefs.getString("requests_list", "[]") ?: "[]"
    try {
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val reqEmail = obj.getString("email")
            if (reqEmail == email || email.lowercase() == "admin@moviesbox.com") {
                list.add(
                    SubscriptionRequest(
                        email = reqEmail,
                        planName = obj.getString("planName"),
                        days = obj.getInt("days"),
                        price = obj.getInt("price"),
                        method = obj.getString("method"),
                        number = obj.getString("number"),
                        trxId = obj.getString("trxId"),
                        status = obj.getString("status"),
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list.sortedByDescending { it.timestamp }
}

fun saveSubscriptionRequest(
    sharedPrefs: android.content.SharedPreferences,
    email: String,
    planName: String,
    days: Int,
    price: Int,
    method: String,
    number: String,
    trxId: String
) {
    val jsonString = sharedPrefs.getString("requests_list", "[]") ?: "[]"
    try {
        val array = JSONArray(jsonString)
        val obj = JSONObject().apply {
            put("email", email)
            put("planName", planName)
            put("days", days)
            put("price", price)
            put("method", method)
            put("number", number)
            put("trxId", trxId)
            put("status", "Pending")
            put("timestamp", System.currentTimeMillis())
        }
        array.put(obj)
        sharedPrefs.edit().putString("requests_list", array.toString()).apply()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
