package ucne.edu.notablelists.data.mappers

import ucne.edu.notablelists.data.remote.dto.FriendDto
import ucne.edu.notablelists.data.remote.dto.PendingRequestDto
import ucne.edu.notablelists.data.remote.dto.UserResponseDto
import ucne.edu.notablelists.domain.friends.model.Friend
import ucne.edu.notablelists.domain.friends.model.PendingRequest
import ucne.edu.notablelists.domain.users.model.User

fun FriendDto.toDomain(): Friend = Friend(
    id = userId,
    username = username
)

fun PendingRequestDto.toDomain(): PendingRequest = PendingRequest(
    id = friendshipId,
    requesterId = requesterId,
    requesterUsername = requesterUsername
)

fun UserResponseDto.toUserDomain(): User = User(
    remoteId = userId,
    username = username,
    password = ""
)