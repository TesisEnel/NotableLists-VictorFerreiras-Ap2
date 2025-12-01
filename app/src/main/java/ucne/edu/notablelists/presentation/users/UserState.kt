package ucne.edu.notablelists.presentation.users

data class UserState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val showSkipDialog: Boolean = false
)