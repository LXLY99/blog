package org.lxly.blog.repository;

import org.lxly.blog.entity.Settings;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {}
