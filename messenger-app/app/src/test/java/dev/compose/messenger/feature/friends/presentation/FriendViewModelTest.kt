package dev.compose.messenger.feature.friends.presentation

import androidx.compose.ui.graphics.Color
import dev.compose.messenger.feature.conversations.data.ConversationRepository
import dev.compose.messenger.feature.conversations.domain.Conversation
import dev.compose.messenger.feature.conversations.domain.ConversationDetail
import dev.compose.messenger.feature.conversations.domain.GroupInvite
import dev.compose.messenger.feature.friends.data.FriendRepository
import dev.compose.messenger.feature.friends.domain.Friend
import dev.compose.messenger.feature.profile.data.ProfileRepository
import dev.compose.messenger.feature.profile.domain.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeFriendRepository(
    private val friendsFlow: Flow<List<Friend>> = flowOf(emptyList()),
    private val addFriendResult: Result<Unit> = Result.success(Unit)
) : FriendRepository {
    override fun getFriends(): Flow<List<Friend>> = friendsFlow
    override suspend fun addFriend(uid: String): Result<Unit> = addFriendResult
    override suspend fun syncFriends(): Result<Unit> = Result.success(Unit)
}

private class FakeConversationRepository(
    private val createConversationResult: Result<Conversation> = Result.success(fakeConversation())
) : ConversationRepository {
    override fun getConversations(): Flow<List<Conversation>> = flowOf(emptyList())
    override suspend fun createConversation(userId: Long): Result<Conversation> = createConversationResult
    override suspend fun createGroup(title: String, memberUserIds: List<Long>): Result<Conversation> = createConversationResult
    override suspend fun syncConversations(): Result<Unit> = Result.success(Unit)
    override suspend fun deleteConversation(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun getConversationDetail(id: String): Result<ConversationDetail> = throw NotImplementedError()
    override suspend fun getInvites(): Result<List<GroupInvite>> = Result.success(emptyList())
    override suspend fun acceptInvite(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun declineInvite(id: String): Result<Unit> = Result.success(Unit)
    override suspend fun inviteToGroup(conversationId: String, userId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun removeMember(conversationId: String, userId: Long): Result<Unit> = Result.success(Unit)
    override suspend fun changeRole(conversationId: String, userId: Long, role: String): Result<Unit> = Result.success(Unit)
    override suspend fun leaveGroup(conversationId: String): Result<Unit> = Result.success(Unit)
}

private class FakeProfileRepository(
    private val userResult: (Long) -> Result<User> = { Result.success(fakeUser(it)) }
) : ProfileRepository {
    override fun getCurrentUser(): Flow<User?> = MutableStateFlow(null)
    override suspend fun updateProfile(username: String, email: String, avatar: String): Result<Unit> = Result.success(Unit)
    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
    override suspend fun syncProfile(): Result<Unit> = Result.success(Unit)
    override suspend fun getUser(id: Long): Result<User> = userResult(id)
}

private fun fakeConversation(id: String = "1") = Conversation(
    id = id,
    title = "Chat",
    subtitle = "active now",
    participantNames = "",
    lastMessage = "",
    unreadCount = 0,
    accentColor = Color(0xFFC41001)
)

private fun fakeUser(id: Long) = User(id = id, username = "user$id", email = "user$id@leblanc.jp", uid = "uid-$id", avatar = "avatar$id.png")

@OptIn(ExperimentalCoroutinesApi::class)
class FriendViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads friends and their avatars on init`() = runTest {
        val friends = listOf(Friend(id = 2, username = "Ann", email = "ann@shujin.jp"))
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(friendsFlow = flowOf(friends)),
            conversationRepository = FakeConversationRepository(),
            profileRepository = FakeProfileRepository()
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(friends, state.friends)
        assertEquals("avatar2.png", state.friendAvatars[2L])
    }

    @Test
    fun `AddFriend success clears loading and error`() = runTest {
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(),
            conversationRepository = FakeConversationRepository(),
            profileRepository = FakeProfileRepository()
        )

        viewModel.onEvent(FriendEvent.AddFriend("some-uid"))

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `AddFriend failure surfaces error message`() = runTest {
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(addFriendResult = Result.failure(Exception("uid not found"))),
            conversationRepository = FakeConversationRepository(),
            profileRepository = FakeProfileRepository()
        )

        viewModel.onEvent(FriendEvent.AddFriend("bad-uid"))

        assertEquals("uid not found", viewModel.uiState.value.error)
    }

    @Test
    fun `StartChat success emits navigation event to the new conversation`() = runTest {
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(),
            conversationRepository = FakeConversationRepository(createConversationResult = Result.success(fakeConversation(id = "42"))),
            profileRepository = FakeProfileRepository()
        )

        viewModel.onEvent(FriendEvent.StartChat(userId = 2))

        assertEquals("chat/42", viewModel.navigationEvent.value)
    }

    @Test
    fun `onNavigationHandled clears the navigation event`() = runTest {
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(),
            conversationRepository = FakeConversationRepository(),
            profileRepository = FakeProfileRepository()
        )
        viewModel.onEvent(FriendEvent.StartChat(userId = 2))
        assertTrue(viewModel.navigationEvent.value != null)

        viewModel.onNavigationHandled()

        assertNull(viewModel.navigationEvent.value)
    }

    @Test
    fun `ErrorShown clears error`() = runTest {
        val viewModel = FriendViewModel(
            repository = FakeFriendRepository(addFriendResult = Result.failure(Exception("boom"))),
            conversationRepository = FakeConversationRepository(),
            profileRepository = FakeProfileRepository()
        )
        viewModel.onEvent(FriendEvent.AddFriend("bad-uid"))
        assertEquals("boom", viewModel.uiState.value.error)

        viewModel.onEvent(FriendEvent.ErrorShown)

        assertNull(viewModel.uiState.value.error)
    }
}
