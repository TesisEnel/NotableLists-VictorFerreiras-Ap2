package ucne.edu.notablelists.presentation.Notes.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.notes.usecase.*
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val getNotesUseCase: GetNotesUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val upsertNoteUseCase: UpsertNoteUseCase,
    private val postPendingNotesUseCase: PostPendingNotesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotesListState())
    val state: StateFlow<NotesListState> = _state.asStateFlow()

    private val _uiEvent = Channel<NotesListUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadNotes()
    }

    fun onEvent(event: NotesListEvent) {
        when (event) {
            is NotesListEvent.Refresh -> refresh()
            is NotesListEvent.DeleteNote -> deleteNote(event.id)
            is NotesListEvent.ToggleNoteFinished -> toggleNoteFinished(event)
            is NotesListEvent.OnAddNoteClick -> {
                sendUiEvent(NotesListUiEvent.NavigateToAddNote)
            }
            is NotesListEvent.OnNoteClick -> {
                sendUiEvent(NotesListUiEvent.NavigateToEditNote(event.id))
            }
        }
    }

    private fun loadNotes() {
        getNotesUseCase().onEach { notes ->
            _state.update { it.copy(notes = notes, isLoading = false) }
        }.launchIn(viewModelScope)
    }

    private fun deleteNote(id: String) {
        viewModelScope.launch {
            when (val result = deleteNoteUseCase(id)) {
                is Resource.Success -> _state.update { it.copy(errorMessage = null) }
                is Resource.Error -> _state.update { it.copy(errorMessage = result.message) }
                is Resource.Loading -> _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun toggleNoteFinished(event: NotesListEvent.ToggleNoteFinished) {
        viewModelScope.launch {
            upsertNoteUseCase(event.note.copy(isFinished = !event.note.isFinished))
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            val result = postPendingNotesUseCase()
            if (result is Resource.Error) {
                _state.update { it.copy(errorMessage = result.message) }
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    private fun sendUiEvent(event: NotesListUiEvent) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}