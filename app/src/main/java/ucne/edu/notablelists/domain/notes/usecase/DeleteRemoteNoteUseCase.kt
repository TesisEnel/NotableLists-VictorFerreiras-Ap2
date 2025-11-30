package ucne.edu.notablelists.domain.notes.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.notes.repository.NoteRepository
import javax.inject.Inject

class DeleteRemoteNoteUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(id: Int,userId: Int? = null): Resource<Unit> {
        return repository.deleteRemote(id,userId)
    }
}