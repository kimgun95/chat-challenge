package com.challenge.chat.domain.member.controller;

import com.challenge.chat.domain.member.dto.MemberDto;
import com.challenge.chat.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/users")
    public List<MemberDto> getMemberList(){
        log.info("Controller 멤버 리스트 조회");
        return memberService.getMemberList();
    }
}