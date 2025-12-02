package ucne.edu.notablelists

import android.util.Log
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ucne.edu.notablelists.data.local.Notes.NoteDao
import ucne.edu.notablelists.data.local.Notes.SharedNoteEntity
import ucne.edu.notablelists.data.remote.DataSource.NoteRemoteDataSource
import ucne.edu.notablelists.data.remote.Resource
import ucne.edu.notablelists.data.remote.dto.*
import ucne.edu.notablelists.data.repository.SharedNoteRepositoryImpl
import ucne.edu.notablelists.domain.notes.model.Note

@ExperimentalCoroutinesApi
class SharedNoteRepositoryImplTest {

    @MockK
    private lateinit var remoteDataSource: NoteRemoteDataSource

    @MockK
    private lateinit var localDataSource: NoteDao

    private lateinit var repository: SharedNoteRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(android.util.Log::class)

        every { Log.d(any<String>(), any<String>()) } returns 0
        every { Log.e(any<String>(), any<String>()) } returns 0
        every { Log.i(any<String>(), any<String>()) } returns 0
        MockKAnnotations.init(this, relaxUnitFun = true)
        repository = SharedNoteRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Test
    fun `getNotesSharedWithMe should return success with mapped notes`() = runTest {
        val userId = 1
        val mockDto = SharedNoteWithDetailsDto(
            sharedNoteId = 100,
            noteId = 50,
            noteTitle = "Test Note",
            noteDescription = "Test Description",
            ownerUserId = 2,
            ownerUsername = "friend",
            tag = "work",
            isFinished = false,
            reminder = "tomorrow",
            checklist = "[]",
            priority = 1
        )
        val mockResource = Resource.Success(listOf(mockDto))

        coEvery { remoteDataSource.getNotesSharedWithMe(userId) } returns mockResource

        val result = repository.getNotesSharedWithMe(userId)

        assertTrue(result is Resource.Success)
        val notes = (result as Resource.Success).data
        assertEquals(1, notes?.size)
        assertEquals("Test Note", notes?.get(0)?.title)
        assertEquals("Test Description", notes?.get(0)?.description)
    }

    @Test
    fun `getNotesSharedWithMe should return error when remote fails`() = runTest {
        val userId = 1
        val errorMessage = "Network error"
        val mockResource = Resource.Error<List<SharedNoteWithDetailsDto>>(errorMessage)

        coEvery { remoteDataSource.getNotesSharedWithMe(userId) } returns mockResource

        val result = repository.getNotesSharedWithMe(userId)

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
    }

    @Test
    fun `shareNote should return success and save to local database`() = runTest {
        val userId = 1
        val noteId = 50
        val friendId = 2
        val shareResponse = ShareResponseDto(
            message = "Shared successfully",
            sharedNoteId = 100,
            friendName = "Friend",
            noteTitle = "Test Note"
        )
        val mockResource = Resource.Success(shareResponse)

        coEvery { remoteDataSource.shareNoteWithFriend(userId, noteId, friendId) } returns mockResource

        val result = repository.shareNote(userId, noteId, friendId)

        assertTrue(result is Resource.Success)
        assertEquals(shareResponse, (result as Resource.Success).data)

        coVerify {
            localDataSource.upsertSharedNote(
                match { entity ->
                    entity.remoteId == 100 &&
                            entity.noteId == 50 &&
                            entity.ownerUserId == 1 &&
                            entity.targetUserId == 2 &&
                            entity.status == "active"
                }
            )
        }
    }

