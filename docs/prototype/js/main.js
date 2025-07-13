// 速码网主页交互脚本

document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initMobileMenu();
    initSearchBox();
    initScrollEffects();
    initAnimations();
    initProjectCards();
    initStatCounters();
    initModernEffects();
    initParallaxEffects();
});

// 移动端菜单功能
function initMobileMenu() {
    const mobileToggle = document.querySelector('.mobile-menu-toggle');
    const navMenu = document.querySelector('.nav-menu');
    const navSearch = document.querySelector('.nav-search');

    if (mobileToggle) {
        mobileToggle.addEventListener('click', function() {
            // 切换菜单显示状态
            navMenu.classList.toggle('mobile-active');
            navSearch.classList.toggle('mobile-active');

            // 切换图标
            const icon = this.querySelector('i');
            if (icon.classList.contains('fa-bars')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }
}

// 搜索框功能
function initSearchBox() {
    const searchInput = document.querySelector('.search-input');
    const searchBtn = document.querySelector('.search-btn');

    if (searchInput && searchBtn) {
        // 搜索按钮点击事件
        searchBtn.addEventListener('click', function() {
            performSearch(searchInput.value);
        });

        // 回车键搜索
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch(this.value);
            }
        });

        // 搜索建议功能（模拟）
        searchInput.addEventListener('input', function() {
            const query = this.value.trim();
            if (query.length > 2) {
                showSearchSuggestions(query);
            } else {
                hideSearchSuggestions();
            }
        });
    }
}

// 执行搜索
function performSearch(query) {
    if (query.trim()) {
        console.log('搜索:', query);
        // 这里可以添加实际的搜索逻辑
        // 例如：window.location.href = `/search?q=${encodeURIComponent(query)}`;
    }
}

// 显示搜索建议（模拟）
function showSearchSuggestions(query) {
    // 模拟搜索建议
    const suggestions = [
        'Vue3 管理后台',
        'React 电商系统',
        'Spring Boot 微服务',
        'Node.js API',
        'Python 爬虫'
    ].filter(item => item.toLowerCase().includes(query.toLowerCase()));

    // 这里可以创建和显示建议下拉框
    console.log('搜索建议:', suggestions);
}

// 隐藏搜索建议
function hideSearchSuggestions() {
    // 隐藏建议下拉框
}

