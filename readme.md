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
| **评价管理** | 商品评价查看，回复评价，评价数据分析 | ✅ 已完成 |
| **仓库管理** | 库存管理，入库/出库记录，库存预警 | ✅ 已完成 |
| **财务管理** | 收支统计，财务报表，账单管理 | ✅ 已完成 |

## 🛠️ 技术架构

### 技术栈

**前端 (Vue 3.x)**
- **框架**: Vue 3.x (组合式API)
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由管理**: Vue Router 4
- **HTTP客户端**: Axios
- **构建工具**: Vite

**后端 (Java Servlet)**
- **服务器**: Apache Tomcat 9.x
- **Java版本**: JDK 17
- **Web框架**: Servlet
- **数据库**: MySQL 8.0+
- **连接池**: HikariCP
- **JSON处理**: Jackson
- **日志框架**: Log4j2

**开发工具**
- **IDE**: IntelliJ IDEA  + VS Code
- **构建工具**: Maven 3.6+
- **数据库工具**: MySQL Workbench
- **版本控制**: Git

### 架构图

```
┌─────────────────────────────────────────┐
│           浏览器客户端 (Vue SPA)         │
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
│   │  └── Filters/                   │   │
│   └─────────────────────────────────┘   │
│                  │                      │
│           ┌──────▼───────┐              │
│           │    MySQL     │              │
│           │   数据库      │              │
│           └──────────────┘              │
└─────────────────────────────────────────┘
```


## 项目结构树
```
ApexFlow/
├── readme.md                     # 项目说明文档
├── apexflow_server/              # 服务器端代码
│   ├── .smarttomcat/ApexFlow/conf/web.xml  # Tomcat配置文件
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/apex/
│   │   │   │   ├── config/FrontendRouterFilter.java  # 前端路由过滤配置
│   │   │   │   ├── core/
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── IReviewDAO.java           # 评价数据访问接口
│   │   │   │   │   │   ├── ProductDAO.java           # 商品数据访问类
│   │   │   │   │   ├── dto/OrderWithItemsResponse.java  # 订单及订单项响应DTO
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── SystemUser.java           # 系统用户模型
│   │   │   │   │   ├── service/UserService.java      # 用户服务类
│   │   │   │   ├── util/
│   │   │   │   │   ├── LoggingConnection.java        # 日志连接工具类
│   │   │   │   │   ├── LoggingStatement.java         # 日志语句工具类
│   │   │   ├── webapp/assets/
│   │   │   │   ├── strings-BdNYIbtM.js               # 前端字符串处理工具
│   │   │   │   ├── strings-BZDa8bsY.js               # 前端字符串处理工具
│   │   ├── test/java/
│   │   │   ├── AfterSalesDAOTest.java                # 售后DAO测试类
│   │   │   ├── OrderItemDAOTest.java                 # 订单项DAO测试类
│   │   │   ├── OrderServiceTest.java                 # 订单服务测试类
│   │   │   ├── UserDAOTest.java                      # 用户DAO测试类
├── apexflow_web/                 # 前端代码
│   ├── package-lock.json         # npm依赖锁文件
│   ├── src/components/layout/Sidebar.vue  # 侧边栏组件
```


## 核心表格说明

### 1. 代码提交类型说明
| 提交类型 | 描述 |
|----------|------|
| `feat` | 新增功能 |
| `fix` | 修复bug |
| `docs` | 文档更新 |
| `style` | 代码格式调整（不影响代码逻辑） |
| `refactor` | 代码重构（既不是新增功能也不是修bug） |
| `test` | 测试相关代码（新增/修改测试用例） |
| `chore` | 构建过程或辅助工具变动（如依赖更新、配置修改） |


