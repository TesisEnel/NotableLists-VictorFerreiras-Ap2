package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.notes.repository.NoteRepository
import ucne.edu.notablelists.domain.session.usecase.SaveSessionUseCase
import ucne.edu.notablelists.domain.users.model.User
import ucne.edu.notablelists.domain.users.usecase.PostUserUseCase
import javax.inject.Inject

class SyncUserDataUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(userId: Int) {
        noteRepository.syncOnLogin(userId)
    }
}