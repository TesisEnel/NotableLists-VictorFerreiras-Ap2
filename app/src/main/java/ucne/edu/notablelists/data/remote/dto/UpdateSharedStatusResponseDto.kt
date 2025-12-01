package ucne.edu.notablelists.data.remote.dto

data class UpdateSharedStatusResponseDto(
    val message: String = "",
    val removedBy: String = "",
    val newStatus: String = "",
    val sharedNoteId: Int = 0
)