package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.friends.repository.FriendsRepository
import javax.inject.Inject

class DeclineFriendRequestUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(userId: Int, friendshipId: Int): Resource<Unit> {
        return repository.declineFriendRequest(userId, friendshipId)
    }
}