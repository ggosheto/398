package com.clusterview.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignUpView(onSignUpSuccess: () -> Unit, onNavigateToLogin: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val OxfordBlue = Color(0, 33, 71)
    val Tan = Color(210, 180, 140)

    Box(modifier = Modifier.fillMaxSize().background(OxfordBlue), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.width(450.dp).padding(16.dp),
            shape = RoundedCornerShape(24.dp), // More rounded for modern look
            backgroundColor = Color(10, 43, 81), // Slightly lighter blue for depth
            elevation = 12.dp
        ) {
            Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("CREATE ACCOUNT", color = Tan, style = MaterialTheme.typography.h4, fontWeight = FontWeight.ExtraBold)
                Text("Join the ClusterView ecosystem", color = Color.Gray, style = MaterialTheme.typography.caption)

                Spacer(Modifier.height(32.dp))

                // Modern Outlined Text Fields with Tan accents
                FuturisticTextField(value = email, onValueChange = { email = it }, label = "Email Address")
                Spacer(Modifier.height(16.dp))
                // Field 1: Password
                FuturisticTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "PASSWORD (SECURITY KEY)", // Clarification in the label
                    isPassword = true
                )

                // The Requirement Checklist we built earlier stays here
                Spacer(Modifier.height(8.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    RequirementText("Min. 6 Characters", password.length >= 6)
                    RequirementText("Upper & Lower Case", password.any { it.isUpperCase() } && password.any { it.isLowerCase() })
                    RequirementText("At least one number", password.any { it.isDigit() })
                }

                Spacer(Modifier.height(16.dp))

                // Field 2: Confirm Password
                FuturisticTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "CONFIRM PASSWORD (SECURITY KEY)",
                    isPassword = true
                )
                if (validationError != null) {
                    Text(
                        text = validationError!!,
                        color = Color(255, 80, 80), // Tech-red
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        val (isValid, errorMsg) = isPasswordValid(password)

                        when {
                            email.isBlank() -> validationError = "OPERATOR EMAIL REQUIRED"
                            !isValid -> validationError = errorMsg
                            password != confirmPassword -> validationError = "SECURITY KEYS DO NOT MATCH"
                            else -> {
                                val dbError = DatabaseManager.registerUser(email, password)
                                if (dbError == null) {
                                    onSignUpSuccess()
                                } else {
                                    validationError = dbError
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Tan),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("REGISTER", color = OxfordBlue, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                }

                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Sign In", color = Tan.copy(alpha = 0.7f))
                }
            }
        }
    }
}

private fun DatabaseManager.registerUser(email: String, password: String) {}

fun isPasswordValid(password: String): Pair<Boolean, String?> {
    return when {
        password.length < 6 -> false to "PASSWORD MUST BE AT LEAST 6 CHARACTERS"
        password.none { it.isUpperCase() } -> false to "MISSING UPPERCASE CHARACTER"
        password.none { it.isLowerCase() } -> false to "MISSING LOWERCASE CHARACTER"
        password.none { it.isDigit() } -> false to "MISSING AT LEAST ONE NUMBER"
        else -> true to null
    }
}

@Composable
fun RequirementText(text: String, isMet: Boolean) {
    val Tan = Color(210, 180, 140)
    Text(
        text = if (isMet) "✓ $text" else "○ $text",
        color = if (isMet) Tan else Color.Gray.copy(alpha = 0.5f),
        style = MaterialTheme.typography.caption,
        modifier = Modifier.padding(top = 2.dp)
    )
}
