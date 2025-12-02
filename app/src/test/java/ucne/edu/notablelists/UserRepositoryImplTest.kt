package ucne.edu.notablelists

import android.util.Log
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ucne.edu.notablelists.data.local.Users.UserDao
import ucne.edu.notablelists.data.local.Users.UserEntity
import ucne.edu.notablelists.data.remote.DataSource.UserRemoteDataSource
import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.UserRequestDto
import ucne.edu.notablelists.data.remote.dto.UserResponseDto
import ucne.edu.notablelists.data.repository.UserRepositoryImpl
import ucne.edu.notablelists.domain.users.model.User
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private lateinit var repository: UserRepositoryImpl
    private lateinit var localDataSource: UserDao
    private lateinit var remoteDataSource: UserRemoteDataSource

    private val testUserId = "test-user-id"
    private val testRemoteId = 1
    private val testUsername = "testuser"
    private val testPassword = "password123"

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)

        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        localDataSource = mockk()
        remoteDataSource = mockk()
        repository = UserRepositoryImpl(localDataSource, remoteDataSource)
    }

    @Test
    fun `getUser should return mapped domain model`() = runTest {
        val userEntity = UserEntity(
            id = testUserId,
            remoteId = testRemoteId,
            userName = testUsername,
            password = testPassword,
            isPendingCreate = false
        )

        coEvery { localDataSource.getUser(testUserId) } returns userEntity

        val result = repository.getUser(testUserId)

        assertNotNull(result)
        assertEquals(testRemoteId, result?.remoteId)
        assertEquals(testUsername, result?.username)
    }

    @Test
    fun `getUser should return null when not found`() = runTest {
        coEvery { localDataSource.getUser(testUserId) } returns null

        val result = repository.getUser(testUserId)

        assertNull(result)
    }

    @Test
    fun `upsertUser should update user on server and save locally`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        val requestSlot = slot<UserRequestDto>()
        val entitySlot = slot<UserEntity>()

        coEvery { remoteDataSource.updateUser(testRemoteId, capture(requestSlot)) } returns Resource.Success(Unit)
        coEvery { localDataSource.upsert(capture(entitySlot)) } just Runs

        val result = repository.upsertUser(user)

        assertTrue(result is Resource.Success)

        assertTrue(requestSlot.isCaptured)
        val capturedRequest = requestSlot.captured
        assertEquals(testUsername, capturedRequest.username)
        assertEquals(testPassword, capturedRequest.password)

        assertTrue(entitySlot.isCaptured)
        val capturedEntity = entitySlot.captured
        assertEquals(testUserId, capturedEntity.id)
        assertEquals(testRemoteId, capturedEntity.remoteId)
        assertEquals(testUsername, capturedEntity.userName)
    }

    @Test
    fun `upsertUser should return error when no remoteId`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = null,
            username = testUsername,
            password = testPassword
        )

        val result = repository.upsertUser(user)

        assertTrue(result is Resource.Error)
        assertEquals("No remoteId", (result as Resource.Error).message)
    }

    @Test
    fun `upsertUser should return error when server update fails`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        coEvery { remoteDataSource.updateUser(testRemoteId, any()) } returns Resource.Error("Network error")

        val result = repository.upsertUser(user)

        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `deleteUser should delete from server and locally`() = runTest {
        val userEntity = UserEntity(
            id = testUserId,
            remoteId = testRemoteId,
            userName = testUsername,
            password = testPassword,
            isPendingCreate = false
        )

        coEvery { localDataSource.getUser(testUserId) } returns userEntity
        coEvery { remoteDataSource.deleteUser(testRemoteId) } returns Resource.Success(Unit)
        coEvery { localDataSource.delete(testUserId) } just Runs

        val result = repository.deleteUser(testUserId)

        assertTrue(result is Resource.Success)
        coVerify {
            remoteDataSource.deleteUser(testRemoteId)
            localDataSource.delete(testUserId)
        }
    }

    @Test
    fun `deleteUser should return error when user not found locally`() = runTest {
        coEvery { localDataSource.getUser(testUserId) } returns null

        val result = repository.deleteUser(testUserId)

        assertTrue(result is Resource.Error)
        assertEquals("No encontrado", (result as Resource.Error).message)
    }

    @Test
    fun `deleteUser should return error when no remoteId`() = runTest {
        val userEntity = UserEntity(
            id = testUserId,
            remoteId = null,
            userName = testUsername,
            password = testPassword
        )

        coEvery { localDataSource.getUser(testUserId) } returns userEntity

        val result = repository.deleteUser(testUserId)

        assertTrue(result is Resource.Error)
        assertEquals("No remoteId", (result as Resource.Error).message)
    }

    @Test
    fun `postPendingUsers should sync pending users to server`() = runTest {
        val pendingEntity = UserEntity(
            id = testUserId,
            remoteId = null,
            userName = testUsername,
            password = testPassword,
            isPendingCreate = true
        )

        val userResponse = UserResponseDto(
            userId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        val requestSlot = slot<UserRequestDto>()
        val entitySlot = slot<UserEntity>()

        coEvery { localDataSource.getPendingCreateUsers() } returns listOf(pendingEntity)
        coEvery { remoteDataSource.createUser(capture(requestSlot)) } returns Resource.Success(userResponse)
        coEvery { localDataSource.upsert(capture(entitySlot)) } just Runs

        val result = repository.postPendingUsers()

        assertTrue(result is Resource.Success)

        assertTrue(requestSlot.isCaptured)
        val capturedRequest = requestSlot.captured
        assertEquals(testUsername, capturedRequest.username)
        assertEquals(testPassword, capturedRequest.password)

        assertTrue(entitySlot.isCaptured)
        val capturedEntity = entitySlot.captured
        assertEquals(testRemoteId, capturedEntity.remoteId)
        assertFalse(capturedEntity.isPendingCreate)
    }

    @Test
    fun `postPendingUsers should return error when sync fails`() = runTest {
        val pendingEntity = UserEntity(
            id = testUserId,
            remoteId = null,
            userName = testUsername,
            password = testPassword,
            isPendingCreate = true
        )

        coEvery { localDataSource.getPendingCreateUsers() } returns listOf(pendingEntity)
        coEvery { remoteDataSource.createUser(any()) } returns Resource.Error("Network error")

        val result = repository.postPendingUsers()

        assertTrue(result is Resource.Error)
        assertEquals("Falló sincronización", (result as Resource.Error).message)
    }

    @Test
    fun `postPendingUsers should return success when no pending users`() = runTest {
        coEvery { localDataSource.getPendingCreateUsers() } returns emptyList()

        val result = repository.postPendingUsers()

        assertTrue(result is Resource.Success)
    }

    @Test
    fun `postUser should create user on server and return updated user`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = null,
            username = testUsername,
            password = testPassword
        )

        val userResponse = UserResponseDto(
            userId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        val requestSlot = slot<UserRequestDto>()

        coEvery { remoteDataSource.createUser(capture(requestSlot)) } returns Resource.Success(userResponse)

        val result = repository.postUser(user)

        assertEquals(testRemoteId, result.remoteId)
        assertEquals(testUsername, result.username)

        assertTrue(requestSlot.isCaptured)
        val capturedRequest = requestSlot.captured
        assertEquals(testUsername, capturedRequest.username)
        assertEquals(testPassword, capturedRequest.password)
    }

    @Test(expected = Exception::class)
    fun `postUser should throw exception when server creation fails`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = null,
            username = testUsername,
            password = testPassword
        )

        coEvery { remoteDataSource.createUser(any()) } returns Resource.Error("Network error")

        repository.postUser(user)
    }

    @Test
    fun `putUser should update user on server and return user`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        val requestSlot = slot<UserRequestDto>()

        coEvery { remoteDataSource.updateUser(testRemoteId, capture(requestSlot)) } returns Resource.Success(Unit)

        val result = repository.putUser(user)

        assertEquals(testRemoteId, result.remoteId)
        assertEquals(testUsername, result.username)

        assertTrue(requestSlot.isCaptured)
        val capturedRequest = requestSlot.captured
        assertEquals(testUsername, capturedRequest.username)
        assertEquals(testPassword, capturedRequest.password)
    }

    @Test(expected = Exception::class)
    fun `putUser should throw exception when no remoteId`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = null,
            username = testUsername,
            password = testPassword
        )

        repository.putUser(user)
    }

    @Test(expected = Exception::class)
    fun `putUser should throw exception when server update fails`() = runTest {
        val user = User(
            id = testUserId,
            remoteId = testRemoteId,
            username = testUsername,
            password = testPassword
        )

        coEvery { remoteDataSource.updateUser(testRemoteId, any()) } returns Resource.Error("Network error")

        repository.putUser(user)
    }
}