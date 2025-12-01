package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.auth.AuthRepository
import ucne.edu.notablelists.domain.notes.repository.NoteRepository
import ucne.edu.notablelists.domain.session.usecase.SaveSessionUseCase
import ucne.edu.notablelists.domain.users.model.User
import javax.inject.Inject

class LoginUserUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val saveSessionUseCase: SaveSessionUseCase,
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(username: String, password: String): Resource<User> {
        val result = authRepository.login(username, password)

        if (result is Resource.Success) {
            val loggedInUser = result.data
            val userId = loggedInUser?.remoteId
            if (userId != null) {
                saveSessionUseCase(userId, username)
                noteRepository.syncOnLogin(userId)
            }
        }

        return result
    }
}