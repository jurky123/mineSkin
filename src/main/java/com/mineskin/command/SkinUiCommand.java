package com.mineskin.command;

import com.mineskin.MineSkinPlugin;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /skinui [关键词]}、{@code /skinui page <页码> [关键词]}、{@code /skinui reload}
 * <p>
 * mod 客户端打开 3D 浏览器；原版客户端退回聊天列表（点击换肤/翻页）。
 */
public final class SkinUiCommand {

    private final MineSkinPlugin plugin;

    public SkinUiCommand(MineSkinPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(Commands commands) {
        LiteralCommandNode<CommandSourceStack> node = Commands.literal("skinui")
                .requires(source -> source.getSender().hasPermission("mineskin.use"))
                .executes(context -> {
                    open(context.getSource().getSender(), "", 1);
                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.literal("reload")
                        .requires(source -> source.getSender().hasPermission("mineskin.admin"))
                        .executes(context -> {
                            plugin.reloadCatalog();
                            context.getSource().getSender().sendMessage(Component.text(
                                    "皮肤目录已重载：" + plugin.catalog().size() + " 款自定义皮肤",
                                    NamedTextColor.GREEN));
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(Commands.literal("page")
                        .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    open(context.getSource().getSender(), "",
                                            IntegerArgumentType.getInteger(context, "number"));
                                    return Command.SINGLE_SUCCESS;
                                })
                                .then(Commands.argument("filter", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            open(context.getSource().getSender(),
                                                    StringArgumentType.getString(context, "filter"),
                                                    IntegerArgumentType.getInteger(context, "number"));
                                            return Command.SINGLE_SUCCESS;
                                        }))))
                .then(Commands.argument("filter", StringArgumentType.word())
                        .executes(context -> {
                            open(context.getSource().getSender(),
                                    StringArgumentType.getString(context, "filter"), 1);
                            return Command.SINGLE_SUCCESS;
                        }))
                .build();

        commands.register(node, "MineSkin 皮肤浏览器");
    }

    private void open(CommandSender sender, String filter, int page) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("该命令只能由玩家使用", NamedTextColor.RED));
            return;
        }
        plugin.openBrowser(player, filter, page);
    }
}
