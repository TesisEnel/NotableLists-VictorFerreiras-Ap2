package ucne.edu.notablelists.presentation.add_edit_note

data class NoteEditState(
    val id: String? = null,
    val remoteId: Int? = null,
    val title: String = "",
    val description: String = "",
    val tag: String = "",
    val priority: Int = 0,
    val isFinished: Boolean = false,
    val reminder: String? = null,
    val checklist: String? = null,
    val autoDelete: Boolean = false,
    val deleteAt: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isNoteSaved: Boolean = false
)