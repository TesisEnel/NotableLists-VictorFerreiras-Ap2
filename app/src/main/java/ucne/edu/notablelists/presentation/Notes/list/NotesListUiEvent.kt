package ucne.edu.notablelists.presentation.Notes.list

sealed interface NotesListUiEvent {
    data object NavigateToAddNote : NotesListUiEvent
    data class NavigateToEditNote(val id: String) : NotesListUiEvent
}