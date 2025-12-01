package ucne.edu.notablelists.presentation.friends

import ucne.edu.notablelists.domain.friends.model.Friend
import ucne.edu.notablelists.domain.friends.model.PendingRequest
import ucne.edu.notablelists.domain.users.model.User

data class FriendsState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val friends: List<Friend> = emptyList(),
    val pendingRequests: List<PendingRequest> = emptyList(),
    val searchResults: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedTabIndex: Int = 0,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val friendToDelete: Friend? = null
)