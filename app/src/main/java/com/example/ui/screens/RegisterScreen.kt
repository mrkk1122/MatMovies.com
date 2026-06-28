package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val errorMsg by authViewModel.registerError.collectAsState()
    val registerSuccess by authViewModel.registerSuccess.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.clearErrors()
    }

    LaunchedEffect(registerSuccess) {
        if (registerSuccess) {
            // Auto login or return to login screen
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Register.route) { inclusive = true }
            }
        }
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
                // Beautiful Logo with Gold Gradient
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFFB300),
                                    Color(0xFFE50914)
                                )
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AppRegistration,
                        contentDescription = "Register",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                AnimatedAppName(fontSize = 32.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign up to start your cinematic journey",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 24.dp)
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

                // High Contrast Username Field
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username", color = Color.White.copy(alpha = 0.85f)) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = Color(0xFFFFB300)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("register_username_input"),
                    shape = RoundedCornerShape(12.dp),
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

                // High Contrast Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address", color = Color.White.copy(alpha = 0.85f)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email", tint = Color(0xFFFFB300)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("register_email_input"),
                    shape = RoundedCornerShape(12.dp),
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
                                contentDescription = "Toggle Visibility",
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .testTag("register_password_input"),
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

                // Register Button
                Button(
                    onClick = {
                        if (email.isNotBlank() && password.isNotBlank() && username.isNotBlank()) {
                            authViewModel.register(email.trim(), password, username.trim())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("register_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFB300),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "Register",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign In",
                        color = Color(0xFFFFB300),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { navController.navigate(Screen.Login.route) }
                            .testTag("login_navigation_link")
                    )
                }
            }
        }
    }
}
