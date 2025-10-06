package com.hitech0926.hubcommand;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@Plugin(
        id = "hubcommand",
        name = "HubCommand",
        version = "1.0.3",
        authors = {"Hitech0926"}
)
public class HubCommand {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private boolean enabled;
    private String lobbyServer;
    private String sendSuccessful;
    private String noPermission;
    private String noConsole;

    @Inject
    public HubCommand(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("HubCommand插件已加载！");

        // 加载配置
        loadConfig();

        // 注册命令
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("hub").build(),
                new HubCommandExecutor(this)
        );
        server.getCommandManager().register(
                server.getCommandManager().metaBuilder("lobby").build(),
                new HubCommandExecutor(this)
        );
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("HubCommand插件已卸载！");
    }

    public ProxyServer getServer() {
        return server;
    }

    private void loadConfig() {
        if (!Files.exists(dataDirectory)) {
            try {
                Files.createDirectories(dataDirectory);
            } catch (IOException e) {
                logger.error("无法创建插件目录", e);
                return;
            }
        }

        File file = dataDirectory.resolve("config.yml").toFile();
        Yaml yaml = new Yaml();
        if (!file.exists()) {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (inputStream != null) {
                    Files.copy(inputStream, file.toPath());
                    logger.info("配置文件已创建！");
                } else {
                    logger.error("无法找到默认配置文件");
                    return;
                }
            } catch (IOException e) {
                logger.error("无法创建配置文件", e);
                return;
            }
        }
        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            Map<String, Object> config = yaml.load(inputStream);
            enabled = (boolean) config.getOrDefault("enabled", true);
            lobbyServer = (String) config.getOrDefault("lobby-server", "lobby");
            sendSuccessful = (String) config.getOrDefault("send-successful", "<green>你已被传送到大厅！");
            noPermission = (String) config.getOrDefault("no-permission", "<red>你没有权限执行此命令！");
            noConsole = (String) config.getOrDefault("no-console", "<red>只有玩家可以执行此命令！");

            logger.info("配置文件加载成功！");
        } catch (IOException e) {
            logger.error("无法加载配置文件", e);
        }
    }

    private static class HubCommandExecutor implements SimpleCommand {
        private final HubCommand plugin;

        public HubCommandExecutor(HubCommand plugin) {
            this.plugin = plugin;
        }

        @Override
        public void execute(Invocation invocation) {
            CommandSource source = invocation.source();
            String[] args = invocation.arguments();

            if (!plugin.enabled) {
                return;
            }

            // 处理命令逻辑
            if (args.length == 0) {
                // 玩家传送自己
                if (!(source instanceof Player)) {
                    source.sendMessage(MiniMessage.miniMessage().deserialize(plugin.noConsole));
                    return;
                }

                Player player = (Player) source;
                if (!player.hasPermission("hubcommand.player")) {
                    player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.noPermission));
                    return;
                }

                teleportPlayer(player);
            } else if (args.length == 1) {
                // 管理员传送其他玩家
                if (!source.hasPermission("hubcommand.admin")) {
                    source.sendMessage(MiniMessage.miniMessage().deserialize(plugin.noPermission));
                    return;
                }

                Optional<Player> targetPlayer = plugin.getServer().getPlayer(args[0]);
                if (targetPlayer.isPresent()) {
                    teleportPlayer(targetPlayer.get());
                }
            }
        }

        private void teleportPlayer(Player player) {
            Optional<RegisteredServer> lobbyServer = plugin.getServer().getServer(plugin.lobbyServer);
            if (lobbyServer.isPresent()) {
                player.createConnectionRequest(lobbyServer.get()).fireAndForget();
                player.sendMessage(MiniMessage.miniMessage().deserialize(plugin.sendSuccessful));
            }
        }
    }
}