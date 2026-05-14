package com.nammahaadi.app.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.nammahaadi.app.ui.screens.*
import com.nammahaadi.app.viewmodel.AppViewModel

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Login : Screen("login", "Login", Icons.Default.Lock)
    object Home : Screen("home", "Issues", Icons.Default.Warning)
    object Map : Screen("map", "Map", Icons.Default.Map)
    object Report : Screen("report", "Report", Icons.Default.AddCircle)
    object Alerts : Screen("alerts", "Alerts", Icons.Default.NotificationsActive)
    object Leaderboard : Screen("leaderboard", "Rank", Icons.Default.EmojiEvents)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

val bottomNavItems = listOf(
    Screen.Home, Screen.Map, Screen.Report, Screen.Alerts, Screen.Leaderboard, Screen.Profile
)

@Composable
fun AppNavHost(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val viewModel: AppViewModel = hiltViewModel()
    val isWideScreen = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val showNav = currentDestination?.route != Screen.Login.route

    if (isWideScreen && showNav) {
        // Tablet: NavigationRail on the left
        Row(modifier = Modifier.fillMaxSize()) {
            NavigationRail(
                modifier = Modifier.fillMaxHeight(),
                containerColor = MaterialTheme.colorScheme.surface,
                header = {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "NH",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    NavigationRailItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
            MainNavContent(navController = navController, viewModel = viewModel)
        }
    } else {
        // Phone: BottomNavigationBar
        Scaffold(
            bottomBar = {
                if (showNav) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label, modifier = Modifier.size(22.dp)) },
                                label = { Text(screen.label, style = MaterialTheme.typography.labelSmall) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                MainNavContent(navController = navController, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainNavContent(
    navController: androidx.navigation.NavHostController,
    viewModel: AppViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()

    LaunchedEffect(currentUser) {
        if (currentUser != null && navController.currentDestination?.route == Screen.Login.route) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.Login.route) {
            LoginScreen(viewModel = viewModel, onLoginSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Home.route) {
            HomeScreen(viewModel = viewModel, onViewOnMap = { reportId ->
                navController.navigate("${Screen.Map.route}?report=$reportId")
            })
        }
        composable("${Screen.Map.route}?report={reportId}") { backStack ->
            val reportId = backStack.arguments?.getString("reportId")
            MapScreen(viewModel = viewModel, selectedReportId = reportId)
        }
        composable(Screen.Map.route) {
            MapScreen(viewModel = viewModel, selectedReportId = null)
        }
        composable(Screen.Report.route) {
            ReportScreen(viewModel = viewModel, onSuccess = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            })
        }
        composable(Screen.Alerts.route) {
            AlertsScreen(viewModel = viewModel)
        }
        composable(Screen.Leaderboard.route) {
            LeaderboardScreen(viewModel = viewModel)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(viewModel = viewModel, onLogout = {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
    }
}
