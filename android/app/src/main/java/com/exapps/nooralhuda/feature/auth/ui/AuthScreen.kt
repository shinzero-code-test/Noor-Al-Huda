package com.exapps.nooralhuda.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exapps.nooralhuda.R
import com.exapps.nooralhuda.core.ui.components.NoorCard
import com.exapps.nooralhuda.core.ui.components.NoorGhostButton
import com.exapps.nooralhuda.core.ui.components.NoorPrimaryButton

/** Email/password + guest + Google + reset + passwordless link. */
@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    compact: Boolean = false,
    // Nav-entry scope. Email links arrive via DeepLinkBus, so no Activity cast
    // (a dialog context is not an Activity — that cast crashed on launch).
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var mode by rememberSaveable { mutableStateOf(AuthMode.SignIn) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            if (event is AuthEvent.SignedIn) onSignedIn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!compact) {
            Text(
                text = stringResource(R.string.auth_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        NoorCard {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.auth_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.auth_password)) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (mode == AuthMode.SignIn) {
                    NoorPrimaryButton(
                        onClick = { viewModel.signIn(email, password) },
                        label = stringResource(R.string.auth_sign_in),
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    NoorPrimaryButton(
                        onClick = { viewModel.register(email, password) },
                        label = stringResource(R.string.auth_register),
                        enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                TextButton(onClick = {
                    mode = if (mode == AuthMode.SignIn) AuthMode.Register else AuthMode.SignIn
                }) {
                    Text(
                        stringResource(
                            if (mode == AuthMode.SignIn) R.string.auth_go_register
                            else R.string.auth_go_sign_in
                        )
                    )
                }
            }
        }
        state.errorRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
        state.infoRes?.let {
            Text(
                text = stringResource(it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(4.dp))
        if (viewModel.googleAvailable) {
            // LocalContext passed through, never cast (dialog-safe by design).
            val context = LocalContext.current
            OutlinedButton(
                onClick = { viewModel.signInWithGoogle(context) },
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.auth_continue_google))
            }
        }
        NoorGhostButton(
            onClick = { viewModel.continueAsGuest() },
            label = stringResource(R.string.auth_guest),
            modifier = Modifier.fillMaxWidth()
        )
        NoorGhostButton(
            onClick = { viewModel.sendLink(email) },
            label = stringResource(R.string.auth_send_link),
            modifier = Modifier.fillMaxWidth()
        )
        NoorGhostButton(
            onClick = { viewModel.sendReset(email) },
            label = stringResource(R.string.auth_forgot),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private enum class AuthMode { SignIn, Register }
