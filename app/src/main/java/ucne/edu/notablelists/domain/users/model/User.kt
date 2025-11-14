package ucne.edu.notablelists.domain.users.model

data class User(
    val userId: Int = 0,
    val username: String,
    val password: String
)