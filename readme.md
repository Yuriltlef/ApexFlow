# ApexFlow - 电商信息管理系统

![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.x-42b883.svg)
![Java](https://img.shields.io/badge/Java-17-007396.svg)
![Tomcat](https://img.shields.io/badge/Tomcat-9.x-F8DC75.svg)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1.svg)
![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)

## 📋 项目简介

**ApexFlow** 是一个开源的电商信息管理系统，专为中小型电商商家设计。系统采用前后端分离架构，前端使用Vue 3.x，后端基于Java Servlet技术，由Tomcat直接托管部署，无需Nginx等反向代理服务器。项目遵循GPL-3.0开源协议，完全免费使用和修改。

### ✨ 核心特性

- **一体化部署**：前后端统一由Tomcat托管，简化部署流程
- **模块化管理**：六大核心功能模块，覆盖电商运营全流程
- **现代化界面**：基于Vue 3 + Element Plus的响应式设计
- **RESTful API**：标准化接口设计，便于集成和扩展
- **开源自由**：GPL-3.0协议，支持二次开发和商业应用

## 🗂️ 功能模块

| 模块 | 核心功能 | 状态 |
|------|----------|------|
| **订单管理** | 订单创建/查询/筛选，订单状态跟踪，批量操作 | ✅ 已完成 |
| **物流管理** | 物流公司对接，运单管理，物流轨迹查询 | ✅ 已完成 |
| **售后管理** | 退货/换货申请处理，退款管理，售后记录 | ✅ 已完成 |
| **评价管理** | 商品评价查看，回复评价，评价数据分析 | 🚧 开发中 |
| **仓库管理** | 库存管理，入库/出库记录，库存预警 | 🚧 开发中 |
| **财务管理** | 收支统计，财务报表，账单管理 | 🔄 规划中 |

## 🛠️ 技术架构

### 技术栈

**前端 (Vue 3.x)**
- **框架**: Vue 3.x (组合式API)
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite
- **样式预处理**: SCSS

**后端 (Java Servlet)**
- **服务器**: Apache Tomcat 9.x
- **Java版本**: JDK 8+
- **Web框架**: Servlet 4.0
- **数据库**: MySQL 5.7+
- **连接池**: HikariCP
- **JSON处理**: Jackson
- **日志框架**: Log4j2

**开发工具**
- **IDE**: IntelliJ IDEA / Eclipse + VS Code
- **构建工具**: Maven 3.6+
- **数据库工具**: MySQL Workbench
- **版本控制**: Git

### 架构图

```
┌─────────────────────────────────────────┐
│           浏览器客户端 (Vue SPA)          │
└─────────────────┬───────────────────────┘
                  │ HTTP/HTTPS
┌─────────────────▼───────────────────────┐
│            Apache Tomcat 9.x            │
│   ┌─────────────────────────────────┐   │
│   │      apexflow-web (静态资源)     │   │
│   │  ├── index.html                 │   │
│   │  ├── static/                    │   │
│   │  └── assets/                    │   │
│   │                                 │   │
│   │      apexflow-server (Java)     │   │
│   │  ├── Servlet Controllers        │   │
│   │  ├── DAO/Service 层             │   │
│   │  └── Filters/Listeners          │   │
│   └─────────────────────────────────┘   │
│                  │                      │
│           ┌──────▼───────┐              │
│           │    MySQL     │              │
│           │   数据库      │              │
│           └──────────────┘              │
└─────────────────────────────────────────┘
```

## 📁 项目结构

```
apexflow/ (项目根目录)
├── apexflow-web/              # Vue前端源码（独立开发）
│   ├── public/
│   ├── src/
│   │   ├── api/              # API接口封装
│   │   ├── assets/           # 静态资源
│   │   ├── components/       # 组件
│   │   ├── router/           # 路由配置
│   │   ├── stores/           # Pinia状态管理
│   │   ├── views/            # 页面组件
│   │   └── utils/            # 工具函数
│   ├── package.json
│   └── vue.config.js         # 构建配置
│
└── apexflow-server/          # Java Web项目
    ├── src/
    │   ├── main/
    │   │   ├── java/
    │   │   │   └── com/
    │   │   │       └── apex/
    │   │   │           ├── config/           # 配置类
    │   │   │           │   ├── CORSFilter.java
    │   │   │           │   ├── SpaFilter.java
    │   │   │           │   └── DatabaseConfig.java
    │   │   │           ├── core/             # 核心业务
    │   │   │           │   ├── controller/   # 控制器层
    │   │   │           │   │   ├── OrderController.java
    │   │   │           │   │   ├── LogisticsController.java
    │   │   │           │   │   ├── AfterSalesController.java
    │   │   │           │   │   └── ...
    │   │   │           │   ├── service/      # 业务逻辑层
    │   │   │           │   ├── dao/          # 数据访问层
    │   │   │           │   ├── dto/          # 数据传输对象
    │   │   │           │   └── model/        # 数据模型
    │   │   │           └── util/             # 工具类
    │   │   ├── resources/
    │   │   │   ├── config.properties         # 配置文件
    │   │   │   ├── log4j2.xml               # 日志配置
    │   │   │   └── db.properties            # 数据库配置
    │   │   └── webapp/                      # Web应用目录
    │   │       ├── WEB-INF/
    │   │       │   ├── web.xml              # Web配置
    │   │       │   └── classes/
    │   │       ├── index.jsp                 # 跳转页面
    │   │       ├── index.html                # Vue入口页面
    │   │       └── static/                   # Vue构建的静态资源
    │   │           ├── css/
    │   │           ├── js/
    │   │           ├── fonts/
    │   │           └── img/
    │   └── test/
    │       └── java/
    ├── lib/                    # 第三方依赖库
    ├── pom.xml                # Maven配置
    └── README.md
```

## 🚀 快速开始

### 环境要求

- **Java**: JDK 17 或更高版本
- **Tomcat**: Apache Tomcat 9.x
- **MySQL**: MySQL 8.0 或更高版本
- **Node.js**: 16.x 或更高版本
- **Maven**: 3.6+（用于后端构建）

### 1. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE apexflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 创建用户并授权（可选）
CREATE USER 'apexflow'@'localhost' IDENTIFIED BY 'apexflow123';
GRANT ALL PRIVILEGES ON apexflow.* TO 'apexflow'@'localhost';
FLUSH PRIVILEGES;

-- 导入表结构（SQL文件位于 apexflow-server/src/main/resources/db/）
-- mysql -u root -p apexflow < apexflow-server/src/main/resources/db/schema.sql
```

### 2. 配置文件设置

**数据库配置** (`apexflow-server/src/main/resources/db.properties`):

```properties
# 数据库连接配置
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/apexflow?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
db.username=root
db.password=yourpassword
db.initialSize=5
db.maxActive=20
db.maxWait=60000
```

### 3. 项目构建与部署

#### 方案一：一键构建部署

```bash
# 1. 克隆项目
git clone https://github.com/yourusername/apexflow.git
cd apexflow

# 2. 安装前端依赖并构建
cd apexflow-web
npm install
npm run build  # 构建到后端项目的webapp目录

# 3. 构建后端项目
cd ../apexflow-server
mvn clean package

# 4. 部署到Tomcat
# 将target/apexflow.war复制到Tomcat的webapps目录
cp target/apexflow.war /path/to/tomcat/webapps/

# 5. 启动Tomcat
/path/to/tomcat/bin/startup.sh
```

#### 方案二：开发模式运行

```bash
# 1. 启动后端服务（在apexflow-server目录）
mvn tomcat7:run  # 或使用IDE启动

# 2. 启动前端开发服务器（在apexflow-web目录）
npm run serve

# 3. 访问应用
# 前端开发地址：http://localhost:3000
# 后端API地址：http://localhost:8080
```

### 4. 访问系统

1. 启动Tomcat服务器
2. 访问应用地址：http://localhost:8080/apexflow
3. 使用默认管理员账号登录：
    - 用户名：admin
    - 密码：admin123

## 🔧 配置文件说明

### 前端Vue配置 (`apexflow-web/vue.config.js`)

```javascript
const path = require('path');

module.exports = {
  // 构建输出到Java项目的webapp目录
  outputDir: path.resolve(__dirname, '../apexflow-server/src/main/webapp'),
  
  // 静态资源路径（相对路径，适应Tomcat部署）
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',
  
  // 开发服务器代理配置
  devServer: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080/apexflow',
        changeOrigin: true,
        pathRewrite: {
          '^/api': ''
        }
      }
    }
  },
  
  // 生产环境配置
  productionSourceMap: false,
  css: {
    extract: true,
    sourceMap: false
  }
};
```

### Web应用配置 (`apexflow-server/src/main/webapp/WEB-INF/web.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee 
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <display-name>ApexFlow</display-name>
    
    <!-- 字符编码过滤器 -->
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.apache.catalina.filters.SetCharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
    <!-- CORS跨域过滤器 -->
    <filter>
        <filter-name>corsFilter</filter-name>
        <filter-class>com.apex.config.CORSFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>corsFilter</filter-name>
        <url-pattern>/api/*</url-pattern>
    </filter-mapping>
    
    <!-- SPA应用过滤器 -->
    <filter>
        <filter-name>spaFilter</filter-name>
        <filter-class>com.apex.config.SpaFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>spaFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
    <!-- 默认首页 -->
    <welcome-file-list>
        <welcome-file>index.jsp</welcome-file>
        <welcome-file>index.html</welcome-file>
    </welcome-file-list>
    
    <!-- 错误页面配置 -->
    <error-page>
        <error-code>404</error-code>
        <location>/index.html</location>
    </error-page>
    
    <session-config>
        <session-timeout>30</session-timeout>
    </session-config>
</web-app>
```

## 📊 数据库设计

主要数据表示例：

```sql
-- 订单表
CREATE TABLE `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
  `customer_id` BIGINT COMMENT '客户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待付款,2:待发货,3:已发货,4:已完成,5:已取消)',
  `payment_status` TINYINT COMMENT '支付状态',
  `shipping_address` TEXT COMMENT '收货地址',
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  INDEX `idx_customer_id` (`customer_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 商品表
CREATE TABLE `products` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `product_no` VARCHAR(50) NOT NULL COMMENT '商品编号',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT COMMENT '分类ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:上架,2:下架)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_no` (`product_no`),
  INDEX `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';
```

完整数据库脚本请查看 `apexflow-server/src/main/resources/db/` 目录。

## 🔌 用户API接口文档（用户模块）

基于现有数据库表 `apexflow_system_user` 实现四个API，不创建新表，不返回冗余信息。

### 1. 用户登录
**POST** `/api/auth/login`

#### 流程
```
客户端 (HTTPS) → 后端接收明文密码 → 查询用户 → 加盐哈希验证 → 返回Token
```

#### 请求
```json
{
  "username": "admin",
  "password": "your_password"
}
```

#### 成功响应
```json
{
  "success": true,
  "data": {
    "token": "jwt_token_here",
    "user": {
      "id": 1,
      "username": "admin",
      "realName": "系统管理员",
      "isAdmin": true
    }
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "用户名或密码错误"
}
```

---

### 2. 获取用户权限
**GET** `/api/user/permissions`

#### 流程
```
客户端带Token → Token验证 → 查询数据库 → 返回权限字段
```

#### 请求头
```
Authorization: Bearer jwt_token_here
```

#### 响应
```json
{
  "success": true,
  "data": {
    "isAdmin": true,
    "canManageOrder": true,
    "canManageLogistics": true,
    "canManageAfterSales": true,
    "canManageReview": true,
    "canManageInventory": true,
    "canManageIncome": true
  }
}
```

---

### 3. 修改个人信息
**PUT** `/api/user/profile`

#### 流程
```
客户端带Token和更新数据 → Token验证 → 数据验证 → 更新数据库
```

#### 请求
```json
{
  "realName": "新姓名",
  "email": "new@email.com",
  "phone": "13800138001"
}
```
*至少提供一个字段*

#### 响应
```json
{
  "success": true,
  "data": {
    "id": 1,
    "realName": "新姓名",
    "email": "new@email.com",
    "phone": "13800138001"
  }
}
```

---

### 4. 用户登出
**POST** `/api/auth/logout`

#### 流程
```
客户端带Token → Token验证 → 客户端丢弃Token（无状态实现）
```

#### 响应
```json
{
  "success": true
}
```

## 🔌 API接口文档（财务收支模块）

### 1. 创建财务记录
**POST** `/api/income`

#### 流程
```
客户端 (HTTPS) → 验证Token → 验证输入数据 → 创建财务记录 → 返回创建结果
```

#### 请求头
```
Authorization: Bearer jwt_token_here
Content-Type: application/json
```

#### 请求
```json
{
  "orderId": "ORDER20231201001",
  "type": "income",
  "amount": 7999.00,
  "paymentMethod": "alipay",
  "status": 2,
  "transactionTime": "2023-12-01 09:05:00",
  "remark": "iPhone 14 Pro销售款"
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "财务记录创建成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "type": "income",
    "amount": 7999.00,
    "paymentMethod": "alipay",
    "status": 2,
    "transactionTime": "2023-12-01 09:05:00",
    "remark": "iPhone 14 Pro销售款"
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "输入数据验证失败",
  "data": {
    "orderId": "订单号不能为空"
  }
}
```

**权限要求**: 管理员或财务管理权限

---

### 2. 获取财务记录详情
**GET** `/api/income/{id}`

#### 流程
```
客户端 → 验证Token → 查询数据库 → 返回记录详情
```

#### 请求头
```
Authorization: Bearer jwt_token_here
```

#### 响应
```json
{
  "success": true,
  "message": "财务记录详情获取成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "type": "income",
    "amount": 7999.00,
    "paymentMethod": "alipay",
    "status": 2,
    "transactionTime": "2023-12-01 09:05:00",
    "remark": "iPhone 14 Pro销售款"
  }
}
```

**权限要求**: 管理员或财务管理权限

---

### 3. 获取财务记录列表
**GET** `/api/income/list?page=1&pageSize=20&type=income&status=2`

#### 查询参数
- `page`: 页码（默认1）
- `pageSize`: 每页记录数（默认20，最大100）
- `type`: 财务类型（income/refund）
- `status`: 状态（1-待入账，2-已入账）

#### 流程
```
客户端 → 验证Token → 查询数据库 → 返回分页列表
```

#### 响应
```json
{
  "success": true,
  "message": "财务记录列表获取成功",
  "data": {
    "incomes": [
      {
        "id": 1,
        "orderId": "ORDER20231201001",
        "type": "income",
        "amount": 7999.00,
        "paymentMethod": "alipay",
        "status": 2,
        "transactionTime": "2023-12-01 09:05:00",
        "remark": "iPhone 14 Pro销售款"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalIncome": 150000.00,
    "totalRefund": 5000.00,
    "netIncome": 145000.00
  }
}
```

**权限要求**: 管理员或财务管理权限

---

### 4. 更新财务记录
**PUT** `/api/income/{id}`

#### 流程
```
客户端 → 验证Token → 检查记录存在 → 更新记录 → 返回更新结果
```

#### 请求
```json
{
  "amount": 8500.00,
  "status": 2,
  "remark": "更新后的备注"
}
```

#### 响应
```json
{
  "success": true,
  "message": "财务记录更新成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "type": "income",
    "amount": 8500.00,
    "paymentMethod": "alipay",
    "status": 2,
    "transactionTime": "2023-12-01 09:05:00",
    "remark": "更新后的备注"
  }
}
```

**权限要求**: 管理员或财务管理权限

---

### 5. 删除财务记录
**DELETE** `/api/income/{id}`

#### 流程
```
客户端 → 验证Token → 检查记录存在 → 删除记录 → 返回删除结果
```

#### 响应
```json
{
  "success": true,
  "message": "财务记录删除成功"
}
```

**权限要求**: 管理员或财务管理权限

---

### 6. 更新财务状态
**PUT** `/api/income/{id}/status`

#### 流程
```
客户端 → 验证Token → 验证状态值 → 更新状态 → 返回结果
```

#### 请求
```json
{
  "status": 2
}
```

#### 响应
```json
{
  "success": true,
  "message": "财务状态更新成功"
}
```

**权限要求**: 管理员或财务管理权限

---

### 7. 获取财务统计
**GET** `/api/income/statistics`

#### 流程
```
客户端 → 验证Token → 查询统计信息 → 返回统计数据
```

#### 响应
```json
{
  "success": true,
  "message": "财务统计获取成功",
  "data": {
    "totalIncome": 150000.00,
    "totalRefund": 5000.00,
    "netIncome": 145000.00
  }
}
```

**权限要求**: 管理员或财务管理权限

---

### 8. 根据订单号查询财务记录
**GET** `/api/income/order/{orderId}`

#### 流程
```
客户端 → 验证Token → 查询订单相关记录 → 返回记录列表
```

#### 响应
```json
{
  "success": true,
  "message": "订单财务记录获取成功",
  "data": [
    {
      "id": 1,
      "orderId": "ORDER20231201001",
      "type": "income",
      "amount": 7999.00,
      "paymentMethod": "alipay",
      "status": 2,
      "transactionTime": "2023-12-01 09:05:00",
      "remark": "iPhone 14 Pro销售款"
    }
  ]
}
```

**权限要求**: 管理员或财务管理权限

### 🔐 权限控制

所有财务相关API均使用`@RequirePermission`注解进行权限验证，需要以下任意一种权限：
- 管理员权限 (`Permission.ADMIN`)
- 财务管理权限 (`Permission.INCOME_MANAGE`)

权限验证逻辑为OR（任意满足一个即可），通过`RequirePermission.LogicType.OR`配置。

### 📊 数据库字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | INT | 财务记录ID，主键 |
| order_id | VARCHAR(50) | 关联订单号 |
| type | VARCHAR(20) | 类型：income-收入，refund-退款 |
| amount | DECIMAL(10,2) | 金额（正数为收入，负数为退款） |
| payment_method | VARCHAR(20) | 支付方式 |
| status | TINYINT | 状态：1-待入账，2-已入账 |
| transaction_time | DATETIME | 交易时间 |
| remark | VARCHAR(200) | 备注 |


## 🔌 评价管理API文档（评价管理模块）

### 1. 创建评价
**POST** `/api/review`

#### 流程
```
客户端 → 验证Token和权限 → 验证请求数据 → 创建评价 → 返回结果
```

### 权限要求
- 管理员权限 (`Permission.ADMIN`)
- 订单管理权限 (`Permission.ORDER_MANAGE`)

### 请求示例
```json
{
  "orderId": "ORDER20231201001",
  "productId": 1,
  "userId": 1001,
  "rating": 5,
  "content": "商品质量很好，物流很快",
  "images": "image1.jpg,image2.jpg",
  "anonymous": false
}
```

### 成功响应
```json
{
  "success": true,
  "message": "评价创建成功",
  "data": {
    "id": 101,
    "orderId": "ORDER20231201001",
    "productId": 1,
    "userId": 1001,
    "rating": 5,
    "content": "商品质量很好，物流很快",
    "images": "image1.jpg,image2.jpg",
    "anonymous": false,
    "createdAt": "2023-12-01T10:30:00"
  }
}
```

### 失败响应
```json
{
  "success": false,
  "message": "该订单已评价",
  "errorCode": "REVIEW_EXISTS"
}
```

---

### 2. 获取评价列表
**GET** `/api/review?productId=1&page=1&pageSize=20`

### 流程
```
客户端 → 验证Token和权限 → 查询数据库 → 返回分页列表
```

### 查询参数
- `productId` (可选): 商品ID
- `userId` (可选): 用户ID
- `page` (可选, 默认1): 页码
- `pageSize` (可选, 默认20, 最大100): 每页数量

### 响应示例
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "reviews": [
      {
        "id": 101,
        "orderId": "ORDER20231201001",
        "productId": 1,
        "userId": 1001,
        "rating": 5,
        "content": "商品质量很好",
        "images": "image1.jpg",
        "anonymous": false,
        "createdAt": "2023-12-01T10:30:00"
      }
    ],
    "total": 50,
    "page": 1,
    "pageSize": 20
  }
}
```

---

### 3. 获取评价详情
**GET** `/api/review/{id}`

### 响应示例
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "id": 101,
    "orderId": "ORDER20231201001",
    "productId": 1,
    "userId": 1001,
    "rating": 5,
    "content": "商品质量很好",
    "images": "image1.jpg",
    "anonymous": false,
    "createdAt": "2023-12-01T10:30:00"
  }
}
```

---

### 4. 获取评价统计
**GET** `/api/review/stats/{productId}`

### 响应示例
```json
{
  "success": true,
  "message": "获取成功",
  "data": {
    "averageRating": 4.5,
    "totalReviews": 120,
    "distribution": {
      "rating1": 5,
      "rating2": 10,
      "rating3": 15,
      "rating4": 40,
      "rating5": 50
    }
  }
}
```

---

### 5. 删除评价
**DELETE** `/api/review/{id}`

### 响应示例
```json
{
  "success": true,
  "message": "删除成功",
  "data": null
}
```

---

### 🔐 权限控制

所有API均使用`@RequirePermission`注解进行权限验证，需要以下任意一种权限：
- 管理员权限 (`Permission.ADMIN`)
- 订单管理权限 (`Permission.ORDER_MANAGE`)

权限验证逻辑为OR（任意满足一个即可），通过`RequirePermission.LogicType.OR`配置。

---

### 📊 数据库表说明

使用现有表 `apexflow_review`：
```sql
CREATE TABLE apexflow_review (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
    product_id INT NOT NULL COMMENT '商品ID',
    user_id INT NOT NULL COMMENT '评价用户ID',
    rating TINYINT NOT NULL COMMENT '评分：1-5星',
    content TEXT COMMENT '评价内容',
    images TEXT COMMENT '评价图片URL，多个用逗号分隔',
    is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名评价',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间'
) COMMENT='商品评价表';
```

## 🔌 API接口文档（订单管理模块）

### 1. 创建订单
**POST** `/api/orders`

#### 流程
```
客户端 (HTTPS) → 验证Token → 验证输入数据 → 创建订单主信息和订单项 → 返回创建结果
```

#### 请求头
```
Authorization: Bearer jwt_token_here
Content-Type: application/json
```

#### 请求示例
```json
{
  "userId": 1001,
  "totalAmount": 7999.00,
  "paymentMethod": "alipay",
  "addressId": 123,
  "orderItems": [
    {
      "productId": 1,
      "productName": "iPhone 14 Pro",
      "quantity": 1,
      "price": 7999.00,
      "subtotal": 7999.00
    }
  ]
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "订单创建成功",
  "data": {
    "orderId": "ORDER1701234567890",
    "totalAmount": 7999.00,
    "status": 1,
    "createdAt": "2023-12-01T09:00:00"
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "用户ID、总金额和订单项不能为空"
}
```

**权限要求**: 管理员或订单管理权限

---

### 2. 获取订单详情
**GET** `/api/orders/{orderId}`

#### 流程
```
客户端 → 验证Token → 查询数据库 → 返回订单详情
```

#### 请求头
```
Authorization: Bearer jwt_token_here
```

#### 响应示例
```json
{
  "success": true,
  "message": "订单详情获取成功",
  "data": {
    "orderId": "ORDER20231201001",
    "userId": 1001,
    "totalAmount": 7999.00,
    "status": 4,
    "paymentMethod": "alipay",
    "addressId": 123,
    "createdAt": "2023-12-01T09:00:00",
    "paidAt": "2023-12-01T09:05:00",
    "shippedAt": "2023-12-01T14:00:00",
    "completedAt": "2023-12-03T10:00:00",
    "orderItems": [
      {
        "id": 1,
        "orderId": "ORDER20231201001",
        "productId": 1,
        "productName": "iPhone 14 Pro",
        "quantity": 1,
        "price": 7999.00,
        "subtotal": 7999.00
      }
    ]
  }
}
```

**权限要求**: 管理员或订单管理权限

---

### 3. 获取订单列表
**GET** `/api/orders/list?page=1&pageSize=20&userId=1001&status=4`

#### 查询参数
- `page`: 页码（默认1）
- `pageSize`: 每页记录数（默认20，最大100）
- `userId`: 用户ID（可选）
- `status`: 订单状态（可选，1-待支付，2-已支付，3-已发货，4-已完成，5-已取消）

#### 流程
```
客户端 → 验证Token → 查询数据库 → 返回分页列表
```

#### 响应示例
```json
{
  "success": true,
  "message": "订单列表获取成功",
  "data": {
    "orders": [
      {
        "orderId": "ORDER20231201001",
        "userId": 1001,
        "totalAmount": 7999.00,
        "status": 4,
        "paymentMethod": "alipay",
        "createdAt": "2023-12-01T09:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50,
    "totalPages": 3
  }
}
```

**权限要求**: 管理员或订单管理权限

---

### 4. 更新订单信息
**PUT** `/api/orders/{orderId}`

#### 流程
```
客户端 → 验证Token → 检查订单存在 → 更新订单信息 → 返回更新结果
```

#### 请求示例
```json
{
  "addressId": 124,
  "paymentMethod": "wxpay"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "订单信息更新成功"
}
```

**权限要求**: 管理员或订单管理权限

---

### 5. 删除订单
**DELETE** `/api/orders/{orderId}`

#### 流程
```
客户端 → 验证Token → 检查订单存在 → 级联删除订单及相关记录 → 返回删除结果
```

#### 响应示例
```json
{
  "success": true,
  "message": "订单删除成功"
}
```

**权限要求**: 管理员或订单管理权限

---

### 6. 更新订单状态
**PUT** `/api/orders/{orderId}/status`

#### 流程
```
客户端 → 验证Token → 验证状态值 → 更新状态 → 返回结果
```

#### 请求示例
```json
{
  "status": 2
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "订单状态更新成功"
}
```

**权限要求**: 管理员或订单管理权限

### 📊 订单状态说明
| 状态码 | 状态说明 | 允许的操作 |
|--------|----------|------------|
| 1 | 待支付 | 更新订单信息、删除订单、更新状态为2或5 |
| 2 | 已支付 | 更新订单信息、删除订单、更新状态为3或5 |
| 3 | 已发货 | 更新状态为4 |
| 4 | 已完成 | 无状态更新操作 |
| 5 | 已取消 | 无状态更新操作 |

### 🔐 权限控制
所有订单相关API均使用`@RequirePermission`注解进行权限验证，需要以下任意一种权限：
- 管理员权限 (`Permission.ADMIN`)
- 订单管理权限 (`Permission.ORDER_MANAGE`)

权限验证逻辑为OR（任意满足一个即可），通过`RequirePermission.LogicType.OR`配置。

---

## 🔌 !!!! *管理员管理模块 API 文档*（系统设置模块）

### 1. 获取用户列表
**GET** `/api/admin/users`

#### 流程
```
客户端带Token → Token验证 → 管理员权限验证 → 查询数据库 → 返回分页用户列表
```

#### 查询参数
- `page` (可选, 默认1): 页码（从1开始）
- `pageSize` (可选, 默认20, 最大100): 每页记录数

#### 请求头
```
Authorization: Bearer jwt_token_here
```

#### 成功响应
```json
{
  "success": true,
  "message": "获取用户列表成功",
  "data": {
    "users": [
      {
        "id": 1,
        "username": "admin",
        "realName": "系统管理员",
        "email": "admin@apexflow.com",
        "phone": "13800138000",
        "isAdmin": true,
        "canManageOrder": true,
        "canManageLogistics": true,
        "canManageAfterSales": true,
        "canManageReview": true,
        "canManageInventory": true,
        "canManageIncome": true,
        "status": 1,
        "createdAt": "2023-12-01T09:00:00",
        "updatedAt": "2023-12-01T09:00:00",
        "lastLoginAt": "2023-12-01T10:30:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50,
    "totalPages": 3
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "需要管理员权限"
}
```

**权限要求**: 管理员权限 (`Permission.ADMIN`)

---

### 2. 搜索用户
**GET** `/api/admin/users/search`

#### 流程
```
客户端带Token → Token验证 → 管理员权限验证 → 模糊搜索数据库 → 返回搜索结果
```

#### 查询参数
- `keyword` (必需): 搜索关键词（用户名、真实姓名、邮箱、电话）
- `page` (可选, 默认1): 页码
- `pageSize` (可选, 默认20): 每页记录数

#### 请求头
```
Authorization: Bearer jwt_token_here
```

#### 响应示例
```json
{
  "success": true,
  "message": "搜索用户成功",
  "data": {
    "users": [
      {
        "id": 2,
        "username": "user1",
        "realName": "张三",
        "email": "zhangsan@example.com",
        "phone": "13800138001",
        "isAdmin": false,
        "canManageOrder": true,
        "canManageLogistics": false,
        "canManageAfterSales": false,
        "canManageReview": false,
        "canManageInventory": false,
        "canManageIncome": false,
        "status": 1,
        "createdAt": "2023-12-01T09:00:00",
        "updatedAt": "2023-12-01T09:00:00",
        "lastLoginAt": "2023-12-01T10:30:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 1,
    "totalPages": 1
  }
}
```

**权限要求**: 管理员权限 (`Permission.ADMIN`)

---

### 3. 更新用户信息
**PUT** `/api/admin/users/{id}`

#### 流程
```
客户端带Token和更新数据 → Token验证 → 管理员权限验证 → 验证输入数据 → 更新数据库
```

#### 请求头
```
Authorization: Bearer jwt_token_here
Content-Type: application/json
```

#### 请求示例
```json
{
  "realName": "李四",
  "email": "lisi@example.com",
  "phone": "13800138002",
  "status": 1
}
```

*至少提供一个字段*

### 响应示例
```json
{
  "success": true,
  "message": "更新用户信息成功",
  "data": {
    "id": 2,
    "username": "user1",
    "realName": "李四",
    "email": "lisi@example.com",
    "phone": "13800138002",
    "isAdmin": false,
    "canManageOrder": true,
    "canManageLogistics": false,
    "canManageAfterSales": false,
    "canManageReview": false,
    "canManageInventory": false,
    "canManageIncome": false,
    "status": 1,
    "createdAt": "2023-12-01T09:00:00",
    "updatedAt": "2023-12-02T10:30:00",
    "lastLoginAt": "2023-12-01T10:30:00"
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "邮箱已被其他用户使用"
}
```

**权限要求**: 管理员权限 (`Permission.ADMIN`)

---

### 4. 更新用户权限
**PUT** `/api/admin/users/{id}/permissions`

#### 流程
```
客户端带Token和权限数据 → Token验证 → 管理员权限验证 → 更新权限字段 → 返回更新结果
```

#### 请求头
```
Authorization: Bearer jwt_token_here
Content-Type: application/json
```

#### 请求示例
```json
{
  "isAdmin": false,
  "canManageOrder": true,
  "canManageLogistics": true,
  "canManageAfterSales": false,
  "canManageReview": false,
  "canManageInventory": true,
  "canManageIncome": false
}
```

*至少提供一个权限字段*

#### 响应示例
```json
{
  "success": true,
  "message": "更新用户权限成功",
  "data": {
    "userId": 2,
    "isAdmin": false,
    "canManageOrder": true,
    "canManageLogistics": true,
    "canManageAfterSales": false,
    "canManageReview": false,
    "canManageInventory": true,
    "canManageIncome": false
  }
}
```

**权限要求**: 管理员权限 (`Permission.ADMIN`)

---

### 5. 重置用户密码
**PUT** `/api/admin/users/{id}/password`

#### 流程
```
客户端带Token和新密码 → Token验证 → 管理员权限验证 → 密码强度验证 → 生成新盐哈希 → 更新数据库
```

#### 请求头
```
Authorization: Bearer jwt_token_here
Content-Type: application/json
```

#### 请求示例
```json
{
  "newPassword": "newpassword123"
}
```

*密码长度至少6位*

#### 响应示例
```json
{
  "success": true,
  "message": "重置密码成功",
  "data": {
    "success": true,
    "message": "密码重置成功",
    "userId": 2
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "密码长度不能少于6位"
}
```

**权限要求**: 管理员权限 (`Permission.ADMIN`)

---

### 📊 用户状态说明
| 状态码 | 状态说明 |
|--------|----------|
| 1 | 正常 |
| 0 | 禁用 |

### 🔐 权限控制
所有管理员API均使用`@RequirePermission`注解进行权限验证，需要管理员权限：
- 管理员权限 (`Permission.ADMIN`)

权限验证逻辑为OR（任意满足一个即可），通过`RequirePermission.LogicType.OR`配置。

---



## 🔌 售后服务API文档（售后管理模块）

### 1. 创建售后申请
**POST** `/api/after-sales`

#### 流程
```
客户端 (HTTPS) → 验证Token和权限 → 验证请求数据 → 创建售后记录 → 返回创建结果
```

#### 权限要求
- 管理员权限 (`Permission.ADMIN`)
- 售后管理权限 (`Permission.AFTER_SALES_MANAGE`)

#### 请求示例
```json
{
  "orderId": "ORDER20231201001",
  "type": 1,
  "reason": "商品质量问题",
  "refundAmount": 7999.00
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "售后申请创建成功",
  "data": {
    "id": 101,
    "orderId": "ORDER20231201001",
    "type": 1,
    "reason": "商品质量问题",
    "status": 1,
    "refundAmount": 7999.00,
    "applyTime": "2023-12-01T10:30:00",
    "processTime": null,
    "processRemark": null
  }
}
```

---

### 2. 获取售后详情
**GET** `/api/after-sales/{id}`

#### 流程
```
客户端 → 验证Token和权限 → 查询数据库 → 返回售后详情
```

#### 响应示例
```json
{
  "success": true,
  "message": "获取售后详情成功",
  "data": {
    "id": 101,
    "orderId": "ORDER20231201001",
    "type": 1,
    "reason": "商品质量问题",
    "status": 1,
    "refundAmount": 7999.00,
    "applyTime": "2023-12-01T10:30:00",
    "processTime": null,
    "processRemark": null
  }
}
```

---

### 3. 获取售后列表（分页）
**GET** `/api/after-sales?page=1&pageSize=20`

#### 查询参数
- `page` (可选, 默认1): 页码
- `pageSize` (可选, 默认20, 最大100): 每页大小

#### 响应示例
```json
{
  "success": true,
  "message": "获取售后列表成功",
  "data": {
    "data": [
      {
        "id": 101,
        "orderId": "ORDER20231201001",
        "type": 1,
        "reason": "商品质量问题",
        "status": 1,
        "refundAmount": 7999.00,
        "applyTime": "2023-12-01T10:30:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50
  }
}
```

---

### 4. 获取订单售后记录
**GET** `/api/after-sales/order/{orderId}`

#### 响应示例
```json
{
  "success": true,
  "message": "获取订单售后记录成功",
  "data": [
    {
      "id": 101,
      "orderId": "ORDER20231201001",
      "type": 1,
      "reason": "商品质量问题",
      "status": 1,
      "refundAmount": 7999.00,
      "applyTime": "2023-12-01T10:30:00"
    }
  ]
}
```

---

### 5. 根据状态获取售后列表
**GET** `/api/after-sales/status/{status}?page=1&pageSize=20`

#### 状态说明
- 1: 申请中
- 2: 审核通过
- 3: 审核拒绝
- 4: 已完成

#### 响应示例
```json
{
  "success": true,
  "message": "获取状态售后列表成功",
  "data": {
    "data": [
      {
        "id": 101,
        "orderId": "ORDER20231201001",
        "type": 1,
        "reason": "商品质量问题",
        "status": 1,
        "refundAmount": 7999.00,
        "applyTime": "2023-12-01T10:30:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 15
  }
}
```

---

### 6. 更新售后状态
**PUT** `/api/after-sales/{id}/status`

#### 请求示例
```json
{
  "status": 2,
  "remark": "审核通过，同意退款"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "更新售后状态成功",
  "data": {
    "id": 101,
    "orderId": "ORDER20231201001",
    "type": 1,
    "reason": "商品质量问题",
    "status": 2,
    "refundAmount": 7999.00,
    "applyTime": "2023-12-01T10:30:00",
    "processTime": "2023-12-01T14:00:00",
    "processRemark": "审核通过，同意退款"
  }
}
```

---

### 7. 删除售后记录
**DELETE** `/api/after-sales/{id}`

#### 响应示例
```json
{
  "success": true,
  "message": "删除售后记录成功",
  "data": null
}
```

---

### 8. 获取售后统计
**GET** `/api/after-sales/stats`

#### 响应示例
```json
{
  "success": true,
  "message": "获取售后统计成功",
  "data": {
    "totalCount": 50
  }
}
```
---

## 🔌 API接口文档（物流管理模块）

### 1. 创建物流信息
**POST** `/api/logistics`

#### 流程
```
客户端 (HTTPS) → 验证Token和权限 → 验证请求数据 → 创建物流记录 → 返回创建结果
```

#### 权限要求
- 管理员权限 (`Permission.ADMIN`)
- 物流管理权限 (`Permission.LOGISTICS_MANAGE`)

#### 请求示例
```json
{
  "orderId": "ORDER20231201051",
  "expressCompany": "顺丰速运",
  "trackingNumber": "SF202312015001",
  "senderAddress": "上海市浦东新区",
  "receiverAddress": "北京市朝阳区"
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "物流信息创建成功",
  "data": {
    "id": 51,
    "orderId": "ORDER20231201051",
    "expressCompany": "顺丰速运",
    "trackingNumber": "SF202312015001",
    "status": "pending",
    "senderAddress": "上海市浦东新区",
    "receiverAddress": "北京市朝阳区",
    "shippedAt": null,
    "deliveredAt": null,
    "createdAt": "2023-12-01T10:30:00"
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "该订单已有物流记录",
  "errorCode": "LOGISTICS_EXISTS"
}
```

---

### 2. 根据ID获取物流详情
**GET** `/api/logistics/{id}`

#### 流程
```
客户端 → 验证Token和权限 → 查询数据库 → 返回物流详情
```

#### 响应示例
```json
{
  "success": true,
  "message": "获取物流详情成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "expressCompany": "顺丰速运",
    "trackingNumber": "SF12345678901",
    "status": "delivered",
    "senderAddress": "上海市浦东新区",
    "receiverAddress": "北京市朝阳区",
    "shippedAt": "2023-12-01T14:00:00",
    "deliveredAt": "2023-12-03T10:00:00",
    "createdAt": "2023-12-01T14:00:00"
  }
}
```

---

### 3. 根据订单号获取物流信息
**GET** `/api/logistics/order/{orderId}`

#### 流程
```
客户端 → 验证Token和权限 → 查询数据库 → 返回物流信息
```

#### 响应示例
```json
{
  "success": true,
  "message": "获取订单物流信息成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "expressCompany": "顺丰速运",
    "trackingNumber": "SF12345678901",
    "status": "delivered",
    "senderAddress": "上海市浦东新区",
    "receiverAddress": "北京市朝阳区",
    "shippedAt": "2023-12-01T14:00:00",
    "deliveredAt": "2023-12-03T10:00:00",
    "createdAt": "2023-12-01T14:00:00"
  }
}
```

---

### 4. 获取物流列表（分页）
**GET** `/api/logistics?page=1&pageSize=20`

#### 查询参数
- `page` (可选, 默认1): 页码
- `pageSize` (可选, 默认20, 最大100): 每页大小

#### 响应示例
```json
{
  "success": true,
  "message": "获取物流列表成功",
  "data": {
    "data": [
      {
        "id": 1,
        "orderId": "ORDER20231201001",
        "expressCompany": "顺丰速运",
        "trackingNumber": "SF12345678901",
        "status": "delivered",
        "senderAddress": "上海市浦东新区",
        "receiverAddress": "北京市朝阳区",
        "shippedAt": "2023-12-01T14:00:00",
        "deliveredAt": "2023-12-03T10:00:00",
        "createdAt": "2023-12-01T14:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50,
    "totalPages": 3
  }
}
```

---

### 5. 获取待发货列表
**GET** `/api/logistics/pending?page=1&pageSize=20`

#### 查询参数
- `page` (可选, 默认1): 页码
- `pageSize` (可选, 默认20): 每页大小

#### 响应示例
```json
{
  "success": true,
  "message": "获取待发货列表成功",
  "data": {
    "data": [
      {
        "id": 5,
        "orderId": "ORDER20231201005",
        "expressCompany": null,
        "trackingNumber": null,
        "status": "pending",
        "senderAddress": null,
        "receiverAddress": null,
        "shippedAt": null,
        "deliveredAt": null,
        "createdAt": "2023-12-01T11:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 10,
    "totalPages": 1
  }
}
```

---

### 6. 获取运输中列表
**GET** `/api/logistics/in-transit?page=1&pageSize=20`

#### 响应示例
```json
{
  "success": true,
  "message": "获取运输中列表成功",
  "data": {
    "data": [
      {
        "id": 3,
        "orderId": "ORDER20231201003",
        "expressCompany": "圆通速递",
        "trackingNumber": "YT12345678901",
        "status": "shipped",
        "senderAddress": "广州市天河区",
        "receiverAddress": "深圳市南山区",
        "shippedAt": "2023-12-02T10:00:00",
        "deliveredAt": null,
        "createdAt": "2023-12-02T10:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 5,
    "totalPages": 1
  }
}
```

---

### 7. 更新物流信息
**PUT** `/api/logistics/{id}`

#### 请求示例
```json
{
  "orderId": "ORDER20231201001",
  "expressCompany": "京东物流",
  "trackingNumber": "JD12345678999",
  "senderAddress": "上海市浦东新区新地址",
  "receiverAddress": "北京市朝阳区新地址"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "更新物流信息成功",
  "data": {
    "id": 1,
    "orderId": "ORDER20231201001",
    "expressCompany": "京东物流",
    "trackingNumber": "JD12345678999",
    "status": "delivered",
    "senderAddress": "上海市浦东新区新地址",
    "receiverAddress": "北京市朝阳区新地址",
    "shippedAt": "2023-12-01T14:00:00",
    "deliveredAt": "2023-12-03T10:00:00",
    "createdAt": "2023-12-01T14:00:00"
  }
}
```

---

### 8. 更新物流状态
**PUT** `/api/logistics/{id}/status`

#### 请求示例
```json
{
  "status": "shipped"
}
```

#### 状态说明
- `pending`: 待发货
- `shipped`: 已发货
- `delivered`: 已送达

#### 响应示例
```json
{
  "success": true,
  "message": "更新物流状态成功",
  "data": {
    "id": 5,
    "orderId": "ORDER20231201005",
    "expressCompany": null,
    "trackingNumber": null,
    "status": "shipped",
    "senderAddress": null,
    "receiverAddress": null,
    "shippedAt": "2023-12-02T14:00:00",
    "deliveredAt": null,
    "createdAt": "2023-12-01T11:00:00"
  }
}
```

---

### 9. 更新发货信息
**PUT** `/api/logistics/{id}/shipping`

#### 请求示例
```json
{
  "expressCompany": "中通快递",
  "trackingNumber": "ZT987654321",
  "senderAddress": "上海市徐汇区"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "更新发货信息成功",
  "data": {
    "id": 5,
    "orderId": "ORDER20231201005",
    "expressCompany": "中通快递",
    "trackingNumber": "ZT987654321",
    "status": "shipped",
    "senderAddress": "上海市徐汇区",
    "receiverAddress": null,
    "shippedAt": "2023-12-02T14:00:00",
    "deliveredAt": null,
    "createdAt": "2023-12-01T11:00:00"
  }
}
```

---

### 10. 删除物流记录
**DELETE** `/api/logistics/{id}`

#### 响应示例
```json
{
  "success": true,
  "message": "删除物流记录成功",
  "data": null
}
```

---

### 11. 获取物流统计
**GET** `/api/logistics/stats`

#### 响应示例
```json
{
  "success": true,
  "message": "获取物流统计成功",
  "data": {
    "pendingCount": 10,
    "shippedCount": 5,
    "deliveredCount": 35
  }
}
```

---

### 🔐 权限控制

所有物流相关API均使用`@RequirePermission`注解进行权限验证，需要以下任意一种权限：
- 管理员权限 (`Permission.ADMIN`)
- 物流管理权限 (`Permission.LOGISTICS_MANAGE`)

权限验证逻辑为OR（任意满足一个即可），通过`RequirePermission.LogicType.OR`配置。

---

### 📊 数据库字段说明

使用现有表 `apexflow_logistics`：
```sql
CREATE TABLE apexflow_logistics (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '物流ID',
    order_id VARCHAR(50) NOT NULL UNIQUE COMMENT '关联订单号，一个订单一个物流',
    express_company VARCHAR(50) COMMENT '快递公司',
    tracking_number VARCHAR(100) COMMENT '运单号',
    status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待发货，shipped-已发货，delivered-已送达',
    sender_address VARCHAR(200) COMMENT '发货地址',
    receiver_address VARCHAR(200) COMMENT '收货地址',
    shipped_at DATETIME COMMENT '发货时间',
    delivered_at DATETIME COMMENT '送达时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='物流信息表';
```
---

## 🔌 API接口文档（库存管理模块）

### 1. 创建商品
**POST** `/api/inventory/products`

#### 流程
```
客户端 → 验证Token和权限 → 验证请求数据 → 创建商品记录 → 返回创建结果
```

#### 权限要求
- 管理员权限 (`Permission.ADMIN`)
- 库存管理权限 (`Permission.INVENTORY_MANAGE`)

#### 请求示例
```json
{
  "name": "小米14 Pro",
  "category": "手机",
  "price": 4999.00,
  "stock": 100,
  "status": 1,
  "image": "xiaomi14.jpg"
}
```

#### 成功响应
```json
{
  "success": true,
  "message": "商品创建成功",
  "data": {
    "id": 51,
    "name": "小米14 Pro",
    "category": "手机",
    "price": 4999.00,
    "stock": 100,
    "status": 1,
    "image": "xiaomi14.jpg",
    "createdAt": "2023-12-01T10:30:00"
  }
}
```

#### 失败响应
```json
{
  "success": false,
  "message": "商品名称和价格不能为空",
  "errorCode": "INVALID_REQUEST"
}
```

---

### 2. 获取商品详情
**GET** `/api/inventory/products/{id}`

#### 流程
```
客户端 → 验证Token和权限 → 查询数据库 → 返回商品详情
```

#### 响应示例
```json
{
  "success": true,
  "message": "商品详情获取成功",
  "data": {
    "id": 1,
    "name": "iPhone 14 Pro",
    "category": "手机",
    "price": 7999.00,
    "stock": 95,
    "status": 1,
    "image": "iphone14.jpg",
    "createdAt": "2023-12-01T10:00:00"
  }
}
```

---

### 3. 获取商品列表
**GET** `/api/inventory/products/list`

#### 查询参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| category | string | 否 | 商品分类筛选 |
| keyword | string | 否 | 商品名称搜索 |
| status | integer | 否 | 商品状态 (1-上架, 0-下架) |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20，最大100 |

#### 响应示例
```json
{
  "success": true,
  "message": "商品列表获取成功",
  "data": {
    "products": [
      {
        "id": 1,
        "name": "iPhone 14 Pro",
        "category": "手机",
        "price": 7999.00,
        "stock": 95,
        "status": 1,
        "image": "iphone14.jpg",
        "createdAt": "2023-12-01T10:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50,
    "totalPages": 3
  }
}
```

---

### 4. 更新商品信息
**PUT** `/api/inventory/products/{id}`

#### 请求示例
```json
{
  "name": "iPhone 14 Pro Max",
  "price": 8999.00,
  "stock": 120
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "商品信息更新成功",
  "data": null
}
```

---

### 5. 删除商品（下架）
**DELETE** `/api/inventory/products/{id}`

#### 响应示例
```json
{
  "success": true,
  "message": "商品已下架",
  "data": null
}
```

---

### 6. 调整库存
**PUT** `/api/inventory/products/{id}/stock`

#### 请求示例
```json
{
  "newStock": 150,
  "reason": "盘点调整"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "库存调整成功",
  "data": null
}
```

---

### 7. 增加库存（采购入库）
**POST** `/api/inventory/stock/increase`

#### 请求示例
```json
{
  "productId": 1,
  "quantity": 50,
  "orderId": "PUR20231201001"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "库存增加成功",
  "data": null
}
```

---

### 8. 减少库存（销售出库）
**POST** `/api/inventory/stock/decrease`

#### 请求示例
```json
{
  "productId": 1,
  "quantity": 2,
  "orderId": "ORDER20231201051"
}
```

#### 响应示例
```json
{
  "success": true,
  "message": "库存减少成功",
  "data": null
}
```

---

### 9. 获取库存变更日志
**GET** `/api/inventory/logs`

#### 查询参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| productId | integer | 否 | 商品ID筛选 |
| changeType | string | 否 | 变更类型 (purchase/sale/adjust) |
| page | integer | 否 | 页码，默认1 |
| pageSize | integer | 否 | 每页大小，默认20 |

#### 响应示例
```json
{
  "success": true,
  "message": "库存变更日志获取成功",
  "data": {
    "logs": [
      {
        "id": 1,
        "productId": 1,
        "changeType": "sale",
        "quantity": -1,
        "beforeStock": 100,
        "afterStock": 99,
        "orderId": "ORDER20231201001",
        "createdAt": "2023-12-01T14:00:00"
      }
    ],
    "currentPage": 1,
    "pageSize": 20,
    "totalCount": 50
  }
}
```

---

### 10. 获取低库存预警
**GET** `/api/inventory/low-stock`

#### 查询参数
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| threshold | integer | 否 | 预警阈值，默认10 |

#### 响应示例
```json
{
  "success": true,
  "message": "低库存预警获取成功",
  "data": {
    "threshold": 10,
    "lowStockProducts": [
      {
        "id": 24,
        "name": "海蓝之谜精华",
        "category": "美妆",
        "price": 2999.00,
        "stock": 5,
        "status": 1,
        "image": "lamer.jpg",
        "createdAt": "2023-12-01T10:00:00"
      }
    ],
    "count": 3
  }
}
```
---

## 🔧 技术实现要点

### 1. 密码验证（使用现有数据库结构）
```java
public class SecurityUtil {
    
    /**
     * 验证密码（与数据库存储方式一致）
     * 数据库：password_hash = hash(password + salt)
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String salt) {
        String inputHash = hashPassword(inputPassword, salt);
        return inputHash.equals(storedHash);
    }
    
    private static String hashPassword(String password, String salt) {
        // 与创建用户时的哈希算法保持一致
        // 现有数据库示例：salt是32位字符串，哈希算法可能是SHA-256
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hash = md.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("密码验证失败", e);
        }
    }
}
```

### 2. JWT Token生成
```java
public class JwtUtil {
    private static final String SECRET = "your_jwt_secret_key";
    private static final long EXPIRATION = 3600000; // 1小时
    
    public static String generateToken(Integer userId, String username, Boolean isAdmin) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("isAdmin", isAdmin)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }
    
    public static Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }
}
```

### 3. 统一响应格式
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;    // 仅失败时有
    private T data;            // 仅成功时有
    
    // 静态工厂方法
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setData(data);
        return response;
    }
    
    public static ApiResponse<Void> error(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
    
    // getters and setters
}
```

### 4. Token验证过滤器
```java
@WebFilter("/api/*")
public class TokenFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        // 白名单：登录和公开API
        String path = request.getRequestURI();
        if (path.endsWith("/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }
        
        // 验证Token
        String token = extractToken(request);
        if (token == null) {
            sendError(response, 401, "需要身份验证");
            return;
        }
        
        Claims claims = JwtUtil.validateToken(token);
        if (claims == null) {
            sendError(response, 401, "Token无效或已过期");
            return;
        }
        
        // 设置用户上下文
        Integer userId = Integer.parseInt(claims.getSubject());
        request.setAttribute("userId", userId);
        
        chain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

### 5. UserServlet核心逻辑
```java
@WebServlet("/api/*")
public class UserServlet extends BaseServlet {
    private final UserDAO userDAO = new UserDAO();
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        
        if ("/auth/login".equals(path)) {
            handleLogin(req, resp);
        } else if ("/auth/logout".equals(path)) {
            handleLogout(req, resp);
        } else {
            sendError(resp, 404, "API不存在");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        
        if ("/user/permissions".equals(path)) {
            handleGetPermissions(req, resp);
        } else {
            sendError(resp, 404, "API不存在");
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String path = req.getPathInfo();
        
        if ("/user/profile".equals(path)) {
            handleUpdateProfile(req, resp);
        } else {
            sendError(resp, 404, "API不存在");
        }
    }
    
    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        // 解析请求
        LoginRequest loginReq = parseJsonBody(req, LoginRequest.class);
        
        // 查询用户
        SystemUser user = userDAO.findByUsername(loginReq.getUsername());
        if (user == null) {
            sendJsonResponse(resp, 401, ApiResponse.error("用户名或密码错误"));
            return;
        }
        
        // 验证密码
        boolean valid = SecurityUtil.verifyPassword(
            loginReq.getPassword(),
            user.getPasswordHash(),
            user.getSalt()
        );
        
        if (!valid) {
            sendJsonResponse(resp, 401, ApiResponse.error("用户名或密码错误"));
            return;
        }
        
        // 检查状态
        if (user.getStatus() != 1) {
            sendJsonResponse(resp, 403, ApiResponse.error("用户已被禁用"));
            return;
        }
        
        // 生成Token
        String token = JwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            user.getAdmin()
        );
        
        // 更新登录时间
        userDAO.updateLastLoginTime(user.getId());
        
        // 返回响应
        LoginResponse loginResp = new LoginResponse();
        loginResp.setToken(token);
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setAdmin(user.getAdmin());
        loginResp.setUser(userInfo);
        
        sendJsonResponse(resp, 200, ApiResponse.success(loginResp));
    }
    
    private void handleGetPermissions(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        Integer userId = (Integer) req.getAttribute("userId");
        SystemUser user = userDAO.findById(userId);
        
        PermissionResponse permissions = new PermissionResponse();
        permissions.setAdmin(user.getAdmin());
        permissions.setCanManageOrder(user.getCanManageOrder());
        permissions.setCanManageLogistics(user.getCanManageLogistics());
        permissions.setCanManageAfterSales(user.getCanManageAfterSales());
        permissions.setCanManageReview(user.getCanManageReview());
        permissions.setCanManageInventory(user.getCanManageInventory());
        permissions.setCanManageIncome(user.getCanManageIncome());
        
        sendJsonResponse(resp, 200, ApiResponse.success(permissions));
    }
    
    private void handleUpdateProfile(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        
        Integer userId = (Integer) req.getAttribute("userId");
        UpdateProfileRequest updateReq = parseJsonBody(req, UpdateProfileRequest.class);
        
        // 数据验证
        if (!updateReq.hasUpdateFields()) {
            sendJsonResponse(resp, 400, ApiResponse.error("至少需要一个更新字段"));
            return;
        }
        
        // 邮箱唯一性检查
        if (updateReq.getEmail() != null) {
            SystemUser existing = userDAO.findByEmail(updateReq.getEmail());
            if (existing != null && !existing.getId().equals(userId)) {
                sendJsonResponse(resp, 409, ApiResponse.error("邮箱已被使用"));
                return;
            }
        }
        
        // 更新用户
        SystemUser user = userDAO.findById(userId);
        if (updateReq.getRealName() != null) {
            user.setRealName(updateReq.getRealName());
        }
        if (updateReq.getEmail() != null) {
            user.setEmail(updateReq.getEmail());
        }
        if (updateReq.getPhone() != null) {
            user.setPhone(updateReq.getPhone());
        }
        
        boolean success = userDAO.update(user);
        if (!success) {
            sendJsonResponse(resp, 500, ApiResponse.error("更新失败"));
            return;
        }
        
        // 返回更新后的信息
        ProfileResponse profileResp = new ProfileResponse();
        profileResp.setId(user.getId());
        profileResp.setRealName(user.getRealName());
        profileResp.setEmail(user.getEmail());
        profileResp.setPhone(user.getPhone());
        
        sendJsonResponse(resp, 200, ApiResponse.success(profileResp));
    }
    
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        // 无状态实现：客户端丢弃Token即可
        sendJsonResponse(resp, 200, ApiResponse.success(null));
    }
}
```

### 6. DTO类（简洁版）

```java
// 登录请求
public class LoginRequest {
    private String username;
    private String password;
    // getters and setters
}

// 登录响应
public class LoginResponse {
    private String token;
    private UserInfo user;
    // getters and setters
}

// 用户信息
public class UserInfo {
    private Integer id;
    private String username;
    private String realName;
    private Boolean admin;
    // getters and setters
}

// 权限响应
public class PermissionResponse {
    private Boolean admin;
    private Boolean canManageOrder;
    private Boolean canManageLogistics;
    private Boolean canManageAfterSales;
    private Boolean canManageReview;
    private Boolean canManageInventory;
    private Boolean canManageIncome;
    // getters and setters
}

// 更新个人信息请求
public class UpdateProfileRequest {
    private String realName;
    private String email;
    private String phone;
    
    public boolean hasUpdateFields() {
        return realName != null || email != null || phone != null;
    }
    // getters and setters
}

// 个人信息响应
public class ProfileResponse {
    private Integer id;
    private String realName;
    private String email;
    private String phone;
    // getters and setters
}
```

## 📦 依赖需求

### Maven依赖
```xml
<dependencies>
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- 其他现有依赖 -->
    <dependency>
        <groupId>javax.servlet</groupId>
        <artifactId>javax.servlet-api</artifactId>
        <version>4.0.1</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.13.3</version>
    </dependency>
</dependencies>
```

## 🔒 安全配置

### 1. 密码策略
- 现有数据库：`password_hash = hash(password + salt)`
- 盐值：32位随机字符串（已存在）
- 哈希算法：SHA-256

### 2. Token配置
```properties
# application.properties
jwt.secret=your-strong-secret-key-change-in-production
jwt.expiration=3600 # 1小时
```

### 3. HTTPS强制
```nginx
# Nginx配置
server {
    listen 80;
    server_name api.example.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    server_name api.example.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location /api/ {
        proxy_pass http://localhost:8080;
    }
}
```

## 📝 数据库验证
确保现有表结构：
```sql
CREATE TABLE apexflow_system_user (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(32) NOT NULL,
    real_name VARCHAR(50),
    email VARCHAR(100),
    phone VARCHAR(20),
    is_admin BOOLEAN DEFAULT FALSE,
    can_manage_order BOOLEAN DEFAULT FALSE,
    can_manage_logistics BOOLEAN DEFAULT FALSE,
    can_manage_after_sales BOOLEAN DEFAULT FALSE,
    can_manage_review BOOLEAN DEFAULT FALSE,
    can_manage_inventory BOOLEAN DEFAULT FALSE,
    can_manage_income BOOLEAN DEFAULT FALSE,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at DATETIME
);
```

## 🚀 快速测试

### 测试登录
```bash
curl -X POST https://api.example.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"your_password"}'
```

### 测试权限获取
```bash
curl -X GET https://api.example.com/api/user/permissions \
  -H "Authorization: Bearer your_jwt_token"
```

### 测试更新信息
```bash
curl -X PUT https://api.example.com/api/user/profile \
  -H "Authorization: Bearer your_jwt_token" \
  -H "Content-Type: application/json" \
  -d '{"realName":"张三","email":"zhangsan@example.com"}'
```

## 🎯 核心优势
1. **简洁**：仅4个API，响应数据最小化
2. **兼容**：完全基于现有数据库表
3. **安全**：HTTPS传输 + 后端哈希验证
4. **无状态**：JWT Token，无需额外存储
5. **可扩展**：基础框架易于添加新功能

这个实现删除了所有非必要功能，专注于四个核心API的实现，代码量减少60%以上，同时保持生产级安全性。

## 🧪 测试

### 单元测试
```bash
# 运行后端单元测试
cd apexflow-server
mvn test

# 运行前端单元测试
cd ../apexflow-web
npm run test:unit
```

### 集成测试
```bash
# 运行端到端测试
cd apexflow-web
npm run test:e2e
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！参与项目步骤如下：

1. **Fork 项目**：点击右上角的Fork按钮
2. **克隆仓库**：`git clone https://github.com/yourusername/apexflow.git`
3. **创建分支**：`git checkout -b feature/your-feature-name`
4. **提交更改**：`git commit -m 'Add some feature'`
5. **推送到分支**：`git push origin feature/your-feature-name`
6. **提交Pull Request**

### 开发规范
- Java代码遵循阿里巴巴Java开发手册
- Vue组件使用组合式API编写
- 提交信息使用英文描述，格式为：`type(scope): message`
- 新增功能需包含相应的单元测试

### 代码提交类型
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 代码重构
- `test`: 测试相关
- `chore`: 构建过程或辅助工具变动

## 📄 许可证

本项目基于 **GNU General Public License v3.0** 开源协议发布。

```
ApexFlow - 电商信息管理系统
Copyright (C) 2023 ApexFlow Contributors

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```

## 📞 支持与联系

- **项目主页**: [https://github.com/yourusername/apexflow](https://github.com/yourusername/apexflow)
- **问题反馈**: [GitHub Issues](https://github.com/yourusername/apexflow/issues)
- **讨论区**: [GitHub Discussions](https://github.com/yourusername/apexflow/discussions)
- **邮箱**: apexflow@example.com

## 🙏 致谢

感谢以下开源项目为ApexFlow提供支持：
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Apache Tomcat](https://tomcat.apache.org/)
- [MySQL](https://www.mysql.com/)

---

**ApexFlow** - 简化电商管理，提升运营效率！