    @Test
    fun `shareNote should return error when remote fails`() = runTest {
        val userId = 1
        val noteId = 50
        val friendId = 2
        val errorMessage = "Already shared"
        val mockResource = Resource.Error<ShareResponseDto>(errorMessage)

        coEvery { remoteDataSource.shareNoteWithFriend(userId, noteId, friendId) } returns mockResource

        val result = repository.shareNote(userId, noteId, friendId)

        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, (result as Resource.Error).message)
        coVerify(exactly = 0) { localDataSource.upsertSharedNote(any()) }
    }

    @Test
    fun `getSharedNoteDetails should return mapped note when found`() = runTest {
        val userId = 1
        val noteId = 50
        val mockDto = SharedNoteWithDetailsDto(
            sharedNoteId = 100,
            noteId = 50,
            noteTitle = "Test Note",
            noteDescription = "Test Description",
            ownerUserId = 2,
            ownerUsername = "friend",
            tag = "work",
            isFinished = false,
            reminder = "tomorrow",
            checklist = "[]",
            priority = 1
        )
        val mockResource = Resource.Success<SharedNoteWithDetailsDto?>(mockDto)

        coEvery { remoteDataSource.getSharedNoteDetails(userId, noteId) } returns mockResource

        val result = repository.getSharedNoteDetails(userId, noteId)

        assertTrue(result is Resource.Success)
        val note = (result as Resource.Success).data
        assertEquals("Test Note", note?.title)
        assertEquals("Test Description", note?.description)
        assertEquals(50, note?.remoteId)
    }

    @Test
    fun `getSharedNoteDetails should return error when not found`() = runTest {
        val userId = 1
        val noteId = 50
        val mockResource = Resource.Success<SharedNoteWithDetailsDto?>(null)

        coEvery { remoteDataSource.getSharedNoteDetails(userId, noteId) } returns mockResource

        val result = repository.getSharedNoteDetails(userId, noteId)

        assertTrue(result is Resource.Error)
        assertEquals("Nota no encontrada o vacía", (result as Resource.Error).message)
    }

    @Test
    fun `canAccessNote should return true when user owns note`() = runTest {
        val userId = 1
        val noteId = 50
        val mockNoteEntity = createMockNoteEntity(userId = userId)

        coEvery { localDataSource.getNoteByRemoteId(noteId) } returns mockNoteEntity

        val result = repository.canAccessNote(userId, noteId)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(true, data)
    }

    @Test
    fun `canAccessNote should return true when note is shared with user`() = runTest {
        val userId = 1
        val noteId = 50
        val mockNoteEntity = createMockNoteEntity(userId = 2)

        coEvery { localDataSource.getNoteByRemoteId(noteId) } returns mockNoteEntity
        coEvery { localDataSource.isNoteSharedWithUser(noteId, userId) } returns true

        val result = repository.canAccessNote(userId, noteId)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(true, data)
    }


    @Test
    fun `canAccessNote should return false when user has no access`() = runTest {
        val userId = 1
        val noteId = 50
        val mockNoteEntity = createMockNoteEntity(userId = 2)

        coEvery { localDataSource.getNoteByRemoteId(noteId) } returns mockNoteEntity
        coEvery { localDataSource.isNoteSharedWithUser(noteId, userId) } returns false

        val result = repository.canAccessNote(userId, noteId)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(false, data)
    }

    @Test
    fun `updateSharedNoteStatus should update local database on success`() = runTest {
        val userId = 1
        val sharedNoteId = 100
        val updateResponse = UpdateSharedStatusResponseDto(
            message = "Updated",
            removedBy = "owner",
            newStatus = "removed_by_owner",
            sharedNoteId = sharedNoteId
        )
        val mockResource = Resource.Success(updateResponse)
        val mockEntity = SharedNoteEntity(
            remoteId = sharedNoteId,
            noteId = 50,
            ownerUserId = 1,
            targetUserId = 2,
            status = "active"
        )

        coEvery { remoteDataSource.updateSharedNoteStatus(userId, sharedNoteId) } returns mockResource
        coEvery { localDataSource.getSharedNoteByRemoteId(sharedNoteId) } returns mockEntity

        val result = repository.updateSharedNoteStatus(userId, sharedNoteId)

        assertTrue(result is Resource.Success)
        assertEquals(updateResponse, (result as Resource.Success).data)

        coVerify {
            localDataSource.upsertSharedNote(
                mockEntity.copy(status = "removed_by_owner")
            )
        }
    }

    @Test
    fun `syncSharedNotes should clear and save both shared with me and by me`() = runTest {
        val userId = 1

        val sharedWithMeDto = SharedNoteWithDetailsDto(
            sharedNoteId = 100,
            noteId = 50,
            noteTitle = "Shared With Me",
            noteDescription = "Description",
            ownerUserId = 2,
            ownerUsername = "owner",
            tag = "tag",
            isFinished = false,
            reminder = null,
            checklist = null,
            priority = 0
        )

        val sharedByMeDto = SharedNoteByMeDto(
            sharedNoteId = 101,
            noteId = 51,
            noteTitle = "Shared By Me",
            targetUserId = 3,
            targetUsername = "target",
            status = "active"
        )

        val withMeSuccess = Resource.Success(listOf(sharedWithMeDto))
        val byMeSuccess = Resource.Success(listOf(sharedByMeDto))

        coEvery { remoteDataSource.getNotesSharedWithMe(userId) } returns withMeSuccess
        coEvery { remoteDataSource.getNotesSharedByMe(userId) } returns byMeSuccess

        val result = repository.syncSharedNotes(userId)

        assertTrue(result is Resource.Success)

        coVerify { localDataSource.clearAllSharedNotes() }

        coVerify {
            localDataSource.upsertSharedNote(
                match { entity ->
                    entity.remoteId == 100 &&
                            entity.noteId == 50 &&
                            entity.ownerUserId == 2 &&
                            entity.targetUserId == 1 &&
                            entity.status == "active"
                }
            )
        }

        coVerify {
            localDataSource.upsertSharedNote(
                match { entity ->
                    entity.remoteId == 101 &&
                            entity.noteId == 51 &&
                            entity.ownerUserId == 1 &&
                            entity.targetUserId == 3 &&
                            entity.status == "active"
                }
            )
        }
    }

    @Test
    fun `canShareNote should return true for valid noteId`() = runTest {
        val noteId = 50

        val result = repository.canShareNote(noteId)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(true, data)
    }

    @Test
    fun `canShareNote should return false for null noteId`() = runTest {
        val result = repository.canShareNote(null)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(false, data)
    }

    @Test
    fun `canShareNote should return false for invalid noteId`() = runTest {
        val result = repository.canShareNote(-1)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(false, data)
    }

    @Test
    fun `getNotesSharedByMe should return success`() = runTest {
        val userId = 1
        val mockDto = SharedNoteByMeDto(
            sharedNoteId = 100,
            noteId = 50,
            noteTitle = "Test Note",
            targetUserId = 2,
            targetUsername = "friend",
            status = "active"
        )
        val mockResource = Resource.Success(listOf(mockDto))

        coEvery { remoteDataSource.getNotesSharedByMe(userId) } returns mockResource

        val result = repository.getNotesSharedByMe(userId)

        assertTrue(result is Resource.Success)
        val data = (result as Resource.Success).data
        assertEquals(1, data?.size)
        assertEquals("Test Note", data?.get(0)?.noteTitle)
    }

    @Test
    fun `getAllSharedNotes should return pair of lists`() = runTest {
        val userId = 1

        val withMeDto = SharedNoteWithDetailsDto(
            sharedNoteId = 100,
            noteId = 50,
            noteTitle = "With Me",
            noteDescription = "Desc",
            ownerUserId = 2,
            ownerUsername = "owner",
            tag = "tag",
            isFinished = false,
            reminder = null,
            checklist = null,
            priority = 0
        )

        val byMeDto = SharedNoteByMeDto(
            sharedNoteId = 101,
            noteId = 51,
            noteTitle = "By Me",
            targetUserId = 3,
            targetUsername = "target",
            status = "active"
        )

        val withMeResource = Resource.Success(listOf(withMeDto))
        val byMeResource = Resource.Success(listOf(byMeDto))

        coEvery { remoteDataSource.getNotesSharedWithMe(userId) } returns withMeResource
        coEvery { remoteDataSource.getNotesSharedByMe(userId) } returns byMeResource

        val result = repository.getAllSharedNotes(userId)

        assertTrue(result is Resource.Success)
        val pair = (result as Resource.Success).data
        assertEquals(1, pair?.first?.size)
        assertEquals(1, pair?.second?.size)
        assertEquals("With Me", pair?.first[0]?.noteTitle)
        assertEquals("By Me", pair?.second[0]?.noteTitle)
    }

    private fun createMockNoteEntity(
        remoteId: Int = 50,
        userId: Int? = 1
    ): ucne.edu.notablelists.data.local.Notes.NoteEntity {
        return ucne.edu.notablelists.data.local.Notes.NoteEntity(
            id = "test-id",
            remoteId = remoteId,
            userId = userId,
            title = "Test Note",
            description = "Test Description",
            tag = "work",
            isFinished = false,
            reminder = null,
            checklist = null,
            priority = 1,
            isPendingCreate = false
        )
    }
}