### 2. 订单状态转换规则（测试验证）
| 原状态 | 目标状态 | 说明 |
|--------|----------|------|
| 1（待支付） | 2（已支付） | 支付成功后状态转换，触发收入记录创建 |
| 2（已支付） | 3（已发货） | 商家发货后状态转换 |
| 3（已发货） | 4（已完成） | 买家确认收货后状态转换 |
| 1（待支付） | 5（已取消） | 订单取消，触发库存回补 |


## 核心功能示例

### 1. 订单项批量创建示例（测试用例）
```java
private static @NotNull List<OrderItem> getOrderItems() {
    List<OrderItem> batchItems = new ArrayList<>();

    OrderItem item1 = new OrderItem();
    item1.setOrderId("ITEM_BATCH_001");
    item1.setProductId(1);
    item1.setProductName("iPhone 14 Pro");
    item1.setQuantity(2);
    item1.setPrice(new BigDecimal("7999.00"));
    item1.setSubtotal(new BigDecimal("15998.00"));

    OrderItem item2 = new OrderItem();
    item2.setOrderId("ITEM_BATCH_001");
    item2.setProductId(2);
    item2.setProductName("MacBook Pro 16英寸");
    item2.setQuantity(1);
    item2.setPrice(new BigDecimal("18999.00"));
    item2.setSubtotal(new BigDecimal("18999.00"));

    batchItems.add(item1);
    batchItems.add(item2);
    return batchItems;
}

// 批量创建调用
boolean result = orderItemDAO.createBatch(batchItems);
```


### 2. 用户权限获取示例（服务层代码）
```java
public Map<String, Object> getPermissions(String token) {
    Map<String, Object> result = new HashMap<>();

    // 验证Token
    if (!JwtUtil.validateToken(token)) {
        result.put("success", false);
        result.put("message", "无效的Token");
        return result;
    }

    Integer userId = JwtUtil.getUserIdFromToken(token);
    SystemUser user = userDAO.getPermissions(userId);
    
    // 构建权限响应
    Map<String, Object> permissions = new HashMap<>();
    permissions.put("isAdmin", user.getAdmin());
    permissions.put("canManageOrder", user.getCanManageOrder());
    permissions.put("canManageLogistics", user.getCanManageLogistics());
    permissions.put("canManageReview", user.getCanManageReview());
    // ... 其他权限
    
    result.put("success", true);
    result.put("data", permissions);
    return result;
}
```


### 3. 前端侧边栏样式示例（Vue组件CSS）
```css
/* 折叠菜单tooltip样式 */
.sidebar-menu.el-menu--collapse :deep(.el-tooltip) {
  font-size: 12px !important;
  padding: 4px 8px !important;
  background: #24292e !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15) !important;
  border-radius: 4px !important;
  max-width: 200px !important;
}

/* 徽章样式 */
.badge-wrapper {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  padding: 0px;
}
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

-- 导入表结构（SQL文件位于 /apexflow_db）
-- mysql -u root -p apexflow < /apexflow_db/xxx.sql
```

### 2. 项目构建与部署

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
mvn tomcat9:run  # 或使用IDE启动

# 2. 启动前端开发服务器（在apexflow-web目录）
npm run serve

# 3. 访问应用
# 前端开发地址：http://localhost:3000
# 后端API地址：http://localhost:8080
```

## 📊 数据库设计

主要数据表示例：

```sql
CREATE TABLE apexflow_order (
    id VARCHAR(50) PRIMARY KEY COMMENT '订单号，格式如ORDER20231215001',
    user_id INT NOT NULL COMMENT '下单用户ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status TINYINT NOT NULL COMMENT '状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消',
    payment_method VARCHAR(20) COMMENT '支付方式：alipay,wxpay等',
    address_id INT COMMENT '收货地址ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    paid_at DATETIME COMMENT '支付时间',
    shipped_at DATETIME COMMENT '发货时间',
    completed_at DATETIME COMMENT '完成时间'
) COMMENT='订单主表';

