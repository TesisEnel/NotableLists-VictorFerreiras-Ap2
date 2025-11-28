package ucne.edu.notablelists.presentation.Notes.list

import ucne.edu.notablelists.domain.notes.model.Note

data class NotesListState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false
)