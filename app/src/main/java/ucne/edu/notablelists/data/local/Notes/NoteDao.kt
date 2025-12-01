package ucne.edu.notablelists.data.local.Notes

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: String): NoteEntity?

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT * FROM notes WHERE isPendingCreate = 1")
    suspend fun getPendingCreateNotes(): List<NoteEntity>

    @Query("SELECT * FROM notes")
    fun observeNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId = :userId")
    fun getUserNotes(userId: Int): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE userId IS NULL")
    fun getLocalNotes(): Flow<List<NoteEntity>>
    @Query("UPDATE notes SET userId = :userId WHERE userId IS NULL")
    suspend fun linkNotesToUser(userId: Int)
    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<NoteEntity>

    @Query("DELETE FROM notes")
    suspend fun deleteAllNotes()

    @Upsert
    suspend fun upsertSharedNote(sharedNote: SharedNoteEntity)

    @Query("SELECT * FROM shared_notes WHERE targetUserId = :userId AND status = 'active'")
    fun getSharedNotesWithMe(userId: Int): Flow<List<SharedNoteEntity>>

    @Query("SELECT * FROM shared_notes WHERE ownerUserId = :userId AND status IN ('active', 'hidden_by_target')")
    fun getSharedNotesByMe(userId: Int): Flow<List<SharedNoteEntity>>

    @Query("DELETE FROM shared_notes WHERE remoteId = :remoteId")
    suspend fun deleteSharedNote(remoteId: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM shared_notes WHERE noteId = :noteId AND targetUserId = :userId AND status = 'active')")
    suspend fun isNoteSharedWithUser(noteId: Int, userId: Int): Boolean

    @Query("DELETE FROM shared_notes")
    suspend fun clearAllSharedNotes()

    @Query("SELECT * FROM shared_notes WHERE remoteId = :remoteId")
    suspend fun getSharedNoteByRemoteId(remoteId: Int): SharedNoteEntity?
    @Query("SELECT * FROM notes WHERE remoteId = :remoteId")
    suspend fun getNoteByRemoteId(remoteId: Int): NoteEntity?

    @Query("SELECT * FROM notes WHERE userId = :userId AND remoteId = :remoteId")
    suspend fun getUserNoteByRemoteId(userId: Int, remoteId: Int): NoteEntity?
}