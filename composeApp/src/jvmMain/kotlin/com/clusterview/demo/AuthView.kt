package com.clusterview.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

import androidx.compose.foundation.BorderStroke

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush


val MidnightNavy = Color(0xFF181A2F)
val DeepOcean = Color(0xFF242E49)
val SlateBlue = Color(0xFF37415C)
val SunsetCoral = Color(0xFFFDA481)
val CrimsonRed = Color(0xFFB4182D)
val DeepMaroon = Color(0xFF54162B)
val SoftSand = Color(0xFFDFB6B2)

@Composable
fun LoginView(
    onAuthSuccess: (User) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var stayLoggedIn by remember { mutableStateOf(false) }

    val OxfordBlue = Color(0xFF181A2F)
    val Tan = Color(0xFFFDA481)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF242E49), Color(0xFF37415C)) // Deep gradient
                )
            )
            .drawBehind {
                // Draw a subtle tech-grid
                val gridSize = 40.dp.toPx()
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(Color.White.copy(alpha = 0.05f), start = Offset(x.toFloat(), 0f), end = Offset(x.toFloat(), size.height))
                }
                for (y in 0..size.height.toInt() step gridSize.toInt()) {
                    drawLine(Color.White.copy(alpha = 0.05f), start = Offset(0f, y.toFloat()), end = Offset(size.width, y.toFloat()))
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(420.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFF181A2F), // Surface color
            elevation = 20.dp, // High elevation for a "floating" look
            border = BorderStroke(1.dp, Color(0xFFFDA481).copy(alpha = 0.3f)) // The "Glowing" border
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Section
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Tan,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "CORE ACCESS",
                    style = MaterialTheme.typography.h4,
                    fontWeight = FontWeight.ExtraBold,
                    color = Tan,
                    letterSpacing = 4.sp
                )
                Text(
                    "IDENTIFICATION REQUIRED",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(40.dp))

                // Input Fields
                FuturisticTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "OPERATOR ID (EMAIL)"
                )
                Spacer(Modifier.height(16.dp))
                FuturisticTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "ACCESS KEY",
                    isPassword = true
                )

                if (errorMessage != null) {
                    Text(
                        errorMessage!!,
                        color = Color(0xFF54162B),
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.caption
                    )
                }

                Spacer(Modifier.height(32.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = stayLoggedIn,
                        onCheckedChange = { stayLoggedIn = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Tan,
                            uncheckedColor = Tan.copy(alpha = 0.5f),
                            checkmarkColor = OxfordBlue
                        )
                    )
                    Text(
                        text = "STAY LOGGED IN",
                        color = Tan.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(start = 8.dp),
                        letterSpacing = 1.sp
                    )
                }

                // Action Button
                Button(
                    onClick = {
                        /*isLoading = true
                        val user = AuthManager.login(email, password, false)
                        if (user != null) {
                            onAuthSuccess(user)
                        } else {
                            errorMessage = "PROTOCOL ERROR: INVALID CREDENTIALS"
                            isLoading = false
                        }*/

                        /*val authenticatedUser = DatabaseManager.verifyLogin(email, password)
                        if (authenticatedUser != null) {
                            onAuthSuccess(authenticatedUser)
                        } else {
                            errorMessage = "ACCESS DENIED: UNKNOWN OPERATOR"
                            isLoading = false
                        }*/

                        isLoading = true
                        // We pass 'stayLoggedIn' as the 'remember' parameter
                        val authenticatedUser = AuthManager.login(email, password, stayLoggedIn)

                        if (authenticatedUser != null) {
                            onAuthSuccess(authenticatedUser)
                        } else {
                            errorMessage = "ACCESS DENIED: INVALID CREDENTIALS"
                            isLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Tan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = OxfordBlue, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    } else {
                        Text("INITIALIZE ACCESS", fontWeight = FontWeight.Bold, color = OxfordBlue)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Navigation to Sign Up
                TextButton(onClick = onNavigateToSignUp) {
                    Text("NEW OPERATOR? REGISTER PROFILE", color = Tan.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FuturisticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false
) {
    val Tan = Color(0xFFFDA481)
    // State to track if password should be shown or hidden
    var passwordVisible by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Tan.copy(alpha = 0.5f), fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = Tan,
            unfocusedBorderColor = Tan.copy(alpha = 0.2f),
            textColor = Color.White,
            cursorColor = Tan
        ),
        // Logic to switch between hidden stars and plain text
        visualTransformation = if (isPassword && !passwordVisible)
            androidx.compose.ui.text.input.PasswordVisualTransformation()
        else androidx.compose.ui.text.input.VisualTransformation.None,

        // The Eye Icon Button
        trailingIcon = {
            if (isPassword) {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null, tint = Tan.copy(alpha = 0.6f))
                }
            }
        }
    )
}

@Composable
fun AuthView(onLoginSuccess: () -> Unit) {
    var showSignUp by remember { mutableStateOf(false) }

    if (showSignUp) {
        SignUpView(
            onSignUpSuccess = { onLoginSuccess() },
            onNavigateToLogin = { showSignUp = false }
        )
    } else {
        LoginView(
            onAuthSuccess = { onLoginSuccess() },
            onNavigateToSignUp = { showSignUp = true }
        )
    }
}

