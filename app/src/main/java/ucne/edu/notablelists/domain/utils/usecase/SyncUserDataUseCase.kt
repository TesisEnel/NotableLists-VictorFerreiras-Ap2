package ucne.edu.notablelists.domain.utils.usecase

import ucne.edu.notablelists.domain.notes.repository.NoteRepository
import javax.inject.Inject

class SyncUserDataUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend operator fun invoke(userId: Int) {
        noteRepository.syncOnLogin(userId)
    }
}