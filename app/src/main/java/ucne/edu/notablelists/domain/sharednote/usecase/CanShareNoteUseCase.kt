package ucne.edu.notablelists.domain.sharednote.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.sharednote.repository.SharedNoteRepository
import javax.inject.Inject

class CanShareNoteUseCase @Inject constructor(
    private val repository: SharedNoteRepository
) {
    suspend operator fun invoke(noteId: Int?): Resource<Boolean> {
        return repository.canShareNote(noteId)
    }
}