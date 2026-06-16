package com.example.tarik

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.tarik.ui.ArchiveScreen
import com.example.tarik.ui.DetailScreen
import com.example.tarik.ui.HistoryViewModel
import com.example.tarik.ui.SettingsDrawerContent
import com.example.tarik.ui.SettingsViewModel
import com.example.tarik.ui.TodayScreen
import com.example.tarik.ui.theme.TarikTheme
import kotlinx.coroutines.launch


private data class BottomTab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val BOTTOM_TABS = listOf(
    BottomTab("home", "Today", Icons.Filled.DateRange),
    BottomTab("archive", "Archive", Icons.Filled.Bookmark)
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val darkMode by settingsViewModel.darkMode.collectAsState()

            // dark mode reacts live to the DataStore flow hence flipping the toggle in the drawer
            // immediately rebuilds the theme without restart
            TarikTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val historyViewModel: HistoryViewModel = viewModel()
                // the viewmodel is scoped to the Activity so it survives rotation
                // this is the same instance that gets passed to every screen
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                // setting up the permission request
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (!granted) {
                        Toast.makeText(
                            this,
                            "Notifications disabled - you can enable them later in system settings",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                // request the permission once on first launch
                LaunchedEffect(Unit) {
                    requestNotificationPermissionIfNeeded(notificationPermissionLauncher::launch)
                }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet {
                            SettingsDrawerContent(settingsViewModel = settingsViewModel)
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text("Tarik") },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(Icons.Filled.Menu, contentDescription = "Open menu")
                                    }
                                }
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDestination = navBackStackEntry?.destination

                                BOTTOM_TABS.forEach { tab ->
                                    NavigationBarItem(
                                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                                        label = { Text(tab.label) },
                                        selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true,
                                        onClick = {
                                            // popUpTo prevents stacking duplicate destinations
                                            navController.navigate(tab.route) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        NavHost(
                            navController = navController,
                            startDestination = "home",
                            modifier = Modifier.padding(innerPadding)
                        ) {
                            composable("home") {
                                TodayScreen(
                                    viewModel = historyViewModel,
                                    onCardClick = { id -> navController.navigate("details/$id") }
                                )
                            }

                            composable("archive") {
                                ArchiveScreen(
                                    viewModel = historyViewModel,
                                    onCardClick = { id -> navController.navigate("details/$id") }
                                )
                            }

                            composable("details/{itemId}") { backStackEntry ->
                                val id = backStackEntry.arguments?.getString("itemId")?.toIntOrNull()
                                if (id != null) {
                                    DetailScreen(
                                        itemId = id,
                                        viewModel = historyViewModel,
                                        onBackClick = { navController.popBackStack() },
                                        onSeeFullArticleClick = { url -> openWikipediaArticle(url) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded(launchRequest: (String) -> Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            launchRequest(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // this functions allows the user to go further in the search if the details of the article interested them.
    private fun openWikipediaArticle(url: String) {
        // since we treat external data including api's as untrusted we first include this if statement in an attempt to validate the url
        if (!url.startsWith("https://") || !url.contains("wikipedia.org")) {
            Toast.makeText(this, "Invalid article URL", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        try {
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No browser available", Toast.LENGTH_SHORT).show()
            }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Cannot open article", Toast.LENGTH_SHORT).show()
        }
    }
}
