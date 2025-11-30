package ucne.edu.notablelists.domain.notes.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.notes.model.Note
import ucne.edu.notablelists.domain.notes.repository.NoteRepository
import javax.inject.Inject

class FetchUserNotesUseCase @Inject constructor(
    private val repository: NoteRepository
) {
    suspend operator fun invoke(userId: Int): Resource<List<Note>> {
        return repository.fetchUserNotesFromApi(userId)
    }
}