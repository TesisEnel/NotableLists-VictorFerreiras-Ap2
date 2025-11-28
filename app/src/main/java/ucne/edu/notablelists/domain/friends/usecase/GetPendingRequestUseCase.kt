package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.PendingRequestDto
import ucne.edu.notablelists.domain.friends.FriendsRepository
import javax.inject.Inject

class GetPendingRequestUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(userId: Int): Resource<List<PendingRequestDto>> = repository.getPendingRequests(userId)
}