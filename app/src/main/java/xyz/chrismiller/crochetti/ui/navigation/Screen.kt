package xyz.chrismiller.crochetti.ui.navigation

sealed class Screen(val route: String) {
    data object PatternList : Screen("patterns")
    data object PatternCreate : Screen("patterns/create")

    data object PatternEdit : Screen("patterns/{patternId}/edit") {
        fun createRoute(patternId: Long) = "patterns/$patternId/edit"
    }

    data object PatternProgress : Screen("patterns/{patternId}/progress") {
        fun createRoute(patternId: Long) = "patterns/$patternId/progress"
    }

    companion object {
        const val PATTERN_ID_ARG = "patternId"
    }
}
