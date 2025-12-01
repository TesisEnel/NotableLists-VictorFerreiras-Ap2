package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.FriendDto
import ucne.edu.notablelists.domain.friends.repository.FriendsRepository
import javax.inject.Inject

class GetFriendsUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(userId: Int): Resource<List<FriendDto>> = repository.getFriends(userId)
}