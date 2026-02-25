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

// --- THEME COLORS ---
val MidnightNavy = Color(0xFF181A2F)
val DeepOcean = Color(0xFF242E49)
val SlateBlue = Color(0xFF37415C)
val SunsetCoral = Color(0xFFFDA481)
val CrimsonRed = Color(0xFFB4182D)
// val DeepMaroon = Color(0xFF54162B)
// val SoftSand = Color(0xFFDFB6B2)

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
            .background(brush = Brush.verticalGradient(colors = listOf(DeepOcean, SlateBlue)))
            .drawBehind {
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
            modifier = Modifier.width(420.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            backgroundColor = MidnightNavy,
            elevation = 20.dp,
            border = BorderStroke(1.dp, Tan.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Lock, null, tint = Tan, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(16.dp))
                Text("CORE ACCESS", style = MaterialTheme.typography.h4, fontWeight = FontWeight.ExtraBold, color = Tan, letterSpacing = 4.sp)
                Text("IDENTIFICATION REQUIRED", style = MaterialTheme.typography.caption, color = Color.Gray, letterSpacing = 1.sp)
                Spacer(Modifier.height(40.dp))

                FuturisticTextField(value = email, onValueChange = { email = it }, label = "OPERATOR ID (EMAIL)")
                Spacer(Modifier.height(16.dp))
                FuturisticTextField(value = password, onValueChange = { password = it }, label = "ACCESS KEY", isPassword = true)

                if (errorMessage != null) {
                    Text(errorMessage!!, color = CrimsonRed, modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.caption)
                }

                Spacer(Modifier.height(32.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = stayLoggedIn,
                        onCheckedChange = { stayLoggedIn = it },
                        colors = CheckboxDefaults.colors(checkedColor = Tan, uncheckedColor = Tan.copy(alpha = 0.5f), checkmarkColor = OxfordBlue)
                    )
                    Text("STAY LOGGED IN", color = Tan.copy(alpha = 0.8f), style = MaterialTheme.typography.caption, modifier = Modifier.padding(start = 8.dp))
                }

                Button(
                    onClick = {
                        isLoading = true
                        // FIX: Ensure DatabaseManager.verifyLogin is visible and accessible
                        val authenticatedUser = DatabaseManager.verifyLogin(email, password)

                        if (authenticatedUser != null) {
                            if (stayLoggedIn) { AuthManager.saveUser(authenticatedUser.id) }
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
                TextButton(onClick = onNavigateToSignUp) {
                    Text("NEW OPERATOR? REGISTER PROFILE", color = Tan.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun FuturisticTextField(value: String, onValueChange: (String) -> Unit, label: String, isPassword: Boolean = false) {
    val Tan = Color(0xFFFDA481)
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Tan.copy(alpha = 0.5f), fontSize = 12.sp) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = Tan, unfocusedBorderColor = Tan.copy(alpha = 0.2f), textColor = Color.White),
        visualTransformation = if (isPassword && !passwordVisible) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = Tan.copy(alpha = 0.6f))
                }
            }
        }
    )
}

@Composable
fun AuthView(onLoginSuccess: (User) -> Unit) {
    var showSignUp by remember { mutableStateOf(false) }

    if (showSignUp) {
        SignUpView(
            onSignUpSuccess = { user ->
                if (user != null) {
                    // User signed up with STAY LOGGED IN - log them in
                    showSignUp = false
                    onLoginSuccess(user)
                } else {
                    // User signed up without STAY LOGGED IN - go back to login
                    showSignUp = false
                }
            },
            onNavigateToLogin = { showSignUp = false }
        )
    } else {
        LoginView(
            onAuthSuccess = { user -> onLoginSuccess(user) },
            onNavigateToSignUp = { showSignUp = true }
        )
    }
}