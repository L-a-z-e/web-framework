package com.laze.backend.user.mapper;

import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
//import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapStructMapper {
//    UserMapStructMapper INSTANCE = Mappers.getMapper(UserMapStructMapper.class);
    /**
     * CustomUserDetails 객체를 UserInfo DTO 객체로 변환하는 메소드 규칙
     * @param customUserDetails 변환할 CustomUserDetails 객체
     * @return 변환된 UserInfo 객체
     */

    // source: 원본 객체(CustomUserDetails)의 필드 이름 (getter 이름에서 파생될 수 있음)
    // target: 대상 객체(UserInfo)의 필드 이름
    @Mapping(source = "authorities", target = "authorityGroupIds", qualifiedByName = "mapAuthoritiesToStrings")
    @Mapping(target = "retmYn", ignore = true)
    @Mapping(target = "pwno", ignore = true)
    @Mapping(target = "pwnoErrorRtrv", ignore = true)
    @Mapping(target = "pwnoChgDt", ignore = true)
    UserInfo toUserInfo(CustomUserDetails customUserDetails); // 변환 메소드 시그니처 정의

    /**
     * Collection<? extends GrantedAuthority> 를 List<String> 으로 변환하는 헬퍼 메소드
     * MapStruct는 이 메소드를 찾아서 authorities 필드 매핑 시 자동으로 사용합니다.
     * @param authorities GrantedAuthority 컬렉션
     * @return 권한 이름 문자열 리스트
     */
    // 메소드 이름은 자유롭게 지정 가능, @Named 로 이름을 부여하여 @Mapping 에서 참조할 수도 있음
    // 여기서는 이름 기반으로 찾도록 함 (또는 아래처럼 default 메소드로 직접 구현)
    @Named("mapAuthoritiesToStrings")
    default List<String> mapAuthoritiesToStrings(Collection<? extends GrantedAuthority> authorities) {
        if (authorities == null) {
            return Collections.emptyList();
        }
        // 각 GrantedAuthority 객체에서 getAuthority() 메소드로 문자열 이름 추출
        return authorities.stream()
            .map(GrantedAuthority::getAuthority) // .map(auth -> auth.getAuthority()) 와 동일
            .collect(Collectors.toList());
    }
}
