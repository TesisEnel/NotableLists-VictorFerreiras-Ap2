package ucne.edu.notablelists.data.remote.dto

data class ShareResponseDto(
    val message: String,
    val sharedNoteId: Int,
    val friendName: String,
    val noteTitle: String
)