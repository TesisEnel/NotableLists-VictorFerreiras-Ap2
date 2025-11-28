package ucne.edu.notablelists.presentation.Notes.list

import ucne.edu.notablelists.domain.notes.model.Note

sealed interface NotesListEvent {
    data object Refresh : NotesListEvent
    data class DeleteNote(val id: String) : NotesListEvent
    data class ToggleNoteFinished(val note: Note) : NotesListEvent
    data class OnNoteClick(val id: String) : NotesListEvent
    data object OnAddNoteClick : NotesListEvent
}