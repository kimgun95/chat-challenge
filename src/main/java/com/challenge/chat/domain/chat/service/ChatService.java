package com.challenge.chat.domain.chat.service;

import com.challenge.chat.domain.chat.dto.ChatDto;
import com.challenge.chat.domain.chat.dto.ChatRoomDto;
import com.challenge.chat.domain.chat.entity.Chat;
import com.challenge.chat.domain.chat.entity.ChatRoom;
import com.challenge.chat.domain.chat.entity.MemberChatRoom;
import com.challenge.chat.domain.chat.entity.MessageType;
import com.challenge.chat.domain.chat.repository.ChatRepository;
import com.challenge.chat.domain.chat.repository.ChatRoomRepository;
import com.challenge.chat.domain.chat.repository.MemberChatRoomRepository;
import com.challenge.chat.domain.member.entity.Member;
import com.challenge.chat.domain.member.service.MemberService;
import com.challenge.chat.exception.RestApiException;
import com.challenge.chat.exception.dto.ChatErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

	private final MemberChatRoomRepository memberChatRoomRepository;
	private final ChatRoomRepository chatRoomRepository;
	private final ChatRepository chatRepository;
	private final MemberService memberService;

	@Transactional
	public ChatRoomDto makeChatRoom(final String roomName, final String memberEmail) {

		ChatRoom chatRoom = ChatRoom.of(roomName);
		Member member = memberService.findMemberByEmail(memberEmail);

		// TODO : 비동기적으로 chatRoom 과 memberChatRoom을 저장하기
		chatRoomRepository.save(chatRoom);
		memberChatRoomRepository.save(MemberChatRoom.of(chatRoom, member));

		return ChatRoomDto.from(chatRoom);
	}

	@Transactional
	public ChatRoomDto registerChatRoom(final String roomCode, final String memberEmail) {

		ChatRoom chatRoom = findChatRoom(roomCode);
		Member member = memberService.findMemberByEmail(memberEmail);
		if (memberChatRoomRepository.findByMemberAndRoom(member, chatRoom).isEmpty()){
			memberChatRoomRepository.save(MemberChatRoom.of(chatRoom, member));
		}

		return ChatRoomDto.from(chatRoom);
	}

	@Transactional(readOnly = true)
	public List<ChatRoomDto> searchChatRoomList(final String memberEmail) {

		// TODO : 채팅방 리스트를 가져오는 동작이 2번의 쿼리를 동기적으로 실행해서 오히려 느려질 수 있는 지점이 될 수 있음
		Member member = memberService.findMemberByEmail(memberEmail);
		List<MemberChatRoom> memberChatRoomList = findChatRoomByMember(member);

		return memberChatRoomList
			.stream()
			.map(a -> ChatRoomDto.from(a.getRoom()))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<ChatDto> searchChatList(final String roomCode, final String memberEmail) {

		List<Chat> chatList = chatRepository.findByRoomCode(roomCode).orElse(Collections.emptyList());

		return chatList.stream()
			.map(ChatDto::from)
			.sorted(Comparator.comparing(ChatDto::getCreatedAt))
			.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public ChatDto makeEnterMessageAndSetSessionAttribute(ChatDto chatDto, SimpMessageHeaderAccessor headerAccessor) {

		// socket session에 사용자의 정보 저장
		try {
			Objects.requireNonNull(headerAccessor.getSessionAttributes()).put("email", chatDto.getEmail());
			headerAccessor.getSessionAttributes().put("roomCode", chatDto.getRoomCode());
			headerAccessor.getSessionAttributes().put("nickname", chatDto.getNickname());
		} catch (Exception e) {
			throw new RestApiException(ChatErrorCode.SOCKET_CONNECTION_ERROR);
		}

		chatDto.setMessage(chatDto.getNickname() + "님 입장!! ο(=•ω＜=)ρ⌒☆");

		return chatDto;
	}


	public ChatDto leaveChatRoom(SimpMessageHeaderAccessor headerAccessor) {

		String roomCode = (String)headerAccessor.getSessionAttributes().get("roomCode");
		String nickName = (String)headerAccessor.getSessionAttributes().get("nickName");
		String userId = (String)headerAccessor.getSessionAttributes().get("userId");

		return ChatDto.builder()
			.type(MessageType.LEAVE)
			.roomCode(roomCode)
			.nickname(nickName)
			.email(userId)
			.message(nickName + "님 퇴장!! ヽ(*。>Д<)o゜")
			.build();
	}

	public ChatRoom findChatRoom(String roomCode) {
		return chatRoomRepository.findByRoomCode(roomCode).orElseThrow(
			() -> new RestApiException(ChatErrorCode.CHATROOM_NOT_FOUND));
	}

	private List<MemberChatRoom> findChatRoomByMember(Member member) {
		return memberChatRoomRepository.findByMember(member).orElseThrow(
			() -> new RestApiException(ChatErrorCode.CHATROOM_NOT_FOUND));
	}
}