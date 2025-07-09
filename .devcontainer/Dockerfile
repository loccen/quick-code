# 基于官方Ubuntu镜像
FROM mcr.microsoft.com/vscode/devcontainers/base:ubuntu-22.04

# 设置环境变量
ENV DEBIAN_FRONTEND=noninteractive
ENV NODE_VERSION=20.18.0
ENV JAVA_VERSION=17
ENV MAVEN_VERSION=3.9.6

# 创建vscode用户（如果不存在）
ARG USERNAME=vscode
ARG USER_UID=1000
ARG USER_GID=$USER_UID

# 安装基础工具和依赖
RUN apt-get update && apt-get install -y \
    # 基础工具
    curl \
    wget \
    git \
    unzip \
    zip \
    tree \
    htop \
    vim \
    nano \
    jq \
    # 构建工具
    build-essential \
    pkg-config \
    # 网络工具
    net-tools \
    iputils-ping \
    telnet \
    # SSL证书
    ca-certificates \
    gnupg \
    lsb-release \
    # Python（某些工具需要）
    python3 \
    python3-pip \
    # 其他工具
    software-properties-common \
    apt-transport-https \
    && rm -rf /var/lib/apt/lists/*

# 安装Docker CLI
RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null \
    && apt-get update \
    && apt-get install -y docker-ce-cli \
    && rm -rf /var/lib/apt/lists/*

# 安装Node.js和npm
RUN curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
    && apt-get install -y nodejs

# 安装全局Node.js工具
RUN npm install -g \
    typescript \
    pm2 \
    serve

# 安装Java 17
RUN apt-get update && apt-get install -y openjdk-17-jdk \
    && rm -rf /var/lib/apt/lists/*

# 设置JAVA_HOME
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH=$JAVA_HOME/bin:$PATH

# 安装Maven
RUN wget https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
    && tar -xzf apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /opt \
    && ln -s /opt/apache-maven-${MAVEN_VERSION} /opt/maven \
    && rm apache-maven-${MAVEN_VERSION}-bin.tar.gz

# 设置Maven环境变量
ENV MAVEN_HOME=/opt/maven
ENV PATH=$MAVEN_HOME/bin:$PATH

# 安装Gradle（可选）
RUN wget https://services.gradle.org/distributions/gradle-8.5-bin.zip \
    && unzip gradle-8.5-bin.zip -d /opt \
    && ln -s /opt/gradle-8.5 /opt/gradle \
    && rm gradle-8.5-bin.zip

# 设置Gradle环境变量
ENV GRADLE_HOME=/opt/gradle
ENV PATH=$GRADLE_HOME/bin:$PATH

# 安装数据库客户端工具
RUN apt-get update && apt-get install -y \
    mysql-client \
    redis-tools \
    && rm -rf /var/lib/apt/lists/*

# 安装MinIO客户端
RUN wget https://dl.min.io/client/mc/release/linux-amd64/mc \
    && chmod +x mc \
    && mv mc /usr/local/bin/

# 创建工作目录
WORKDIR /workspace

# 设置用户权限
RUN if [ "$USER_GID" != "1000" ] || [ "$USER_UID" != "1000" ]; then \
        groupmod --gid $USER_GID $USERNAME \
        && usermod --uid $USER_UID --gid $USER_GID $USERNAME \
        && chown -R $USER_UID:$USER_GID /home/$USERNAME; \
    fi

# 为vscode用户设置Maven和Gradle目录权限
RUN mkdir -p /home/$USERNAME/.m2 \
    && mkdir -p /home/$USERNAME/.gradle \
    && chown -R $USERNAME:$USERNAME /home/$USERNAME/.m2 \
    && chown -R $USERNAME:$USERNAME /home/$USERNAME/.gradle

# Oh My Zsh 将通过 devcontainer features 安装，跳过手动安装
USER $USERNAME

# 配置Git（基础配置，用户可以覆盖）
RUN git config --global init.defaultBranch main \
    && git config --global core.autocrlf input \
    && git config --global pull.rebase false

# 验证npm安装
RUN npm --version

# 创建Maven目录
RUN mkdir -p /home/$USERNAME/.m2

# 切换回root用户进行最后的配置
USER root

# 安装一些有用的开发工具
RUN apt-get update && apt-get install -y \
    # 性能监控工具
    iotop \
    iftop \
    # 文本处理工具
    ripgrep \
    fd-find \
    # 其他实用工具
    tmux \
    screen \
    && rm -rf /var/lib/apt/lists/*

# 创建常用的开发目录
RUN mkdir -p /workspace/user-frontend \
    && mkdir -p /workspace/admin-frontend \
    && mkdir -p /workspace/backend \
    && mkdir -p /workspace/shared \
    && mkdir -p /workspace/scripts \
    && mkdir -p /workspace/docs \
    && chown -R $USERNAME:$USERNAME /workspace

# 设置默认用户
USER $USERNAME

# 设置工作目录
WORKDIR /workspace

# 默认命令
CMD ["sleep", "infinity"]
