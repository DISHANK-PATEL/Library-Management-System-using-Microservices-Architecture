package com.library.memberservice.service;

import com.library.memberservice.dto.MemberDTO;
import com.library.memberservice.entity.Member;
import com.library.memberservice.mapper.MemberMapper;
import com.library.memberservice.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberDTO getMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Member not found with id: " + id));
        return MemberMapper.toDTO(member);
    }
}