package ucne.edu.notablelists

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ucne.edu.notablelists.data.remote.DataSource.UserRemoteDataSource
import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.FriendDto
import ucne.edu.notablelists.data.remote.dto.PendingRequestDto
import ucne.edu.notablelists.data.remote.dto.UserResponseDto
import ucne.edu.notablelists.data.repository.FriendsRepositoryImpl

@OptIn(ExperimentalCoroutinesApi::class)
class FriendsRepositoryImplTest {

    private lateinit var repository: FriendsRepositoryImpl
    private lateinit var remoteDataSource: UserRemoteDataSource

    @Before
    fun setup() {
        remoteDataSource = mockk()
        repository = FriendsRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `sendFriendRequest should delegate to remoteDataSource`() = runTest {
        val userId = 1
        val friendId = 2
        val expectedResult = Resource.Success(Unit)

        coEvery {
            remoteDataSource.sendFriendRequest(userId, friendId)
        } returns expectedResult

        val result = repository.sendFriendRequest(userId, friendId)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `getPendingRequests should delegate to remoteDataSource`() = runTest {
        val userId = 1
        val pendingRequests = listOf(
            PendingRequestDto(
                friendshipId = 1,
                requesterId = 2,
                requesterUsername = "friend1"
            )
        )
        val expectedResult = Resource.Success(pendingRequests)

        coEvery {
            remoteDataSource.getPendingRequests(userId)
        } returns expectedResult

        val result = repository.getPendingRequests(userId)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `acceptFriendRequest should delegate to remoteDataSource`() = runTest {
        val userId = 1
        val friendshipId = 10
        val expectedResult = Resource.Success(Unit)

        coEvery {
            remoteDataSource.acceptFriendRequest(userId, friendshipId)
        } returns expectedResult

        val result = repository.acceptFriendRequest(userId, friendshipId)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `getFriends should delegate to remoteDataSource`() = runTest {
        val userId = 1
        val friends = listOf(
            FriendDto(
                userId = 2,
                username = "friend1"
            ),
            FriendDto(
                userId = 3,
                username = "friend2"
            )
        )
        val expectedResult = Resource.Success(friends)

        coEvery {
            remoteDataSource.getFriends(userId)
        } returns expectedResult

        val result = repository.getFriends(userId)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `getAllUsers should delegate to remoteDataSource`() = runTest {
        val users = listOf(
            UserResponseDto(
                userId = 1,
                username = "user1",
                password = "pass1"
            ),
            UserResponseDto(
                userId = 2,
                username = "user2",
                password = "pass2"
            )
        )
        val expectedResult = Resource.Success(users)

        coEvery {
            remoteDataSource.getAllUsers()
        } returns expectedResult

        val result = repository.getAllUsers()

        assertEquals(expectedResult, result)
    }

    @Test
    fun `searchUsers should filter users by username`() = runTest {
        val searchQuery = "john"
        val allUsers = listOf(
            UserResponseDto(1, "john_doe", "pass1"),
            UserResponseDto(2, "jane_doe", "pass2"),
            UserResponseDto(3, "johnny", "pass3"),
            UserResponseDto(4, "mike", "pass4")
        )

        coEvery {
            remoteDataSource.getAllUsers()
        } returns Resource.Success(allUsers)

        val result = repository.searchUsers(searchQuery)

        assertTrue(result is Resource.Success)
        val filteredUsers = (result as Resource.Success).data
        assertEquals(2, filteredUsers?.size)
        assertTrue(filteredUsers?.all { it.username.contains(searchQuery, ignoreCase = true) } == true)
        assertEquals(listOf(allUsers[0], allUsers[2]), filteredUsers)
    }

    @Test
    fun `searchUsers should return empty list when no matches found`() = runTest {
        val searchQuery = "xyz"
        val allUsers = listOf(
            UserResponseDto(1, "john", "pass1"),
            UserResponseDto(2, "jane", "pass2")
        )

        coEvery {
            remoteDataSource.getAllUsers()
        } returns Resource.Success(allUsers)

        val result = repository.searchUsers(searchQuery)

        assertTrue(result is Resource.Success)
        val filteredUsers = (result as Resource.Success).data
        assertTrue(filteredUsers?.isEmpty() == true)
    }

    @Test
    fun `searchUsers should handle case insensitive search`() = runTest {
        val searchQuery = "JOHN"
        val allUsers = listOf(
            UserResponseDto(1, "john", "pass1"),
            UserResponseDto(2, "Johnny", "pass2"),
            UserResponseDto(3, "mike", "pass3")
        )

        coEvery {
            remoteDataSource.getAllUsers()
        } returns Resource.Success(allUsers)

        val result = repository.searchUsers(searchQuery)

        assertTrue(result is Resource.Success)
        val filteredUsers = (result as Resource.Success).data
        assertEquals(2, filteredUsers?.size)
        assertTrue(filteredUsers?.all {
            it.username.contains("john", ignoreCase = true)
        } == true)
    }

    @Test
    fun `searchUsers should return error when getAllUsers fails`() = runTest {
        val searchQuery = "test"

        coEvery {
            remoteDataSource.getAllUsers()
        } returns Resource.Error("Network error")

        val result = repository.searchUsers(searchQuery)

        assertTrue(result is Resource.Error)
        assertEquals("Network error", (result as Resource.Error).message)
    }

    @Test
    fun `removeFriend should delegate to remoteDataSource`() = runTest {
        val userId = 1
        val friendId = 2
        val expectedResult = Resource.Success(Unit)

        coEvery {
            remoteDataSource.removeFriend(userId, friendId)
        } returns expectedResult

        val result = repository.removeFriend(userId, friendId)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `searchUsers should return loading when getAllUsers returns loading`() = runTest {
        val searchQuery = "test"

        coEvery {
            remoteDataSource.getAllUsers()
        } returns Resource.Loading()

        val result = repository.searchUsers(searchQuery)

        assertTrue(result is Resource.Loading)
    }
}