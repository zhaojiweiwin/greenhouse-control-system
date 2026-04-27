# Greenhouse Control System / 温室智能控制系统

面向毕业设计的温室管控骨架：遥测接入、命令下发、实时看板、策略与告警、历史趋势及演示脚本。

---

## 软件架构设计

### 总体架构

系统采用 **B/S + 边缘设备经 MQTT 接入** 的物联网温室管控形态：

| 层次 | 职责 |
|------|------|
| **感知与执行层** | 终端设备通过 **MQTT** 上报遥测、接收控制指令（主题形如 `greenhouse/{deviceId}/telemetry`、`greenhouse/{deviceId}/command`）。仓库内可用 `scripts/simulate_device.py` 模拟。 |
| **接入与业务层** | **Spring Boot** 提供 **REST API**、**MQTT 订阅/发布**、**WebSocket** 推送、**规则引擎**（策略联动告警与自动下发）、**Spring Data JPA** 持久化。 |
| **数据层** | **MySQL** 存储设备、传感器数据、控制命令、告警、控制规则等（见 `db/schema.sql`）。 |
| **展示层** | **Vue 3** 单页应用：实时监控、设备控制、历史趋势、策略管理、告警中心等；经 **HTTP** 访问后端、**WebSocket** 接收实时遥测。 |

**数据流概要**：设备 → MQTT → 后端解析入库并执行规则评估 →（必要时）写库并经 MQTT 下发命令；前端 → REST 查询/控制；后端 → WebSocket → 前端看板。

### 技术栈与版本

#### 后端（`backend/pom.xml`）

| 项 | 版本 / 说明 |
|----|-------------|
| JDK | 1.8 |
| Spring Boot | **2.7.18**（BOM 管理依赖） |
| Spring 能力 | Web、Data JPA、Validation、Actuator、WebSocket |
| MQTT 客户端 | Eclipse Paho **1.2.5**（`org.eclipse.paho.client.mqttv3`） |
| MySQL 驱动 | `com.mysql:mysql-connector-j`（版本由 Spring Boot BOM 管理） |
| Lombok | optional |
| Maven | `spring-boot-maven-plugin` 2.7.18；`maven-compiler-plugin` **3.11.0** |

#### 前端（`frontend/package.json`）

| 项 | 版本（语义化范围） |
|----|---------------------|
| Vue | ^**3.4.31** |
| Vite | ^**5.3.4**（`@vitejs/plugin-vue` ^5.0.5） |
| vue-router | ^**4.4.0** |
| pinia | ^**2.1.7** |
| axios | ^**1.7.2** |

#### 基础设施（`docker-compose.yml`）

| 项 | 说明 |
|----|------|
| MySQL | 镜像 **mysql:8**；可挂载 `db/schema.sql` 初始化 |
| MQTT Broker | **eclipse-mosquitto:2** |
| 后端 / 前端容器 | compose 中可选：`backend` 构建自 `backend/`；前端 dev 使用 **node:18** |

#### 运行时配置（`backend/src/main/resources/application.yml`）

- HTTP 端口：**8080**
- JDBC：`jdbc:mysql://localhost:3306/greenhouse`，`serverTimezone=UTC`
- MQTT：`tcp://localhost:1883`，遥测订阅 `greenhouse/+/telemetry`，命令主题 `greenhouse/{deviceId}/command`

> **说明**：`docker-compose.yml` 中 MySQL 示例密码与 `application.yml` 可能不一致，部署时请统一修改环境变量或配置文件。

#### 脚本

- **Python 3**：`scripts/seed_month_data.py`（批量灌库）、`scripts/simulate_device.py`（遥测模拟）等。

### 后端分层（包结构 `com.greenhouse`）

| 包 / 目录 | 职责 |
|-----------|------|
| `controller` | REST 入口（遥测、设备、控制、策略、告警等） |
| `service` | 业务逻辑（遥测聚合、设备在线、控制下发、规则引擎、告警等） |
| `repository` | Spring Data JPA 数据访问 |
| `entity` | 与表结构对应的实体 |
| `dto` | 请求体、响应体、查询参数 |
| `mqtt` | MQTT 监听与发布 |
| `config` | MQTT、WebSocket、配置属性等 |

### 前端结构（`frontend/src`）

| 路径 | 职责 |
|------|------|
| `views/` | 各业务页面 |
| `api/` | axios 封装与接口模块 |
| `stores/` | Pinia 状态（如实时看板） |
| `router/` | 路由 |
| `utils/` | 工具函数（设备元数据、时间格式等） |

### 数据库

- 关系库 **MySQL**，建议使用 **utf8mb4**（见 `db/schema.sql`）。
- 核心表：设备、传感器数据、控制命令、告警记录、控制规则等。

### 部署拓扑（开发环境）

`docker compose up -d mysql mqtt` 启动 **MySQL :3306** 与 **MQTT :1883**；后端 `mvn spring-boot:run`（:8080）、前端 `npm run dev`（默认 Vite 端口，可按 `vite.config.js` 配置）；亦可使用 compose 中的 `backend` / `frontend` 服务。

---

## 仓库结构（Structure）

- `docs/`：架构说明与演示文档
- `db/`：MySQL 建表脚本
- `backend/`：Spring Boot（REST + MQTT + JPA）
- `frontend/`：Vue 3 管理端
- `scripts/`：仿真与灌库脚本

---

## 快速开始（Quick Start）

1. 启动依赖：`docker compose up -d mysql mqtt`（或本机已安装的 MySQL / Mosquitto）
2. 初始化库：执行 `db/schema.sql`，并按需运行 `python3 scripts/seed_month_data.py`
3. 启动后端：`cd backend && mvn spring-boot:run`
4. 启动前端：`cd frontend && npm install && npm run dev`
5. 模拟遥测（可选）：`python3 scripts/simulate_device.py`

---

## 文档与任务映射（Implemented TODO Mapping）

- 硬件与接入：`docs/hardware-stack.md`
- 数据库设计：`db/schema.sql`、`docs/db-design.md`
- 后端骨架：`backend/`
- 前端骨架：`frontend/`
- 联调与演示：`docker-compose.yml`、`scripts/`、`docs/demo-script.md`
