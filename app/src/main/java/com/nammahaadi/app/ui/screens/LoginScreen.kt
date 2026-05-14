package com.nammahaadi.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nammahaadi.app.R
import com.nammahaadi.app.ui.theme.Surface
import com.nammahaadi.app.viewmodel.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun LoginScreen(viewModel: AppViewModel, onLoginSuccess: () -> Unit) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var step by remember { mutableStateOf("phone") } // "phone", "otp", "name"
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf(List(6) { "" }) }
    var name by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val focusRequesters = remember { List(6) { FocusRequester() } }

    val isPhoneValid = phone.length == 10 && phone.all { it.isDigit() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Surface, Color(0xFFFDFCFB)))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .widthIn(max = 400.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(visible = step != "name") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(18.dp))
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Namma Haadi", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text("Testing Mode Enabled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            
            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    AnimatedContent(targetState = step, label = "login_steps") { currentStep ->
                        when (currentStep) {
                            "phone" -> {
                                PhoneStep(
                                    phone = phone,
                                    onPhoneChange = { if (it.length <= 10) phone = it },
                                    isPhoneValid = isPhoneValid,
                                    loading = loading,
                                    error = error,
                                    onSendOtp = {
                                        keyboardController?.hide()
                                        loading = true
                                        scope.launch {
                                            delay(1200) // Simulate network
                                            loading = false
                                            step = "otp"
                                        }
                                    }
                                )
                            }
                            "otp" -> {
                                OtpStep(
                                    phone = phone,
                                    otp = otp,
                                    onOtpChange = { otp = it },
                                    focusRequesters = focusRequesters,
                                    loading = loading,
                                    error = error,
                                    onBack = { step = "phone"; error = "" },
                                    onVerify = {
                                        keyboardController?.hide()
                                        val code = otp.joinToString("")
                                        if (code == "123456") {
                                            loading = true
                                            scope.launch {
                                                val mockUid = "user_$phone"
                                                // Check if user exists in Firestore
                                                when (val res = viewModel.firestoreRepo.getUser(mockUid)) {
                                                    is com.nammahaadi.app.data.repository.Result.Success -> {
                                                        viewModel.onLoginSuccess(mockUid, res.data.name, "+91$phone")
                                                        onLoginSuccess()
                                                    }
                                                    else -> {
                                                        step = "name"
                                                        loading = false
                                                    }
                                                }
                                            }
                                        } else {
                                            error = "Invalid OTP. Try 123456"
                                        }
                                    }
                                )
                            }
                            "name" -> {
                                NameStep(
                                    name = name,
                                    onNameChange = { name = it },
                                    loading = loading,
                                    onComplete = {
                                        loading = true
                                        scope.launch {
                                            viewModel.onLoginSuccess("user_$phone", name, "+91$phone")
                                            onLoginSuccess()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            if (step == "otp") {
                Text(
                    "Testing Tip: Use 123456 as the OTP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
fun PhoneStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    isPhoneValid: Boolean,
    loading: Boolean,
    error: String,
    onSendOtp: () -> Unit
) {
    Column {
        Text("Sign In", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Enter any 10-digit number to test", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.all { c -> c.isDigit() }) onPhoneChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Phone Number") },
            placeholder = { Text("98765 43210") },
            leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(20.dp)) },
            prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(onGo = { if (isPhoneValid) onSendOtp() }),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onSendOtp,
            enabled = isPhoneValid && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Get Testing OTP", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ChevronRight, null)
            }
        }
    }
}

@Composable
fun OtpStep(
    phone: String,
    otp: List<String>,
    onOtpChange: (List<String>) -> Unit,
    focusRequesters: List<FocusRequester>,
    loading: Boolean,
    error: String,
    onBack: () -> Unit,
    onVerify: () -> Unit
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.Default.ArrowBack, null)
            }
            Text("Verify OTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        Text("Testing for +91 $phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(24.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            otp.forEachIndexed { i, digit ->
                OutlinedTextField(
                    value = digit,
                    onValueChange = { v ->
                        val clean = v.filter { it.isDigit() }
                        if (clean.length >= 1) {
                            val newOtp = otp.toMutableList()
                            newOtp[i] = clean.takeLast(1)
                            onOtpChange(newOtp)
                            if (i < 5) focusRequesters[i + 1].requestFocus()
                        } else {
                            val newOtp = otp.toMutableList()
                            newOtp[i] = ""
                            onOtpChange(newOtp)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .focusRequester(focusRequesters[i]),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (i == 5) ImeAction.Done else ImeAction.Next),
                    keyboardActions = KeyboardActions(onDone = { if (otp.all { it.isNotEmpty() }) onVerify() }),
                    textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
        
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onVerify,
            enabled = otp.all { it.isNotEmpty() } && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Verify & Continue", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun NameStep(
    name: String,
    onNameChange: (String) -> Unit,
    loading: Boolean,
    onComplete: () -> Unit
) {
    Column {
        Text("Profile Setup", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("What's your name?", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Full Name") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onComplete() })
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onComplete,
            enabled = name.isNotBlank() && !loading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            else Text("Get Started", style = MaterialTheme.typography.titleMedium)
        }
    }
}