// 滚动效果
function initScrollEffects() {
    const header = document.querySelector('.header');
    let lastScrollY = window.scrollY;
    let isHeaderHidden = false;
    let scrollTimeout = null;
    let scrollDirection = 0; // 1: 向下, -1: 向上, 0: 无明确方向
    let scrollHistory = []; // 记录最近几次滚动方向

    // 滚动配置
    const SCROLL_THRESHOLD = 15; // 滚动阈值，避免小幅滚动触发
    const HIDE_THRESHOLD = 100;  // 开始隐藏header的最小滚动距离
    const DEBOUNCE_DELAY = 80;   // 防抖延迟
    const DIRECTION_HISTORY_LENGTH = 3; // 记录滚动方向历史的长度

    // 检查滚动方向的稳定性
    function getStableScrollDirection(currentScrollY) {
        const currentDirection = currentScrollY > lastScrollY ? 1 : -1;

        // 添加到历史记录
        scrollHistory.push(currentDirection);
        if (scrollHistory.length > DIRECTION_HISTORY_LENGTH) {
            scrollHistory.shift();
        }

        // 只有当最近几次滚动方向一致时才认为方向稳定
        if (scrollHistory.length >= DIRECTION_HISTORY_LENGTH) {
            const isDirectionStable = scrollHistory.every(dir => dir === currentDirection);
            return isDirectionStable ? currentDirection : 0;
        }

        return 0; // 方向不稳定
    }

    // 优化的滚动处理函数
    function handleScroll() {
        const currentScrollY = window.scrollY;
        const scrollDifference = Math.abs(currentScrollY - lastScrollY);

        // 只有滚动距离超过阈值才处理
        if (scrollDifference < SCROLL_THRESHOLD) {
            return;
        }

        // 获取稳定的滚动方向
        const stableDirection = getStableScrollDirection(currentScrollY);

        // 头部阴影效果（立即响应）
        if (currentScrollY > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }

        // 头部隐藏/显示效果（需要稳定的滚动方向）
        if (stableDirection !== 0) {
            const shouldHide = stableDirection === 1 && currentScrollY > HIDE_THRESHOLD;
            const shouldShow = stableDirection === -1 || currentScrollY <= HIDE_THRESHOLD;

            // 防止频繁切换状态
            if (shouldHide && !isHeaderHidden) {
                header.classList.add('hidden');
                isHeaderHidden = true;
            } else if (shouldShow && isHeaderHidden) {
                header.classList.remove('hidden');
                isHeaderHidden = false;
            }
        }

        lastScrollY = currentScrollY;
    }

    // 检测是否为移动设备
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
                     window.innerWidth <= 768;

    // 移动端使用不同的配置
    const mobileConfig = {
        SCROLL_THRESHOLD: isMobile ? 25 : SCROLL_THRESHOLD,
        DEBOUNCE_DELAY: isMobile ? 120 : DEBOUNCE_DELAY,
        DIRECTION_HISTORY_LENGTH: isMobile ? 4 : DIRECTION_HISTORY_LENGTH
    };

    // 使用防抖的滚动监听器
    window.addEventListener('scroll', function() {
        // 清除之前的定时器
        if (scrollTimeout) {
            clearTimeout(scrollTimeout);
        }

        // 立即处理阴影效果（不需要防抖）
        const currentScrollY = window.scrollY;
        if (currentScrollY > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }

        // 防抖处理隐藏/显示效果
        scrollTimeout = setTimeout(handleScroll, mobileConfig.DEBOUNCE_DELAY);
    }, { passive: true }); // 使用passive监听器提升性能

    // 页面可见性变化时重置状态
    document.addEventListener('visibilitychange', function() {
        if (document.visibilityState === 'visible') {
            // 页面重新可见时重置滚动状态
            scrollHistory = [];
            lastScrollY = window.scrollY;
        }
    });

    // 窗口大小变化时重新检测设备类型
    window.addEventListener('resize', debounce(function() {
        const newIsMobile = window.innerWidth <= 768;
        if (newIsMobile !== isMobile) {
            // 设备类型变化时重置状态
            scrollHistory = [];
            isHeaderHidden = false;
            header.classList.remove('hidden');
        }
    }, 250));
}

// 动画效果
function initAnimations() {
    // 创建Intersection Observer来触发动画
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
            }
        });
    }, observerOptions);

    // 观察需要动画的元素
    const animateElements = document.querySelectorAll(
        '.feature-card, .project-card, .stat-item'
    );

    animateElements.forEach(el => {
        observer.observe(el);
    });
}

// 项目卡片交互
function initProjectCards() {
    const projectCards = document.querySelectorAll('.project-card');

    projectCards.forEach(card => {
        // 演示按钮点击事件
        const demoBtn = card.querySelector('.btn-outline');
        if (demoBtn) {
            demoBtn.addEventListener('click', function(e) {
                e.preventDefault();
                const projectTitle = card.querySelector('.project-title').textContent;
                showDemoModal(projectTitle);
            });
        }

        // 购买按钮点击事件
        const buyBtn = card.querySelector('.btn-primary');
        if (buyBtn) {
            buyBtn.addEventListener('click', function(e) {
                e.preventDefault();
                const projectTitle = card.querySelector('.project-title').textContent;
                const projectPrice = card.querySelector('.project-price span').textContent;
                showPurchaseModal(projectTitle, projectPrice);
            });
        }

        // 卡片点击事件
        card.addEventListener('click', function(e) {
            // 如果点击的不是按钮，则跳转到项目详情页
            if (!e.target.closest('.btn')) {
                const projectTitle = this.querySelector('.project-title').textContent;
                console.log('查看项目详情:', projectTitle);
                // window.location.href = `/project/${projectId}`;
            }
        });
    });
}

