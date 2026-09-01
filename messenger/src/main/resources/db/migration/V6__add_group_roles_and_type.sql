ALTER TABLE conversations ADD COLUMN type VARCHAR(10) NOT NULL DEFAULT 'SINGLE';
UPDATE conversations SET type = 'GROUP' WHERE title IS NOT NULL;

-- No creator is tracked on pre-existing rows, so legacy groups can't be backfilled with a
-- maintainer here; every group created from now on gets one via ConversationRepository.createGroup.
ALTER TABLE conversation_participants ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'MEMBER';
