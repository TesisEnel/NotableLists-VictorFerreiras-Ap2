package ucne.edu.notablelists.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Profile : Screen("profile")
    data object Notes : Screen("notes")
    data object SharedNotes : Screen("shared_notes")

    data object NoteEdit : Screen("note_edit?noteId={noteId}") {
        fun passId(id: String? = null): String {
            return "note_edit?noteId=${id ?: ""}"
        }
    }
}