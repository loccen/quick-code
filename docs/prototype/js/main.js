// 速码网主页交互脚本

document.addEventListener('DOMContentLoaded', function() {
    // 初始化所有功能
    initMobileMenu();
    initSearchBox();
    initScrollEffects();
    initAnimations();
    initProjectCards();
    initStatCounters();
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
    
    window.addEventListener('scroll', function() {
        const currentScrollY = window.scrollY;
        
        // 头部阴影效果
        if (currentScrollY > 10) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
        
        // 头部隐藏/显示效果
        if (currentScrollY > lastScrollY && currentScrollY > 100) {
            header.classList.add('hidden');
        } else {
            header.classList.remove('hidden');
        }
        
        lastScrollY = currentScrollY;
    });
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

// 添加CSS类来支持动画
const style = document.createElement('style');
style.textContent = `
    .header.scrolled {
        box-shadow: var(--shadow-md);
    }
    
    .header.hidden {
        transform: translateY(-100%);
        transition: transform 0.3s ease;
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
    
    @media (max-width: 991px) {
        .nav-menu.mobile-active,
        .nav-search.mobile-active {
            display: block;
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border-top: 1px solid var(--border-light);
            padding: var(--spacing-md);
            box-shadow: var(--shadow-md);
        }
        
        .nav-menu.mobile-active .nav-links {
            flex-direction: column;
            gap: var(--spacing-md);
            margin-left: 0;
        }
    }
`;
document.head.appendChild(style);
