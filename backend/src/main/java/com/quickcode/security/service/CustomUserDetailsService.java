package com.quickcode.security.service;

import com.quickcode.entity.User;
import com.quickcode.repository.UserRepository;
import com.quickcode.security.jwt.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务
 * 实现Spring Security的UserDetailsService接口
 * 
 * @author QuickCode Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 根据用户名加载用户详情
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.debug("正在加载用户详情: {}", usernameOrEmail);
        
        User user = userRepository.findByUsernameOrEmail(usernameOrEmail)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", usernameOrEmail);
                    return new UsernameNotFoundException("用户不存在: " + usernameOrEmail);
                });

        log.debug("成功加载用户详情: {} (ID: {})", user.getUsername(), user.getId());
        return UserPrincipal.create(user);
    }

    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        log.debug("正在根据ID加载用户详情: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("用户不存在: {}", userId);
                    return new UsernameNotFoundException("用户不存在: " + userId);
                });

        log.debug("成功根据ID加载用户详情: {} (ID: {})", user.getUsername(), user.getId());
        return UserPrincipal.create(user);
    }
}
