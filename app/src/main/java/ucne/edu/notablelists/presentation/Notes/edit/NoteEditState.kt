package ucne.edu.notablelists.presentation.Notes.edit

import ucne.edu.notablelists.domain.friends.model.Friend

data class NoteEditState(
    val id: String? = null,
    val remoteId: Int? = null,
    val title: String = "",
    val description: String = "",
    val tag: String = "",
    val priority: Int = 0,
    val isFinished: Boolean = false,
    val reminder: String? = null,
    val checklist: List<ChecklistItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isNoteSaved: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isTagSheetOpen: Boolean = false,
    val availableTags: List<String> = listOf("Personal", "Trabajo", "Estudio", "Ideas", "Urgente"),
    val isOwner: Boolean = true,
    val noteOwnerId: Int? = null,
    val currentUserId: Int? = null,
    val showLoginRequiredDialog: Boolean = false,
    val showNoFriendsDialog: Boolean = false,
    val showShareSheet: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val collaborators: List<Collaborator> = emptyList(),
    val isCollaboratorMenuExpanded: Boolean = false,
    val collaboratorPendingRemoval: Collaborator? = null
)

data class ChecklistItem(
    val text: String,
    val isDone: Boolean
)

data class Collaborator(
    val userId: Int,
    val username: String,
    val isOwner: Boolean,
    val sharedNoteId: Int? = null
)