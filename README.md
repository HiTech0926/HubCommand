# HubCommand - Velocity 大厅传送插件



一款轻量高效的 Velocity 代理端插件，通过 `/hub` 和 `/lobby` 命令实现玩家快速传送至大厅服务器，支持权限管理与全自定义提示消息。





## 📁 配置文件 `config.yml`



```yaml

# 是否启用插件

Enable: true

# 大厅服务器名称（需与 Velocity 配置中的服务器名一致）

LobbyServer: lobby

# 传送成功提示（支持颜色代码）

SendSuccessful: "&7[&e&lHubCommand&7]&a 正在传送到大厅服务器！"

# 无权限提示

NoPermission: "&7[&e&lHubCommand&7]&c 你没有权限执行此命令！"

# 控制台执行提示

NoConsole: "&7[&e&lHubCommand&7]&c 此命令只能由玩家执行！"

```



**配置说明**

- 颜色代码：使用 `&` 符号

- 多语言兼容：直接修改提示文本即可实现本地化



**权限控制**：

- `hubcommand.player`: 基础传送权限

- `hubcommand.admin`: 可传送其他玩家（如 `/hub <玩家名>`）



**技术亮点**：

- 异步传送机制：`fireAndForget()` 确保不阻塞主线程

- 服务器状态检测：自动跳过无效/离线的目标服务器



---



## 🌟 插件特色



### 多维度自定义

- **消息全定制**：支持颜色代码与组件化消息

- **灵活开关**：随时禁用插件而不影响其他功能

- **跨服兼容**：完美适配 Velocity 的服务器集群架构



---



## 🚀 使用场景



1. **大厅返回**：玩家在任何子服输入 `/hub` 即刻回城

2. **活动管理**：管理员强制传送玩家到准备大厅



---



> 项目开源地址：https://github.com/HiTech0926/HubCommand

> 支持作者：https://afdian.com/a/HiTech0926

> QQ交流群：879016948

> 下载方式：通过 Jar 文件直接放入 `plugins/` 目录
