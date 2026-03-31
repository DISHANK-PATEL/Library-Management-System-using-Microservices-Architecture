package com.library.memberservice.mapper;

import com.library.memberservice.dto.MemberDTO;
import com.library.memberservice.entity.Member;

public class MemberMapper {

    public static MemberDTO toDTO(Member member) {
        return MemberDTO.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}