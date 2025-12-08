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
import xyz.chrismiller.crochetti.ui.screen.projectlist.ProjectListScreen
import xyz.chrismiller.crochetti.ui.screen.projectprogress.ProjectProgressScreen

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
                onPatternClick = { patternId, projectCount ->
                    // If multiple projects exist, go to project list; otherwise handled by auto-create
                    navController.navigate(Screen.ProjectList.createRoute(patternId))
                },
                onCreateClick = {
                    navController.navigate(Screen.PatternCreate.route)
                },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.ProjectProgress.createRoute(projectId))
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
                    navController.navigate(Screen.ProjectList.createRoute(patternId))
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

        // Project List for a Pattern
        composable(
            route = Screen.ProjectList.route,
            arguments = listOf(
                navArgument(Screen.PATTERN_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val patternId = backStackEntry.arguments?.getLong(Screen.PATTERN_ID_ARG) ?: return@composable
            ProjectListScreen(
                onNavigateBack = { navController.popBackStack() },
                onProjectClick = { projectId ->
                    navController.navigate(Screen.ProjectProgress.createRoute(projectId))
                },
                onEditPatternClick = {
                    navController.navigate(Screen.PatternEdit.createRoute(patternId))
                }
            )
        }

        // Project Progress (Counter)
        composable(
            route = Screen.ProjectProgress.route,
            arguments = listOf(
                navArgument(Screen.PROJECT_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong(Screen.PROJECT_ID_ARG) ?: return@composable
            ProjectProgressScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { patternId ->
                    navController.navigate(Screen.PatternEdit.createRoute(patternId))
                }
            )
        }
    }
}
