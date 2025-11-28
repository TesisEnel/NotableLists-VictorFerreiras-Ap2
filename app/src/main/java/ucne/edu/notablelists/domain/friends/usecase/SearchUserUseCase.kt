package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.UserResponseDto
import ucne.edu.notablelists.domain.friends.FriendsRepository
import javax.inject.Inject

class SearchUserUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(query: String): Resource<List<UserResponseDto>> = repository.searchUsers(query)
}