CREATE TABLE apexflow_order_item (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
    order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
    product_id INT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(100) NOT NULL COMMENT '商品名称（下单时的快照）',
    quantity INT NOT NULL COMMENT '购买数量',
    price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
    FOREIGN KEY (order_id) REFERENCES apexflow_order(id),
    FOREIGN KEY (product_id) REFERENCES apexflow_product(id)
) COMMENT='订单商品明细表';
```

完整数据库脚本请查看 `apexflow_db` 目录。

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
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <!-- groupId：
        注意：这是工程组的标识。它在一个组织或者项目中通常是唯一的。
        需要在src/main/java/下创建对应软件包,这里是com.apex,
        则所有源代码必须位于com.example包下。
        可以自定义为其他包，比如com.myName,同样需要在src/main/java/下创建对应软件包
     -->
    <groupId>com.apex</groupId>

    <!-- 这是工程的标识。它通常是工程的名称。 -->
    <artifactId>ApexFlow</artifactId>

    <!-- 这是工程的版本号。在 artifact 的仓库中，它用来区分不同的版本。 -->
    <version>1.0-SNAPSHOT</version>

    <!-- 打包类型：web项目为war -->
    <packaging>war</packaging>

    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <servlet.version>4.0.1</servlet.version>
        <junit.version>5.9.2</junit.version>
    </properties>

    <!-- 项目的依赖（下面几个是必须的依赖，如果自己用了其他库自行添加） -->
    <dependencies>
        <!-- Servlet API -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>javax.servlet-api</artifactId>
            <version>${servlet.version}</version>
            <scope>provided</scope>
        </dependency>

        <!-- JSP API -->
        <dependency>
            <groupId>javax.servlet.jsp</groupId>
            <artifactId>javax.servlet.jsp-api</artifactId>
            <version>2.3.3</version>
            <scope>provided</scope>
        </dependency>

        <!-- JSTL API (标准规范) -->
        <dependency>
            <groupId>org.apache.taglibs</groupId>
            <artifactId>taglibs-standard-spec</artifactId>
            <version>1.2.5</version>
        </dependency>

        <!-- JSTL 实现 (Apache Standard Taglibs) -->
        <dependency>
            <groupId>org.apache.taglibs</groupId>
            <artifactId>taglibs-standard-impl</artifactId>
            <version>1.2.5</version>
        </dependency>

        <!-- SLF4J API -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>

        <!-- Logback 实现 -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.5.13</version>
        </dependency>

        <!-- 可选：Logback 配置文件 -->
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-core</artifactId>
            <version>1.5.19</version>
        </dependency>

        <!-- 测试 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter-api</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- H2数据库（开发用） -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <version>2.2.220</version>
            <scope>runtime</scope>
        </dependency>

        <!-- MySQL数据库驱动 -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>

        <!-- 连接池 -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.0.1</version>
        </dependency>

        <!-- 单元测试 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.9.2</version>
            <scope>test</scope>
        </dependency>

        <!-- json支持 -->
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.15.2</version>
        </dependency>

        <dependency>
            <groupId>com.fasterxml.jackson.datatype</groupId>
            <artifactId>jackson-datatype-jsr310</artifactId>
            <version>2.15.2</version> <!-- 请使用与项目中 Jackson 版本兼容的版本 -->
        </dependency>

        <!-- jetbrains注解 -->
        <dependency>
            <groupId>org.jetbrains</groupId>
            <artifactId>annotations</artifactId>
            <version>RELEASE</version>
            <scope>compile</scope>
        </dependency>

        <!-- Mockito -->
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-junit-jupiter</artifactId>
            <version>5.3.1</version>
            <scope>test</scope>
        </dependency>

        <!-- JWT依赖 -->
        <!-- JWT dependencies -->
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

        <!-- Gson依赖 -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>

        <dependency>
            <groupId>javax.validation</groupId>
            <artifactId>validation-api</artifactId>
            <version>2.0.1.Final</version>
        </dependency>
        <dependency>
            <groupId>org.hibernate.validator</groupId>
            <artifactId>hibernate-validator</artifactId>
            <version>6.2.0.Final</version>
        </dependency>

        <dependency>
            <groupId>org.glassfish</groupId>
            <artifactId>javax.el</artifactId>
            <version>3.0.0</version>
        </dependency>

    </dependencies>

    <build>
        <!-- 输出war文件名称，可以自定义 -->
        <finalName>ApexFlow</finalName>
        <plugins>
            <!-- Maven 编译插件 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                    <encoding>UTF-8</encoding>
                </configuration>
            </plugin>

            <!-- Maven War 插件 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-war-plugin</artifactId>
                <version>3.4.0</version>
                <configuration>
                    <failOnMissingWebXml>false</failOnMissingWebXml>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>3.0.0-M5</version>
                <configuration>
                    <argLine>-Xshare:off</argLine>  <!-- 禁用类数据共享 -->
                </configuration>
            </plugin>

            <!-- Tomcat 7 插件（用于 Maven 运行） -->
            <plugin>
                <groupId>org.apache.tomcat.maven</groupId>
                <artifactId>tomcat7-maven-plugin</artifactId>
                <version>2.2</version>
                <configuration>

                    <!-- 端口 -->
                    <port>8080</port>

                    <!--Maven 运行 Tomcat 服务器路径
                    请改为 /artifactId的值-->
                    <path>/ApexFlow</path>

                    <uriEncoding>UTF-8</uriEncoding>

                </configuration>
            </plugin>
        </plugins>
    </build>
</project>

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

### 🔧 PasswordHashGenerator 工具使用文档

#### 📋 工具概述

`PasswordHashGenerator` 是一个用于生成密码盐值和哈希值的命令行工具，专门为 `apexflow_system_user` 数据库表设计。它可以帮助你：
- 为管理员账户生成安全的密码哈希
- 批量创建测试用户数据
- 验证密码哈希的正确性
- 生成可直接执行的SQL语句

---

#### 🚀 开始

使用IDEA打开apexflow_server文件夹，导航到 `/src/main/java/com/apex/util/PasswordHashGenerator.java`直接执行

#### 📖 功能示例

#### 📝 示例1：生成单个密码哈希（快速测试）

**场景**：为管理员账户 "admin" 生成密码 "admin123" 的哈希值

```bash
# 运行工具后选择选项1
请选择操作：1

