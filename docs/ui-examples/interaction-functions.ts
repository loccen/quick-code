/**
 * 速码网现代化UI交互函数库
 * 包含涟漪效果、卡片倾斜、滚动动画等微交互实现
 */

import { ref, onMounted, onUnmounted } from 'vue';

// ========== 涟漪效果实现 ==========

/**
 * 添加涟漪效果到元素
 * @param element - 目标元素
 * @param event - 鼠标事件
 */
export function addRippleEffect(element: HTMLElement, event: MouseEvent) {
  const ripple = document.createElement('span');
  const rect = element.getBoundingClientRect();
  const size = Math.max(rect.width, rect.height);
  const x = event.clientX - rect.left - size / 2;
  const y = event.clientY - rect.top - size / 2;
  
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
  
  element.style.position = 'relative';
  element.style.overflow = 'hidden';
  element.appendChild(ripple);
  
  setTimeout(() => {
    ripple.remove();
  }, 600);
}

/**
 * Vue组合式函数：涟漪效果
 */
export function useRipple() {
  const handleRipple = (event: MouseEvent) => {
    const target = event.currentTarget as HTMLElement;
    addRippleEffect(target, event);
  };
  
  return { handleRipple };
}

// ========== 卡片3D倾斜效果 ==========

/**
 * Vue组合式函数：卡片倾斜效果
 */
export function useCardTilt() {
  const handleMouseMove = (event: MouseEvent) => {
    const card = event.currentTarget as HTMLElement;
    const rect = card.getBoundingClientRect();
    const x = event.clientX - rect.left;
    const y = event.clientY - rect.top;
    
    const centerX = rect.width / 2;
    const centerY = rect.height / 2;
    
    const rotateX = (y - centerY) / 10;
    const rotateY = (centerX - x) / 10;
    
    card.style.transform = `perspective(1000px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) translateZ(10px)`;
  };
  
  const handleMouseLeave = (event: MouseEvent) => {
    const card = event.currentTarget as HTMLElement;
    card.style.transform = 'perspective(1000px) rotateX(0) rotateY(0) translateZ(0)';
  };
  
  return { handleMouseMove, handleMouseLeave };
}

// ========== 数字计数动画 ==========

/**
 * Vue组合式函数：数字计数动画
 */
export function useCounterAnimation() {
  const animateCounter = (element: HTMLElement, target: number, duration = 2000) => {
    const start = 0;
    const increment = target / (duration / 16); // 60fps
    let current = start;
    
    const timer = setInterval(() => {
      current += increment;
      if (current >= target) {
        current = target;
        clearInterval(timer);
      }
      
      element.textContent = Math.floor(current).toLocaleString();
    }, 16);
  };
  
  return { animateCounter };
}

// ========== 滚动触发动画 ==========

/**
 * Vue组合式函数：滚动触发动画
 */
export function useScrollAnimation() {
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };
  
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
      }
    });
  }, observerOptions);
  
  const observeElement = (element: HTMLElement) => {
    observer.observe(element);
  };
  
  return { observeElement };
}

// ========== 智能导航栏滚动控制 ==========

/**
 * Vue组合式函数：智能导航栏滚动控制
 */
