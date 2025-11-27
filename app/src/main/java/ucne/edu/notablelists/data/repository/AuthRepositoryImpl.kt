package ucne.edu.notablelists.data.repository

import ucne.edu.notablelists.data.mappers.toDomain
import ucne.edu.notablelists.data.remote.AuthApiService
import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.UserRequestDto
import ucne.edu.notablelists.data.remote.dto.AuthResponseDto
import ucne.edu.notablelists.domain.auth.AuthRepository
import ucne.edu.notablelists.domain.users.model.User
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApiService
) : AuthRepository {

    override suspend fun login(username: String, password: String): Resource<User> {
        return try {
            val response = authApi.login(UserRequestDto(username, password))

            if (response.success && response.user != null) {
                Resource.Success(response.user.toDomain())
            } else {
                Resource.Error(response.message ?: "Login failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun register(username: String, password: String): Resource<User> {
        return try {
            val response = authApi.register(UserRequestDto(username, password))

            if (response.success && response.user != null) {
                Resource.Success(response.user.toDomain())
            } else {
                Resource.Error(response.message ?: "Registration failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return Resource.Success(Unit)
    }
}