请输入用户名: admin
请输入密码: admin123

========== 生成结果 ==========
用户名: admin
原始密码: admin123
盐值 (32位): a1b2c3d4e5f678901234567890123456
哈希值: Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==
================================

===== SQL 插入语句示例 =====
INSERT INTO apexflow_system_user (username, password_hash, salt)
VALUES ('admin', 'Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==', 'a1b2c3d4e5f678901234567890123456');
==============================
```

#### 🔐 示例2：生成随机强密码

**场景**：为物流管理员生成随机强密码

```bash
# 运行工具后选择选项2
请选择操作：2

请输入用户名: logistics_admin
请输入要生成的密码长度 (默认12位): 16
生成强密码？(y/N): y

========== 随机密码生成 ==========
已生成随机密码，请妥善保管！

========== 生成结果 ==========
用户名: logistics_admin
原始密码: K8$gT2xLp9mNq6zA!B3#c
盐值 (32位): w3XkP7tM1vQ8rZ5yA9bC2dE4fG6hJ8k
哈希值: tL4mN8vQ2xK7pZ9w1R3yU5iJ0oA6sDfGh7jM0OqA==
================================
```

#### 📊 示例3：批量生成测试用户

**场景**：为测试环境批量生成3个测试用户

```bash
# 运行工具后选择选项3
请选择操作：3

请输入要生成的密码数量 (1-10): 3
生成强密码？(y/N): n

请输入用户名1: test_user1
请输入用户名2: test_user2
请输入用户名3: test_user3