export function useSmartNavigation() {
  const isScrolled = ref(false);
  const isHidden = ref(false);
  
  let lastScrollY = 0;
  let scrollHistory: number[] = [];
  let scrollTimeout: number | null = null;
  
  const SCROLL_THRESHOLD = 15;
  const HIDE_THRESHOLD = 100;
  const DEBOUNCE_DELAY = 80;
  const DIRECTION_HISTORY_LENGTH = 3;
  
  const getStableScrollDirection = (currentScrollY: number) => {
    const currentDirection = currentScrollY > lastScrollY ? 1 : -1;
    
    scrollHistory.push(currentDirection);
    if (scrollHistory.length > DIRECTION_HISTORY_LENGTH) {
      scrollHistory.shift();
    }
    
    if (scrollHistory.length >= DIRECTION_HISTORY_LENGTH) {
      const isDirectionStable = scrollHistory.every(dir => dir === currentDirection);
      return isDirectionStable ? currentDirection : 0;
    }
    
    return 0;
  };
  
  const handleScroll = () => {
    const currentScrollY = window.scrollY;
    const scrollDifference = Math.abs(currentScrollY - lastScrollY);
    
    if (scrollDifference < SCROLL_THRESHOLD) {
      return;
    }
    
    // 阴影效果
    isScrolled.value = currentScrollY > 10;
    
    // 隐藏/显示效果
    const stableDirection = getStableScrollDirection(currentScrollY);
    
    if (stableDirection !== 0) {
      const shouldHide = stableDirection === 1 && currentScrollY > HIDE_THRESHOLD;
      const shouldShow = stableDirection === -1 || currentScrollY <= HIDE_THRESHOLD;
      
      if (shouldHide && !isHidden.value) {
        isHidden.value = true;
      } else if (shouldShow && isHidden.value) {
        isHidden.value = false;
      }
    }
    
    lastScrollY = currentScrollY;
  };
  
  const onScroll = () => {
    if (scrollTimeout) {
      clearTimeout(scrollTimeout);
    }
    
    // 立即处理阴影效果
    isScrolled.value = window.scrollY > 10;
    
    // 防抖处理隐藏/显示效果
    scrollTimeout = setTimeout(handleScroll, DEBOUNCE_DELAY);
  };
  
  onMounted(() => {
    window.addEventListener('scroll', onScroll, { passive: true });
  });
  
  onUnmounted(() => {
    window.removeEventListener('scroll', onScroll);
    if (scrollTimeout) {
      clearTimeout(scrollTimeout);
    }
  });
  
  return { isScrolled, isHidden };
}

// ========== 鼠标跟随效果 ==========

/**
 * Vue组合式函数：鼠标跟随效果
 */
export function useMouseFollowEffect() {
  onMounted(() => {
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
    
    const handleMouseMove = (e: MouseEvent) => {
      cursor.style.left = e.clientX + 'px';
      cursor.style.top = e.clientY + 'px';
      cursor.style.display = 'block';
    };
    
    const handleMouseLeave = () => {
      cursor.style.display = 'none';
    };
    
    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseleave', handleMouseLeave);
    
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
    
    onUnmounted(() => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseleave', handleMouseLeave);
      cursor.remove();
    });
  });
}

// ========== 视差滚动效果 ==========

/**
 * Vue组合式函数：视差滚动效果
 */
export function useParallaxEffect() {
  const throttle = (func: Function, limit: number) => {
    let inThrottle: boolean;
    return function(this: any, ...args: any[]) {
      if (!inThrottle) {
        func.apply(this, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    }
  };
  
  onMounted(() => {
    const parallaxElements = document.querySelectorAll('.hero, .code-window');
    
    const handleScroll = throttle(() => {
      const scrolled = window.pageYOffset;
      
      parallaxElements.forEach(el => {
        const rate = scrolled * -0.5;
        (el as HTMLElement).style.transform = `translateY(${rate}px)`;
      });
    }, 16);
    
    window.addEventListener('scroll', handleScroll, { passive: true });
    
    onUnmounted(() => {
      window.removeEventListener('scroll', handleScroll);
    });
  });
}

// ========== 加载动画效果 ==========

/**
 * Vue组合式函数：加载动画效果
 */
export function useLoadingEffects() {
  const addLoadingAnimation = () => {
    // 为图片添加加载动画
    const images = document.querySelectorAll('img');
    images.forEach(img => {
      img.style.opacity = '0';
      img.style.transition = 'opacity 0.3s ease';
      
      img.addEventListener('load', () => {
        img.style.opacity = '1';
      });
    });
  };
  
  onMounted(() => {
    addLoadingAnimation();
  });
  
  return { addLoadingAnimation };
}

// ========== 工具函数 ==========

/**
 * 防抖函数
 */
export function debounce(func: Function, wait: number) {
  let timeout: number;
  return function executedFunction(...args: any[]) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

/**
 * 节流函数
 */
export function throttle(func: Function, limit: number) {
  let inThrottle: boolean;
  return function(this: any, ...args: any[]) {
    if (!inThrottle) {
      func.apply(this, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  }
}
