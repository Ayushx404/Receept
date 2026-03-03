package com.receiptwarranty.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.receiptwarranty.app.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Spacing.lg)
        ) {
            item {
                PrivacySection(
                    title = "Local-First Architecture",
                    content = "Your data is stored in a secure local database on your device. We don't use central servers or cloud harvesting, ensuring your records remain private by default."
                )
                PrivacySection(
                    title = "Private Cloud Infrastructure",
                    content = "When you enable Cloud Sync, it uses your personal Google Drive account. We use restricted scopes (App Data), meaning Vault can ONLY access the files it creates and cannot touch your personal photos or other documents."
                )
                PrivacySection(
                    title = "Zero Third-Party Trackers",
                    content = "The app contains no analytics, no advertisements, and no third-party tracking libraries. Your usage patterns, receipt details, and financial habits are your own."
                )
                PrivacySection(
                    title = "Data Ownership & Portability",
                    content = "Take your data anywhere. Use the 'Export to CSV' feature in Settings to download a local backup of all your records at any time. You are the sole owner of your information."
                )
                PrivacySection(
                    title = "Permission Transparency",
                    content = "We only ask for the minimum permissions needed to function: Camera for scanning, Storage for attaching images, and Google Drive for optional backups. We never track your location or background activity."
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String) {
    Column(modifier = Modifier.padding(bottom = Spacing.xl)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = Spacing.xs)
        )
    }
}