--- 用户 #1 ---
用户名: test_user1
密码: AbC123!@#
盐值: x1Y2z3A4b5C6d7E8f9G0h1I2j3K4l5
哈希值: mN8pQ9rS2tU3vW4xY5z6A7B8C9D0E1

--- 用户 #2 ---
用户名: test_user2
密码: XyZ456$%^
盐值: M6n7O8p9Q0r1S2t3U4v5W6x7Y8z9A0
哈希值: B1C2D3E4F5G6H7I8J9K0L1M2N3O4

--- 用户 #3 ---
用户名: test_user3
密码: LmN789&*(
盐值: B1c2D3e4F5g6H7i8J9k0L1m2N3o4P5
哈希值: Q6R7S8T9U0V1W2X3Y4Z5a6b7c8d9
```

#### 🗃️ 示例4：生成完整用户SQL语句

**场景**：创建具有特定权限的运营人员账户

```bash
# 运行工具后选择选项4
请选择操作：4

请输入用户名: operations
请输入真实姓名: 运营专员
请输入邮箱: operations@apexflow.com
请输入手机号: 13900139001
请输入密码: Ops@123456

请设置权限 (输入y表示启用，n表示禁用):
超级管理员 (is_admin): n
订单管理权限 (can_manage_order): y
物流管理权限 (can_manage_logistics): y
售后管理权限 (can_manage_after_sales): y
评价管理权限 (can_manage_review): y
库存管理权限 (can_manage_inventory): n
收入管理权限 (can_manage_income): n
状态 (1-正常, 0-禁用): 1

========== 生成的SQL语句 ==========
INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, 
is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, 
can_manage_review, can_manage_inventory, can_manage_income, status) VALUES
('operations', 'T7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==', 'q1w2e3r4t5y6u7i8o9p0a1s2d3f4g5', 
'运营专员', 'operations@apexflow.com', '13900139001', 
FALSE, TRUE, TRUE, TRUE, TRUE, FALSE, FALSE, 1);
```

#### ⚡ 示例5：快速生成管理员账户SQL

**场景**：快速创建超级管理员和物流管理员

```bash
# 运行工具后选择选项5
请选择操作：5

请输入管理员用户名 (默认admin): admin
请输入管理员密码 (默认admin123): Admin@2024
请输入真实姓名 (默认系统管理员): 超级管理员
请输入邮箱 (默认admin@apexflow.com): 
请输入手机号 (默认13800138000): 

========== 管理员账户SQL语句 ==========
-- 创建超级管理员账户
INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, 
is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, 
can_manage_review, can_manage_inventory, can_manage_income, status) VALUES
('admin', 'X5y6z7A8B9C0D1E2F3G4H5I6J7K8L9M0N1O2P3Q4', 'z1x2c3v4b5n6m7a8s9d0f1g2h3j4k5l6', 
'超级管理员', 'admin@apexflow.com', '13800138000', 
TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 1);

-- 测试用的普通物流管理员
INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, 
is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, 
can_manage_review, can_manage_inventory, can_manage_income, status) VALUES
('logistics_admin', 'X5y6z7A8B9C0D1E2F3G4H5I6J7K8L9M0N1O2P3Q4', 'z1x2c3v4b5n6m7a8s9d0f1g2h3j4k5l6', 
'物流管理员', 'logistics@apexflow.com', '13900139000', 
FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 1);
```

#### ✅ 示例6：验证密码哈希

**场景**：验证数据库中的密码是否正确

```bash
# 运行工具后选择选项6
请选择操作：6

请输入密码: admin123
请输入盐值: a1b2c3d4e5f678901234567890123456
请输入期望的哈希值: Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==

