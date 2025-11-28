package ucne.edu.notablelists.presentation.Notes.edit

sealed interface NoteEditEvent {
    data class EnteredTitle(val value: String) : NoteEditEvent
    data class EnteredDescription(val value: String) : NoteEditEvent
    data class EnteredTag(val value: String) : NoteEditEvent
    data class ChangePriority(val value: Int) : NoteEditEvent
    data class ChangeReminder(val value: String?) : NoteEditEvent
    data class ChangeChecklist(val value: String?) : NoteEditEvent
    data class ToggleAutoDelete(val isEnabled: Boolean) : NoteEditEvent
    data class ToggleFinished(val isFinished: Boolean) : NoteEditEvent
    data object SaveNote : NoteEditEvent
    data object DeleteNote : NoteEditEvent
    data object OnBackClick : NoteEditEvent
}