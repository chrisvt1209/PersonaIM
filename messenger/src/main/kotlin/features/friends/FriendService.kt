package features.friends

import common.BadRequestException
import common.ConflictException
import common.NotFoundException
import features.users.UserRepository

class FriendService(
    private val friendRepository: FriendRepository,
    private val userRepository: UserRepository
) {
    fun addFriend(userId: Long, friendUid: String): FriendResponse {
        val friend = userRepository.findByUid(friendUid)
            ?: throw NotFoundException("User with UID $friendUid not found")

        if (friend.id == userId) {
            throw BadRequestException("You cannot add yourself as a friend")
        }

        if (friendRepository.areFriends(userId, friend.id)) {
            throw ConflictException("You are already friends with ${friend.username}")
        }

        friendRepository.addFriend(userId, friend.id)
        
        // Add reciprocal relationship for easier bidirectional lookups if needed
        if (!friendRepository.areFriends(friend.id, userId)) {
            friendRepository.addFriend(friend.id, userId)
        }

        return FriendResponse(
            id = friend.id,
            username = friend.username,
            email = friend.email
        )
    }

    fun getFriends(userId: Long): List<FriendResponse> {
        return friendRepository.getFriends(userId).map {
            FriendResponse(
                id = it.id,
                username = it.username,
                email = it.email
            )
        }
    }
}