========== 验证结果 ==========
输入密码: admin123
盐值: a1b2c3d4e5f678901234567890123456
期望哈希: Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==
实际哈希: Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==
验证结果: ✓ 匹配成功
密码验证通过！
```

---

#### 🔧 高级用法

#### 非交互式批量生成

创建一个输入文件 `input.txt`：

```
4
test_user1
测试用户1
test1@example.com
13800000001
Test@123
y
y
y
y
y
y
y
1
0
```

然后运行：
```bash
java -cp . com.apex.util.PasswordHashGenerator < input.txt
```

#### 生成H2数据库测试数据

```sql
-- 使用工具生成的SQL可以直接插入到H2内存数据库
INSERT INTO apexflow_system_user (username, password_hash, salt, is_admin, status) VALUES
('test_admin', 'Lp7mN9vQ2xK4pZ8w1R3yU5iJ0oA6sDfGh7jM0OqA==', 'a1b2c3d4e5f678901234567890123456', TRUE, 1);
```

---

#### ⚠️ 注意事项

1. **安全提醒**
   - 生成的密码和盐值请妥善保管
   - 生产环境请勿使用示例密码
   - 定期更换管理员密码

2. **数据库兼容性**
   - 生成的SQL适用于MySQL、PostgreSQL、H2等数据库
   - 确保数据库表结构与 `apexflow_system_user` 一致
   - 盐值必须是32位字符串

3. **测试建议**
   ```bash
   # 在集成测试中使用
   @BeforeAll
   static void setupTestUsers() {
       // 使用工具生成的哈希值初始化测试数据库
   }
   ```

---

#### 🎯 常见使用场景

#### 场景1：初始化新系统
```bash
# 生成管理员账户
echo "初始化管理员账户..."
java -cp . com.apex.util.PasswordHashGenerator <<EOF > init_admin.sql
5
admin
ChangeMe@2024
系统管理员
admin@company.com
13800138000
0
EOF
```

#### 场景2：创建测试数据
```bash
# 批量创建测试用户
for i in {1..5}; do
  echo "生成测试用户$i..."
  # 使用工具生成并保存到文件
done
```

#### 场景3：密码迁移
```bash
# 验证现有用户密码
echo "验证用户密码哈希..."
java -cp . com.apex.util.PasswordHashGenerator <<EOF
6
old_password
existing_salt_from_db
existing_hash_from_db
0
EOF
```

---

#### 📊 输出格式说明

工具输出的格式如下：

1. **生成结果**：显示用户名、密码、盐值和哈希值
2. **SQL语句**：可直接执行的INSERT语句
3. **验证结果**：显示匹配状态和详细信息

所有生成的盐值均为32位字符串，符合 `apexflow_system_user` 表的设计要求。

---

**提示**：此工具主要用于开发、测试和数据库初始化阶段。生产环境建议使用更安全的密码管理策略。

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
cd apexflow_web
npm run test:e2e
```

## 🤝 贡献指南

我们欢迎所有形式的贡献！参与项目步骤如下：

1. **Fork 项目**：点击右上角的Fork按钮
2. **克隆仓库**：`git clone https://github.com/Yuriltlef/ApexFlow`
3. **创建分支**：`git checkout -b feature/your-feature-name`
4. **提交更改**：`git commit -m 'Add some feature'`
5. **推送到分支**：`git push origin feature/your-feature-name`
6. **提交Pull Request**

### 开发规范
- Java代码遵循阿里巴巴Java开发手册
- Vue组件使用组合式API编写
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

- **项目主页**: [https://github.com/Yuriltlef/ApexFlow](https://github.com/Yuriltlef/ApexFlow)
- **问题反馈**: [GitHub Issues](https://github.com/Yuriltlef/ApexFlow/issues)
- **讨论区**: [GitHub Discussions](https://github.com/Yuriltlef/ApexFlow/discussions)
- **邮箱**: yurilt15312@outlook.com

## 🙏 致谢

感谢以下开源项目为ApexFlow提供支持：
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Apache Tomcat](https://tomcat.apache.org/)
- [MySQL](https://www.mysql.com/)

致谢本组开发人员！！！

---

**ApexFlow** - 简化电商管理，提升运营效率！