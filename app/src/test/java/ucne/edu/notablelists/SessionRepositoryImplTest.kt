package ucne.edu.notablelists

import android.util.Log
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import ucne.edu.notablelists.data.local.SessionManager
import ucne.edu.notablelists.data.repository.SessionRepositoryImpl
import ucne.edu.notablelists.domain.users.model.User

@OptIn(ExperimentalCoroutinesApi::class)
class SessionRepositoryImplTest {

    private lateinit var repository: SessionRepositoryImpl
    private lateinit var sessionManager: SessionManager

    private val testUserId = 1
    private val testUsername = "testuser"

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)

        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        sessionManager = mockk()
        repository = SessionRepositoryImpl(sessionManager)
    }

    @Test
    fun `getUserSession should return username from session manager`() = runTest {
        val usernameFlow = flowOf(testUsername)
        every { sessionManager.getUser() } returns usernameFlow

        val resultFlow = repository.getUserSession()
        val result = resultFlow.first()

        assertEquals(testUsername, result)
        verify { sessionManager.getUser() }
    }

    @Test
    fun `getUserId should return userId from session manager`() = runTest {
        val userIdFlow = flowOf(testUserId)
        every { sessionManager.getUserId() } returns userIdFlow

        val resultFlow = repository.getUserId()
        val result = resultFlow.first()

        assertEquals(testUserId, result)
        verify { sessionManager.getUserId() }
    }

    @Test
    fun `saveUserSession should delegate to session manager`() = runTest {
        coEvery { sessionManager.saveUser(testUserId, testUsername) } just Runs

        repository.saveUserSession(testUserId, testUsername)

        coVerify { sessionManager.saveUser(testUserId, testUsername) }
    }

    @Test
    fun `clearUserSession should delegate to session manager`() = runTest {
        coEvery { sessionManager.clearUser() } just Runs

        repository.clearUserSession()

        coVerify { sessionManager.clearUser() }
    }

    @Test
    fun `getUserSession should return empty flow when no user`() = runTest {
        val emptyFlow = flowOf<String?>(null)
        every { sessionManager.getUser() } returns emptyFlow

        val resultFlow = repository.getUserSession()
        val result = resultFlow.first()

        assertNull(result)
    }

    @Test
    fun `getUserId should return null flow when no user`() = runTest {
        val emptyFlow = flowOf<Int?>(null)
        every { sessionManager.getUserId() } returns emptyFlow

        val resultFlow = repository.getUserId()
        val result = resultFlow.first()

        assertNull(result)
    }

    @Test
    fun `saveUserSession should handle different user data`() = runTest {
        val differentUserId = 999
        val differentUsername = "different_user"
        coEvery { sessionManager.saveUser(differentUserId, differentUsername) } just Runs

        repository.saveUserSession(differentUserId, differentUsername)

        coVerify { sessionManager.saveUser(differentUserId, differentUsername) }
    }
}