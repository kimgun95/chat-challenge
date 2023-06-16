package com.challenge.chat.domain.chat.repository;

import com.challenge.chat.domain.chat.entity.MemberChatRoom;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.Optional;

public interface MemberChatRoomRepository extends CassandraRepository<MemberChatRoom, String> {

	Optional<MemberChatRoom> findByMemberEmailAndRoomId(String memberEmail, String roomId);

	Optional<List<MemberChatRoom>> findByMemberEmail(String email);
}
