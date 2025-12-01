package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.domain.friends.repository.FriendsRepository
import javax.inject.Inject

class SendFriendRequestUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(userId: Int, friendId: Int): Resource<Unit> = repository.sendFriendRequest(userId, friendId)
}