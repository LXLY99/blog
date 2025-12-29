package org.lxly.blog.mapper;

import org.lxly.blog.dto.response.UserInfoDto;
import org.lxly.blog.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInfoDto toDto(User user);
}