// 显示演示模态框
function showDemoModal(projectTitle) {
    console.log('启动演示:', projectTitle);
    // 这里可以显示演示模态框或跳转到演示页面
    alert(`正在启动 "${projectTitle}" 的演示环境...`);
}

// 显示购买模态框
function showPurchaseModal(projectTitle, price) {
    console.log('购买项目:', projectTitle, price);
    // 这里可以显示购买确认模态框
    const confirmed = confirm(`确认购买 "${projectTitle}"？\n价格：${price}`);
    if (confirmed) {
        console.log('确认购买');
        // 执行购买逻辑
    }
}

// 统计数字动画
function initStatCounters() {
    const statNumbers = document.querySelectorAll('.stat-number');

    const animateCounter = (element, target) => {
        const duration = 2000; // 2秒
        const start = 0;
        const increment = target / (duration / 16); // 60fps
        let current = start;

        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }

            // 格式化数字显示
            if (typeof target === 'string' && target.includes('%')) {
                element.textContent = Math.floor(current) + '%';
            } else {
                element.textContent = Math.floor(current).toLocaleString();
            }
        }, 16);
    };

    // 使用Intersection Observer触发计数动画
    const counterObserver = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const element = entry.target;
                const text = element.textContent;

                // 提取数字
                let target;
                if (text.includes('%')) {
                    target = parseInt(text);
                } else {
                    target = parseInt(text.replace(/,/g, ''));
                }

                animateCounter(element, target);
                counterObserver.unobserve(element);
            }
        });
    }, { threshold: 0.5 });

    statNumbers.forEach(el => {
        counterObserver.observe(el);
    });
}

// 工具函数：防抖
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// 工具函数：节流
function throttle(func, limit) {
    let inThrottle;
    return function() {
        const args = arguments;
        const context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(() => inThrottle = false, limit);
        }
    }
}

// 现代化效果初始化
function initModernEffects() {
    // 为按钮添加涟漪效果
    addRippleEffect();

    // 添加鼠标跟随效果
    addMouseFollowEffect();

    // 添加卡片倾斜效果
    addCardTiltEffect();

    // 添加加载动画
    addLoadingEffects();
}

// 涟漪效果
function addRippleEffect() {
    const buttons = document.querySelectorAll('.btn');

    buttons.forEach(button => {
        button.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            const rect = this.getBoundingClientRect();
            const size = Math.max(rect.width, rect.height);
            const x = e.clientX - rect.left - size / 2;
            const y = e.clientY - rect.top - size / 2;

            ripple.style.cssText = `
                position: absolute;
                width: ${size}px;
                height: ${size}px;
                left: ${x}px;
                top: ${y}px;
                background: rgba(255, 255, 255, 0.3);
                border-radius: 50%;
                transform: scale(0);
                animation: ripple 0.6s ease-out;
                pointer-events: none;
            `;

            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);

            setTimeout(() => {
                ripple.remove();
            }, 600);
        });
    });
}

// 鼠标跟随效果
function addMouseFollowEffect() {
    const cursor = document.createElement('div');
    cursor.className = 'custom-cursor';
    cursor.style.cssText = `
        position: fixed;
        width: 20px;
        height: 20px;
        background: rgba(24, 144, 255, 0.3);
        border-radius: 50%;
        pointer-events: none;
        z-index: 9999;
        transition: transform 0.1s ease;
        transform: translate(-50%, -50%);
        display: none;
    `;
    document.body.appendChild(cursor);

    document.addEventListener('mousemove', (e) => {
        cursor.style.left = e.clientX + 'px';
        cursor.style.top = e.clientY + 'px';
        cursor.style.display = 'block';
    });

    document.addEventListener('mouseleave', () => {
        cursor.style.display = 'none';
    });

    // 悬停效果
    const interactiveElements = document.querySelectorAll('.btn, .project-card, .feature-card');
    interactiveElements.forEach(el => {
        el.addEventListener('mouseenter', () => {
            cursor.style.transform = 'translate(-50%, -50%) scale(1.5)';
            cursor.style.background = 'rgba(24, 144, 255, 0.5)';
        });

        el.addEventListener('mouseleave', () => {
            cursor.style.transform = 'translate(-50%, -50%) scale(1)';
            cursor.style.background = 'rgba(24, 144, 255, 0.3)';
        });
    });
}

