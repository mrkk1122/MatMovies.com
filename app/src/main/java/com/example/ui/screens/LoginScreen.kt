package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.utils.DrawableHelper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ui.navigation.Screen
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.components.AnimatedAppName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var isPasswordVisible by remember { mutableStateFlowOf(false) }
    var showGoogleAuth by remember { mutableStateOf(false) }
    var showFacebookAuth by remember { mutableStateOf(false) }

    val errorMsg by authViewModel.loginError.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    // Clear login errors on entering
    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E0608),
                        Color(0xFF0F0F13)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141419)),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Original movie icon on top of the text
                val logoDrawableId = DrawableHelper.getDrawableIdByName("img_app_icon_cinema")
                Image(
                    painter = painterResource(id = logoDrawableId),
                    contentDescription = "MatMovies Logo",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.Black)
                        .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(18.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(14.dp))
                
                // Welcome to MatMovies in bold, colorful, and larger format
                Text(
                    text = "Welcome to",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 1.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "MatMovies",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFB300),
                                Color(0xFFFF5722),
                                Color(0xFFE91E63)
                            )
                        )
                    ),
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = "Sign in to continue streaming",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                if (errorMsg != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMsg ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // High Contrast Email Address Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.White.copy(alpha = 0.85f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFFFB300)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("login_email_input"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFFB300),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color(0xFFFFB300),
                        focusedContainerColor = Color(0xFF1E1E24),
                        unfocusedContainerColor = Color(0xFF1A1A20)
                    )
                )

                // High Contrast Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White.copy(alpha = 0.85f)) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password", tint = Color(0xFFFFB300)) },
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Toggle Password Visibility",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("login_password_input"),
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFFFFB300),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
                        focusedBorderColor = Color(0xFFFFB300),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                        cursorColor = Color(0xFFFFB300),
                        focusedContainerColor = Color(0xFF1E1E24),
                        unfocusedContainerColor = Color(0xFF1A1A20)
                    )
                )

                // Sign In Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank()) {
                            authViewModel.login(email.trim(), password) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Login.route) { inclusive = true }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Social Logins (Google & Facebook)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                    Text(
                        text = "Or Sign In With",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = Color.White.copy(alpha = 0.2f))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Google Button
                    Button(
                        onClick = {
                            showGoogleAuth = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "G ",
                                color = Color(0xFFEA4335),
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Google",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Facebook Button
                    Button(
                        onClick = {
                            showFacebookAuth = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "f ",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Facebook",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Standard register switcher
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New to MatMovies? ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Register Now",
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Register.route) }
                            .testTag("register_navigation_link")
                    )
                }
            }
        }

        // Render Dialog Overlays
        if (showGoogleAuth) {
            GoogleAuthDialog(
                onDismiss = { showGoogleAuth = false },
                onSuccess = { sEmail, sName ->
                    authViewModel.loginSocialUser(sEmail, sName) {
                        showGoogleAuth = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        if (showFacebookAuth) {
            FacebookAuthDialog(
                onDismiss = { showFacebookAuth = false },
                onSuccess = { fbEmail, fbName ->
                    authViewModel.loginSocialUser(fbEmail, fbName) {
                        showFacebookAuth = false
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun GoogleAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: (String, String) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var showCustomInput by remember { mutableStateOf(false) }
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google logo banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                }

                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF4285F4))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Signing in with Google...", color = Color.Gray, fontSize = 14.sp)
                } else if (!showCustomInput) {
                    Text(
                        text = "Choose an account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "to continue to MatMovies",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Profile Account 1
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLoading = true
                                onSuccess("khokonkhokon7990@gmail.com", "Khokon")
                            }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF4285F4), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("K", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Khokon", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("khokonkhokon7990@gmail.com", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    // Profile Account 2
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isLoading = true
                                onSuccess("guest.movies@gmail.com", "Guest User")
                            }
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F3F4))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF34A853), androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Guest User", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("guest.movies@gmail.com", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = { showCustomInput = true }) {
                        Text("Use another account", color = Color(0xFF4285F4), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Sign in with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Your Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF4285F4),
                            focusedLabelColor = Color(0xFF4285F4)
                        )
                    )

                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Google Email") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = Color(0xFF4285F4),
                            focusedLabelColor = Color(0xFF4285F4)
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showCustomInput = false }) {
                            Text("Back", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (customEmail.isNotBlank() && customName.isNotBlank()) {
                                    isLoading = true
                                    onSuccess(customEmail.trim(), customName.trim())
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4285F4))
                        ) {
                            Text("Continue")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FacebookAuthDialog(
    onDismiss: () -> Unit,
    onSuccess: (String, String) -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var fbEmail by remember { mutableStateOf("") }
    var fbName by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F2F5)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Facebook Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1877F2))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "facebook",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF1877F2))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Connecting to Facebook secure login...", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        Text(
                            text = "Log in with your Facebook account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333),
                            modifier = Modifier.padding(bottom = 16.dp),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = fbName,
                            onValueChange = { fbName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFF1877F2),
                                focusedLabelColor = Color(0xFF1877F2),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        OutlinedTextField(
                            value = fbEmail,
                            onValueChange = { fbEmail = it },
                            label = { Text("Mobile number or email address") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                focusedBorderColor = Color(0xFF1877F2),
                                focusedLabelColor = Color(0xFF1877F2),
                                unfocusedLabelColor = Color.Gray
                            )
                        )

                        Button(
                            onClick = {
                                if (fbEmail.isNotBlank() && fbName.isNotBlank()) {
                                    isLoading = true
                                    onSuccess(fbEmail.trim(), fbName.trim())
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Log In", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// Utility extension for helper
private fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
