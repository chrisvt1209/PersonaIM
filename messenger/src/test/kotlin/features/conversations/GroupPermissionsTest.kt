package features.conversations

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val MAINTAINER_ID = 1L
private const val OTHER_MAINTAINER_ID = 2L
private const val INVITER_ID = 3L
private const val MEMBER_ID = 4L
private const val PENDING_ID = 5L
private const val OUTSIDER_ID = 99L

private fun groupOf(vararg participants: Participant) = Conversation(
    id = 1L,
    title = "Test group",
    type = ConversationType.GROUP,
    participants = participants.toList()
)

private fun singleOf(vararg participants: Participant) = Conversation(
    id = 2L,
    title = null,
    type = ConversationType.SINGLE,
    participants = participants.toList()
)

private fun participant(
    userId: Long,
    role: String,
    status: String = ParticipantStatus.ACCEPTED
) = Participant(userId = userId, username = "user$userId", status = status, role = role)

private val standardGroup = groupOf(
    participant(MAINTAINER_ID, ParticipantRole.MAINTAINER),
    participant(OTHER_MAINTAINER_ID, ParticipantRole.MAINTAINER),
    participant(INVITER_ID, ParticipantRole.INVITER),
    participant(MEMBER_ID, ParticipantRole.MEMBER),
    participant(PENDING_ID, ParticipantRole.MEMBER, status = ParticipantStatus.PENDING)
)

class GroupPermissionsTest {

    @Test
    fun `maintainer and inviter can invite, member and outsiders cannot`() {
        assertTrue(GroupPermissions.canInvite(standardGroup, MAINTAINER_ID))
        assertTrue(GroupPermissions.canInvite(standardGroup, INVITER_ID))
        assertFalse(GroupPermissions.canInvite(standardGroup, MEMBER_ID))
        assertFalse(GroupPermissions.canInvite(standardGroup, OUTSIDER_ID))
    }

    @Test
    fun `pending invitee has no invite permission even with an elevated stored role`() {
        assertFalse(GroupPermissions.canInvite(standardGroup, PENDING_ID))
    }

    @Test
    fun `maintainer can remove a plain member`() {
        assertTrue(GroupPermissions.canRemove(standardGroup, MAINTAINER_ID, MEMBER_ID))
    }

    @Test
    fun `maintainer can remove an inviter`() {
        assertTrue(GroupPermissions.canRemove(standardGroup, MAINTAINER_ID, INVITER_ID))
    }

    @Test
    fun `maintainer cannot remove another maintainer`() {
        assertFalse(GroupPermissions.canRemove(standardGroup, MAINTAINER_ID, OTHER_MAINTAINER_ID))
    }

    @Test
    fun `inviter and member cannot remove anyone`() {
        assertFalse(GroupPermissions.canRemove(standardGroup, INVITER_ID, MEMBER_ID))
        assertFalse(GroupPermissions.canRemove(standardGroup, MEMBER_ID, PENDING_ID))
    }

    @Test
    fun `nobody can remove themselves through removeMember`() {
        assertFalse(GroupPermissions.canRemove(standardGroup, MAINTAINER_ID, MAINTAINER_ID))
    }

    @Test
    fun `maintainer can change role of a member or inviter`() {
        assertTrue(GroupPermissions.canChangeRole(standardGroup, MAINTAINER_ID, MEMBER_ID))
        assertTrue(GroupPermissions.canChangeRole(standardGroup, MAINTAINER_ID, INVITER_ID))
    }

    @Test
    fun `maintainer cannot change role of another maintainer`() {
        assertFalse(GroupPermissions.canChangeRole(standardGroup, MAINTAINER_ID, OTHER_MAINTAINER_ID))
    }

    @Test
    fun `inviter and member cannot change anyone's role`() {
        assertFalse(GroupPermissions.canChangeRole(standardGroup, INVITER_ID, MEMBER_ID))
        assertFalse(GroupPermissions.canChangeRole(standardGroup, MEMBER_ID, PENDING_ID))
    }

    @Test
    fun `only a maintainer can delete the group`() {
        assertTrue(GroupPermissions.canDeleteGroup(standardGroup, MAINTAINER_ID))
        assertFalse(GroupPermissions.canDeleteGroup(standardGroup, INVITER_ID))
        assertFalse(GroupPermissions.canDeleteGroup(standardGroup, MEMBER_ID))
    }

    @Test
    fun `1-to-1 conversations never grant group permissions`() {
        val conversation = singleOf(
            participant(MAINTAINER_ID, ParticipantRole.MAINTAINER),
            participant(MEMBER_ID, ParticipantRole.MEMBER)
        )

        assertFalse(GroupPermissions.canInvite(conversation, MAINTAINER_ID))
        assertFalse(GroupPermissions.canRemove(conversation, MAINTAINER_ID, MEMBER_ID))
        assertFalse(GroupPermissions.canChangeRole(conversation, MAINTAINER_ID, MEMBER_ID))
        assertFalse(GroupPermissions.canDeleteGroup(conversation, MAINTAINER_ID))
    }
}
