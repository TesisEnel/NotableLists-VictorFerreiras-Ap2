package ucne.edu.notablelists.domain.friends.usecase

import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.UserResponseDto
import ucne.edu.notablelists.domain.friends.repository.FriendsRepository
import javax.inject.Inject

class GetAllUsersUseCase @Inject constructor(
    private val repository: FriendsRepository
) {
    suspend operator fun invoke(): Resource<List<UserResponseDto>> = repository.getAllUsers()
}