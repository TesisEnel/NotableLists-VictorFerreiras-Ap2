package ucne.edu.notablelists.data.remote

import retrofit2.Response
import retrofit2.http.*
import ucne.edu.notablelists.data.remote.dto.NoteRequestDto
import ucne.edu.notablelists.data.remote.dto.NoteResponseDto
import ucne.edu.notablelists.data.remote.dto.ShareRequestDto
import ucne.edu.notablelists.data.remote.dto.ShareResponseDto
import ucne.edu.notablelists.data.remote.dto.SharedNoteByMeDto
import ucne.edu.notablelists.data.remote.dto.SharedNoteWithDetailsDto
import ucne.edu.notablelists.data.remote.dto.UpdateSharedStatusResponseDto

interface NoteApiService {

    @GET("api/Notes")
    suspend fun getNotes(): Response<List<NoteResponseDto>>

    @GET("api/Notes/{id}")
    suspend fun getNoteById(@Path("id") id: Int): Response<NoteResponseDto>

    @POST("api/Notes")
    suspend fun createNote(@Body request: NoteRequestDto): Response<NoteResponseDto>

    @PUT("api/Notes/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body request: NoteRequestDto): Response<Unit>

    @DELETE("api/Notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int): Response<Unit>

    @GET("api/Notes/Users/{userId}/Notes")
    suspend fun getUserNotes(@Path("userId") userId: Int): Response<List<NoteResponseDto>>

    @POST("api/Notes/Users/{userId}/Notes")
    suspend fun createUserNote(
        @Path("userId") userId: Int,
        @Body request: NoteRequestDto
    ): Response<NoteResponseDto>

    @PUT("api/Notes/Users/{userId}/Notes/{id}")
    suspend fun updateUserNote(
        @Path("userId") userId: Int,
        @Path("id") id: Int,
        @Body request: NoteRequestDto
    ): Response<Unit>

    @DELETE("api/Notes/Users/{userId}/Notes/{id}")
    suspend fun deleteUserNote(
        @Path("userId") userId: Int,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/Notes/Users/{userId}/Notes/{id}")
    suspend fun getUserNoteById(
        @Path("userId") userId: Int,
        @Path("id") id: Int
    ): Response<NoteResponseDto>

    @POST("api/Notes/Users/{userId}/Notes/{noteId}/share")
    suspend fun shareNoteWithFriend(
        @Path("userId") userId: Int,
        @Path("noteId") noteId: Int,
        @Body shareDto: ShareRequestDto
    ): Response<ShareResponseDto>

    @GET("api/Notes/Users/{userId}/shared-notes")
    suspend fun getNotesSharedWithMe(
        @Path("userId") userId: Int
    ): Response<List<SharedNoteWithDetailsDto>>

    @GET("api/Notes/Users/{userId}/shared-by-me")
    suspend fun getNotesSharedByMe(
        @Path("userId") userId: Int
    ): Response<List<SharedNoteByMeDto>>

    @DELETE("api/Notes/Users/{userId}/shared-notes/{sharedNoteId}")
    suspend fun updateSharedNoteStatus(
        @Path("userId") userId: Int,
        @Path("sharedNoteId") sharedNoteId: Int
    ): Response<UpdateSharedStatusResponseDto>
}