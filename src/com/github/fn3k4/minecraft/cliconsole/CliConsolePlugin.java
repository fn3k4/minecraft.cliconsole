/**
 * Abstract: CliConsolePlugin.java
 *
 * @author: fn3k4
 * @date: Apr 18, 2011
 */
package com.github.fn3k4.minecraft.cliconsole;

import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 */
public class CliConsolePlugin extends JavaPlugin {

    public static final String COMMAND_CLICONSOLEVERSION = "cliconsoleversion";

    private static Logger logger = Logger.getLogger(CliConsole.MINECRAFT_LOGGER_NAME);

    /**
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    @Override
    public void onEnable() {
        final CliConsole clinstance = CliConsole.getInstance();

        clinstance.setConfiguration(getConfiguration());

        clinstance.setMinecraftServer(((CraftServer) getServer()).getServer());

        clinstance.startStandalone();

        final PluginDescriptionFile descFile = getDescription();
        logger.info(descFile.getName() + " version " + descFile.getVersion() + " is enabled!");

    }

    /**
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    @Override
    public void onDisable() {
        final PluginDescriptionFile descFile = getDescription();
        logger.info("Disabling " + descFile.getName() + " version " + descFile.getVersion());

        final CliConsole clinstance = CliConsole.getInstance();
        clinstance.stopStandalone();
    }

    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(
     * org.bukkit.command.CommandSender,
     * org.bukkit.command.Command,
     * java.lang.String,
     * java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (COMMAND_CLICONSOLEVERSION.equalsIgnoreCase(cmd.getName())) {
            final PluginDescriptionFile descFile = getDescription();
            sender.sendMessage(ChatColor.YELLOW + descFile.getName() + " version " + descFile.getVersion());
            return true;
        }
        return false;
    }
}
