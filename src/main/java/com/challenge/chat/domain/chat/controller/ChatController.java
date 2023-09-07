package com.challenge.chat.domain.chat.controller;

import com.challenge.chat.domain.chat.dto.ChatDto;
import com.challenge.chat.domain.chat.dto.ChatRoomDto;
import com.challenge.chat.domain.chat.dto.request.ChatRoomAddRequest;
import com.challenge.chat.domain.chat.dto.request.ChatRoomCreateRequest;
import com.challenge.chat.domain.chat.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@CrossOrigin
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;
	private final SimpMessagingTemplate msgOperation;

	@PostMapping("/chat")
	public ResponseEntity<ChatRoomDto> createChatRoom(
		@RequestBody final ChatRoomCreateRequest request,
		@AuthenticationPrincipal final User user) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatService.makeChatRoom(request.getRoomName(), user.getUsername()));
	}

	@PostMapping("/chat/room")
	public ResponseEntity<ChatRoomDto> addChatRoom(
		@RequestBody final ChatRoomAddRequest request,
		@AuthenticationPrincipal final User user) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatService.registerChatRoom(request.getRoomCode(), user.getUsername()));
	}

	@GetMapping("/chat/room")
	public ResponseEntity<List<ChatRoomDto>> showChatRoomList(
		@AuthenticationPrincipal final User user) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatService.searchChatRoomList(user.getUsername()));
	}

	@GetMapping("/chat/{room-code}")
	public ResponseEntity<List<ChatDto>> showChatList(
		@PathVariable("room-code") final String roomCode,
		@AuthenticationPrincipal final User user) {

		return ResponseEntity.status(HttpStatus.OK)
			.body(chatService.searchChatList(roomCode, user.getUsername()));
	}

	@MessageMapping("/chat/enter")
	public void enterChatRoom(
		@RequestBody ChatDto chatDto,
		SimpMessageHeaderAccessor headerAccessor) {

		ChatDto newChatDto = chatService.makeEnterMessageAndSetSessionAttribute(chatDto, headerAccessor);

		msgOperation.convertAndSend("/topic/chat/room/" + chatDto.getRoomCode(), newChatDto);
	}

	@MessageMapping("/chat/send")
	public void sendChatRoom(
		@RequestBody ChatDto chatDto) {


		// chatService.sendChatRoom(chatDto);
		msgOperation.convertAndSend("/topic/chat/room/" + chatDto.getRoomCode(), chatDto);
	}

	// @GetMapping("/chat/{room-code}/{message}")
	// public ResponseEntity<List<ChatDto>> searchChatList(
	// 	@PathVariable("room-code") final String roomCode,
	// 	@PathVariable("message") final String message) {
	//
	// 	log.info("Controller : 채팅 메시지 검색");
	//
	// 	return ResponseEntity.status(HttpStatus.OK)
	// 		.body(chatService.findChatList(roomCode, message));
	// }

	// @EventListener
	// public void webSocketDisconnectListener(SessionDisconnectEvent event) {
	// 	StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
	// 	log.info("Controller webSocketDisconnectListener, 채팅방 나가기");
	// 	ChatDto chatDto = chatService.leaveChatRoom(headerAccessor);
	// 	msgOperation.convertAndSend("/topic/chat/room/" + chatDto.getRoomId(), chatDto);
	// }
}