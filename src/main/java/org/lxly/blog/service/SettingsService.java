package org.lxly.blog.service;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.response.SettingsDto;
import org.lxly.blog.entity.Settings;
import org.lxly.blog.repository.SettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   // ← 这里导入事务注解

import java.util.HashSet;                                      // ← 这里导入 HashSet
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository repo;

    /** 读取站点全局设置 */
    public SettingsDto getSettings() {
        Settings s = repo.findAll().stream().findFirst().orElse(new Settings());
        return SettingsDto.builder()
                .siteName(s.getSiteName())
                .customBackground(s.getCustomBackground())
                .avatar(s.getAvatar())
                .nickname(s.getNickname())
                .bio(s.getBio())
                .notice(s.getNotice())
                .categories(s.getCategories())
                .tags(s.getTags())
                .visitorCount(s.getVisitorCount())
                .siteStartDate(s.getSiteStartDate())
                .build();
    }

    /** 初始化或更新（setup 页面调用） */
    @Transactional
    public SettingsDto initOrUpdate(SettingsDto dto) {
        Settings s = repo.findAll().stream().findFirst().orElse(new Settings());
        s.setSiteName(dto.getSiteName());
        s.setCustomBackground(dto.getCustomBackground());
        s.setAvatar(dto.getAvatar());
        s.setNickname(dto.getNickname());
        s.setBio(dto.getBio());
        s.setNotice(dto.getNotice());

        // 防止 NPE：如果前端没有传 categories / tags，则使用空集合
        Set<String> categories = dto.getCategories() == null ? new HashSet<>() : dto.getCategories();
        Set<String> tags       = dto.getTags() == null ? new HashSet<>()       : dto.getTags();

        s.setCategories(categories);
        s.setTags(tags);

        repo.save(s);
        return getSettings();   // 返回最新的 SettingsDto
    }

    /** 读取 About（这里直接复用 Settings 表的 notice 字段） */
    public SettingsDto getAbout() {
        Settings s = repo.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("站点未初始化"));
        return SettingsDto.builder()
                .siteName(s.getSiteName())
                .notice(s.getNotice())
                .build();
    }
}
