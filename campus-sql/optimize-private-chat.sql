USE campus_service;

-- Private chat polling and unread-count indexes.
-- Run each statement once. If MySQL reports duplicate key name, skip that statement.

ALTER TABLE private_message
  ADD KEY idx_private_msg_pair_after_id (sender_id, receiver_id, id);

ALTER TABLE private_message
  ADD KEY idx_private_msg_receiver_read_sender (receiver_id, is_read, sender_id);

ALTER TABLE private_message
  ADD KEY idx_private_msg_user_time (sender_id, receiver_id, create_time, id);
