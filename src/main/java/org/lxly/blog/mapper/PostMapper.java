package org.lxly.blog.mapper;

import org.lxly.blog.dto.response.*;
import org.lxly.blog.entity.Post;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(target = "slug", source = "slug")
    PostDtos toDto(Post p);

    @Mapping(target = "contentHtml", source = "html")
    @Mapping(target = "authorName", source = "post.author.nickname")
    @Mapping(target = "authorAvatar", source = "post.author.avatar")
    PostDetailDto toDetailDto(Post post, String html);
}
