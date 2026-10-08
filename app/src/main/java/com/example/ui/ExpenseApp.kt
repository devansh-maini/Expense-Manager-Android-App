package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.AppDatabase
import com.example.security.SecurityManager
import com.example.ui.components.PinLockOverlay
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavItems
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.analytics.AnalyticsViewModel
import com.example.ui.screens.backup.BackupScreen
import com.example.ui.screens.backup.BackupViewModel
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.bills.BillsViewModel
import com.example.ui.screens.budgets.BudgetsScreen
import com.example.ui.screens.budgets.BudgetsViewModel
import com.example.ui.screens.calendar.CalendarScreen
import com.example.ui.screens.calendar.CalendarViewModel
import com.example.ui.screens.categories.CategoriesScreen
import com.example.ui.screens.categories.CategoriesViewModel
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.dashboard.DashboardViewModel
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.paymentmethods.PaymentMethodsScreen
import com.example.ui.screens.paymentmethods.PaymentMethodsViewModel
import com.example.ui.screens.security.SecurityScreen
import com.example.ui.screens.security.SecurityViewModel
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.screens.transactions.TransactionsViewModel

@Composable
fun ExpenseApp(
    database: AppDatabase,
    securityManager: SecurityManager,
    modifier: Modifier = Modifier
) {
    val factory = remember { AppViewModelFactory(database, securityManager) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val securityViewModel: SecurityViewModel = viewModel(factory = factory)
    val appSettings by securityViewModel.settings.collectAsStateWithLifecycle()

    var isLocked by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(appSettings) {
        val settings = appSettings
        if (settings != null && settings.isPinLockEnabled && !securityManager.isUnlocked()) {
            isLocked = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val showBottomBar = bottomNavItems.any { it.route == currentRoute }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        tonalElevation = 6.dp,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        for (item in bottomNavItems) {
                            val isSelected = currentRoute == item.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    securityManager.recordActivity()
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.title
                                    )
                                },
                                label = { Text(item.title) },
                                modifier = Modifier.testTag("nav_tab_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    val vm: DashboardViewModel = viewModel(factory = factory)
                    DashboardScreen(
                        viewModel = vm,
                        onNavigateToTransactions = {
                            navController.navigate(Screen.Transactions.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        },
                        onNavigateToBills = {
                            navController.navigate(Screen.Bills.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(Screen.Transactions.route) {
                    val vm: TransactionsViewModel = viewModel(factory = factory)
                    TransactionsScreen(viewModel = vm)
                }

                composable(Screen.Analytics.route) {
                    val vm: AnalyticsViewModel = viewModel(factory = factory)
                    AnalyticsScreen(viewModel = vm)
                }

                composable(Screen.Bills.route) {
                    val vm: BillsViewModel = viewModel(factory = factory)
                    BillsScreen(viewModel = vm)
                }

                composable(Screen.More.route) {
                    MoreScreen(
                        onNavigate = { route -> navController.navigate(route) },
                        onLockAppNow = {
                            securityManager.setUnlocked(false)
                            isLocked = true
                        }
                    )
                }

                composable(Screen.Budgets.route) {
                    val vm: BudgetsViewModel = viewModel(factory = factory)
                    BudgetsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }

                composable(Screen.Calendar.route) {
                    val vm: CalendarViewModel = viewModel(factory = factory)
                    CalendarScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }

                composable(Screen.Categories.route) {
                    val vm: CategoriesViewModel = viewModel(factory = factory)
                    CategoriesScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }

                composable(Screen.PaymentMethods.route) {
                    val vm: PaymentMethodsViewModel = viewModel(factory = factory)
                    PaymentMethodsScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }

                composable(Screen.Security.route) {
                    SecurityScreen(
                        viewModel = securityViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Screen.Backup.route) {
                    val vm: BackupViewModel = viewModel(factory = factory)
                    BackupScreen(viewModel = vm, onBack = { navController.popBackStack() })
                }
            }
        }

        // Overlay PIN Lock Screen if locked
        PinLockOverlay(
            isLocked = isLocked,
            isBiometricAvailable = appSettings?.isBiometricEnabled == true,
            onPinEntered = { enteredPin ->
                val settings = appSettings
                if (settings != null) {
                    val isValid = securityManager.verifyPin(enteredPin, settings.pinSalt, settings.pinHash)
                    if (isValid) {
                        securityManager.setUnlocked(true)
                        isLocked = false
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
            onBiometricUnlock = {
                // Biometric shortcut
                securityManager.setUnlocked(true)
                isLocked = false
            }
        )
    }
}
