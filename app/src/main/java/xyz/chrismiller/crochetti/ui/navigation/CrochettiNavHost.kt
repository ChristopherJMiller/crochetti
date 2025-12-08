package xyz.chrismiller.crochetti.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import xyz.chrismiller.crochetti.ui.screen.patternedit.PatternEditScreen
import xyz.chrismiller.crochetti.ui.screen.patternlist.PatternListScreen
import xyz.chrismiller.crochetti.ui.screen.patternprogress.PatternProgressScreen

@Composable
fun CrochettiNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.PatternList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Pattern List (Home)
        composable(Screen.PatternList.route) {
            PatternListScreen(
                onPatternClick = { patternId ->
                    navController.navigate(Screen.PatternProgress.createRoute(patternId))
                },
                onCreateClick = {
                    navController.navigate(Screen.PatternCreate.route)
                }
            )
        }

        // Create Pattern
        composable(Screen.PatternCreate.route) {
            PatternEditScreen(
                patternId = null,
                onNavigateBack = { navController.popBackStack() },
                onPatternSaved = { patternId ->
                    navController.popBackStack()
                    navController.navigate(Screen.PatternProgress.createRoute(patternId))
                }
            )
        }

        // Edit Pattern
        composable(
            route = Screen.PatternEdit.route,
            arguments = listOf(
                navArgument(Screen.PATTERN_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getLong(Screen.PATTERN_ID_ARG) ?: return@composable
            PatternEditScreen(
                patternId = patternId,
                onNavigateBack = { navController.popBackStack() },
                onPatternSaved = { navController.popBackStack() }
            )
        }

        // Pattern Progress (Counter)
        composable(
            route = Screen.PatternProgress.route,
            arguments = listOf(
                navArgument(Screen.PATTERN_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getLong(Screen.PATTERN_ID_ARG) ?: return@composable
            PatternProgressScreen(
                patternId = patternId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = {
                    navController.navigate(Screen.PatternEdit.createRoute(patternId))
                }
            )
        }
    }
}
