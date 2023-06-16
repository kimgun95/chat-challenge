package com.challenge.chat.domain.member.repository;

import com.challenge.chat.domain.member.entity.MemberFriend;
import org.springframework.data.cassandra.repository.CassandraRepository;

import java.util.List;
import java.util.Optional;

public interface MemberFriendRepository extends CassandraRepository<MemberFriend, String> {

    Optional<MemberFriend> findByMemberEmailAndFriendEmail(String memberEmail, String friendEmail);
    Optional<List<MemberFriend>> findByMemberEmail(String memberEmail);
}