// 卡片倾斜效果
function addCardTiltEffect() {
    const cards = document.querySelectorAll('.project-card, .feature-card');

    cards.forEach(card => {
        card.addEventListener('mousemove', (e) => {
            const rect = card.getBoundingClientRect();
            const x = e.clientX - rect.left;
            const y = e.clientY - rect.top;

            const centerX = rect.width / 2;
            const centerY = rect.height / 2;

            const rotateX = (y - centerY) / 10;
            const rotateY = (centerX - x) / 10;

            card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
        });

        card.addEventListener('mouseleave', () => {
            card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)';
        });
    });
}

// 加载效果
function addLoadingEffects() {
    // 为图片添加加载动画
    const images = document.querySelectorAll('img');
    images.forEach(img => {
        img.style.opacity = '0';
        img.style.transition = 'opacity 0.3s ease';

        img.addEventListener('load', () => {
            img.style.opacity = '1';
        });
    });
}

// 视差效果
function initParallaxEffects() {
    const parallaxElements = document.querySelectorAll('.hero, .code-window');

    window.addEventListener('scroll', throttle(() => {
        const scrolled = window.pageYOffset;

        parallaxElements.forEach(el => {
            const rate = scrolled * -0.5;
            el.style.transform = `translateY(${rate}px)`;
        });
    }, 16));
}

// 添加CSS类来支持动画
const style = document.createElement('style');
style.textContent = `
    .header {
        transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        will-change: transform, background, box-shadow;
    }

    .header.scrolled {
        box-shadow: var(--shadow-layered-md);
        background: rgba(255, 255, 255, 0.95);
    }

    .header.hidden {
        transform: translateY(-100%);
        transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    }

    .header:not(.hidden) {
        transform: translateY(0);
        transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    }

    /* 移动端优化 */
    @media (max-width: 768px) {
        .header.hidden {
            transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .header:not(.hidden) {
            transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
        }
    }

    /* 减少动画偏好的用户 */
    @media (prefers-reduced-motion: reduce) {
        .header {
            transition: none !important;
        }

        .header.hidden {
            transition: none !important;
        }
    }

    .animate-in {
        animation: fadeInUp 0.6s ease-out;
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(30px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    @keyframes ripple {
        to {
            transform: scale(2);
            opacity: 0;
        }
    }

    .project-card, .feature-card {
        transition: transform 0.3s ease;
    }

    @media (max-width: 991px) {
        .nav-menu.mobile-active,
        .nav-search.mobile-active {
            display: block;
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: rgba(255, 255, 255, 0.95);
            backdrop-filter: var(--glass-blur);
            border-top: 1px solid rgba(255, 255, 255, 0.2);
            padding: var(--spacing-md);
            box-shadow: var(--shadow-layered-md);
            border-radius: 0 0 var(--radius-lg) var(--radius-lg);
        }

        .nav-menu.mobile-active .nav-links {
            flex-direction: column;
            gap: var(--spacing-md);
            margin-left: 0;
        }

        .custom-cursor {
            display: none !important;
        }
    }

    @media (prefers-reduced-motion: reduce) {
        .project-card, .feature-card {
            transform: none !important;
        }

        .custom-cursor {
            display: none !important;
        }
    }
`;
document.head.appendChild(style);
