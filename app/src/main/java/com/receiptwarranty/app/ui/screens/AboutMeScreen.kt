package com.receiptwarranty.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.receiptwarranty.app.ui.theme.Spacing
import com.receiptwarranty.app.ui.theme.VaultShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutMeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val githubUrl = "https://github.com/Ayushx404"
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("About Me", fontWeight = FontWeight.Bold) },
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
            contentPadding = PaddingValues(Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Profile Section
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(Spacing.lg))

                Text(
                    text = "Ayush Patil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Android Developer",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(Spacing.xl))

                // Bio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text(
                            text = "Project Story",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.xs))
                        Text(
                            text = "I'm a final-year BSc.IT student, and I built this app as my college project. Like many people, I was tired of losing receipts and forgetting about warranties until it was too late.\n" +
                                    "\n" +
                                    "Instead of storing your data on some random server, everything saves directly to your Google Drive so your receipts stay yours. I made this mainly for myself, but figured others might find it useful too.\n" +
                                    "\n" +
                                    "Simple, private, and (hopefully) helpful.\n",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                // Tech Stack
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.large,
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Text(
                            text = "Tech Stack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spacing.md))
                        
                        TechTag(name = "Jetpack Compose")
                        TechTag(name = "Kotlin Coroutines & Flow")
                        TechTag(name = "Room Database")
                        TechTag(name = "Firebase Authentication")
                        TechTag(name = "Material 3 Design")
                    }
                }

                Spacer(Modifier.height(Spacing.lg))

                // Social Link
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, githubUrl.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = VaultShape.medium
                ) {
                    Icon(Icons.Default.Code, contentDescription = null)
                    Spacer(Modifier.width(Spacing.sm))
                    Text("Visit my GitHub")
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                }
                
                Spacer(Modifier.height(Spacing.xxl))

            }
        }
    }
}

@Composable
private fun TechTag(name: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(Modifier.width(Spacing.sm))
        Text(text = name, style = MaterialTheme.typography.bodyMedium)
    }
}
