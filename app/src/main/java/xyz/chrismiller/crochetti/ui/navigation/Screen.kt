package xyz.chrismiller.crochetti.ui.navigation

sealed class Screen(val route: String) {
    data object PatternList : Screen("patterns")
    data object PatternCreate : Screen("patterns/create")

    data object PatternEdit : Screen("patterns/{patternId}/edit") {
        fun createRoute(patternId: Long) = "patterns/$patternId/edit"
    }

    data object ProjectList : Screen("patterns/{patternId}/projects") {
        fun createRoute(patternId: Long) = "patterns/$patternId/projects"
    }

    data object ProjectProgress : Screen("projects/{projectId}/progress") {
        fun createRoute(projectId: Long) = "projects/$projectId/progress"
    }

    data object QrScanner : Screen("qr_scanner")

    companion object {
        const val PATTERN_ID_ARG = "patternId"
        const val PROJECT_ID_ARG = "projectId"
    }
}
