package com.github.qumasso.arenarestorer;

import com.github.qumasso.arenarestorer.data.ArenaData;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class RestorerPlugin extends JavaPlugin implements CommandExecutor {

    private final String PERMISSION_SET_ARENA = "arenarestorer.setarena";

    private final String PERMISSION_DELETE_ARENA = "arenarestorer.deletearena";

    private final String PERMISSION_SAVE_ARENA = "arenarestorer.savearena";

    private final String PERMISSION_RESTORE_ARENA = "arenarestorer.restorearena";

    private Map<File, FileConfiguration> arenaFiles = new HashMap<>();

    private Map<String, ArenaData> arenaDatas = new HashMap<>();

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(ArenaData.class);
        for (File file : getDataFolder().listFiles()) {
            if (file.getName().equals("config.yml")) continue;
            FileConfiguration config = loadConfig(file);
            arenaDatas.put(getArenaName(file), config.getSerializable(getArenaName(file), ArenaData.class));
        }
        getCommand("setarena").setExecutor(this);
        getCommand("deletearena").setExecutor(this);
        getCommand("savearena").setExecutor(this);
        getCommand("restorearena").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            switch (command.getName()) {
                case "setarena" -> {
                    try {
                        if (!player.hasPermission(PERMISSION_SET_ARENA)) {
                            player.sendMessage(ChatColor.RED + "Sorry, but you haven't got a required permission");
                            return true;
                        }
                        if (args.length != 2) {
                            player.sendMessage(ChatColor.RED + "The arguments are required");
                            return true;
                        }
                        int number = Integer.parseInt(args[1]);
                        ArenaData data = arenaDatas.get(args[0]);
                        if (number == 1) {
                            player.sendMessage(ChatColor.GREEN + "You successfully set the first position of arena " + args[0]);
                            Location location = normalize(player.getLocation());
                            location.setY(location.getWorld().getMaxHeight() - 1);
                            if (data == null) data = new ArenaData();
                            data.setFirst(location);
                            File file = getFile(args[0]);
                            FileConfiguration config = loadConfig(file);
                            config.set(args[0], data);
                            config.save(file);
                            arenaDatas.put(args[0], data);
                            return true;
                        }
                        else if (number == 2) {
                            player.sendMessage(ChatColor.GREEN + "You successfully set the second position of arena " + args[0]);
                            Location location = normalize(player.getLocation());
                            location.setY(0);
                            if (data == null) data = new ArenaData();
                            data.setSecond(location);
                            File file = getFile(args[0]);
                            FileConfiguration config = loadConfig(file);
                            config.set(args[0], data);
                            config.save(file);
                            arenaDatas.put(args[0], data);
                            return true;
                        }
                        else {
                            player.sendMessage(ChatColor.RED + "The second argument can be only 1 or 2");
                            return true;
                        }
                    }
                    catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "The second argument can be only 1 or 2");
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                case "deletearena" -> {
                    if (!player.hasPermission(PERMISSION_DELETE_ARENA)) {
                        player.sendMessage(ChatColor.RED + "Sorry, but you haven't got a required permission");
                        return true;
                    }
                    if (args.length != 1) return false;
                    if (!hasArena(args[0])) {
                        player.sendMessage(ChatColor.RED + "There is no such name arena");
                        return true;
                    }
                    player.sendMessage(ChatColor.GREEN + "You successfully deleted the arena with name " + args[0]);
                    arenaDatas.remove(args[0]);
                    File file = getFile(args[0]);
                    arenaFiles.remove(file);
                    file.delete();
                    return true;
                }
                case "savearena" -> {
                    if (!player.hasPermission(PERMISSION_SAVE_ARENA)) {
                        player.sendMessage(ChatColor.RED + "Sorry, but you haven't got a required permission");
                        return true;
                    }
                    if (args.length != 1) return false;
                    if (!hasArena(args[0])) {
                        player.sendMessage(ChatColor.RED + "There is no such name arena");
                        return true;
                    }
                    player.sendMessage(ChatColor.GREEN + "The data of arena with name " + args[0] + " was successfully saved");
                    saveArenaData(args[0]);
                    return true;
                }
                case "restorearena" -> {
                    if (!player.hasPermission(PERMISSION_RESTORE_ARENA)) {
                        player.sendMessage(ChatColor.RED + "Sorry, but you haven't got a required permission");
                        return true;
                    }
                    if (args.length != 1) return false;
                    if (!hasArena(args[0])) {
                        player.sendMessage(ChatColor.RED + "There is no such name arena");
                        return true;
                    }
                    player.sendMessage(ChatColor.GREEN + "The data of arena with name " + args[0] + " was successfully restored");
                    restoreArenaData(args[0]);
                    return true;
                }
            }
        }
        else {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command");
            return true;
        }
        return false;
    }

    public void saveArenaData(String arenaName) {
        File arenaFile = getFile(arenaName);
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ArenaData data = arenaDatas.get(arenaName);
        FileConfiguration config = loadConfig(arenaFile);
        if (data == null) return;
        data.save();
        config.set(arenaName, data);
        try {
            config.save(arenaFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void restoreArenaData(String arenaName) {
        ArenaData data = arenaDatas.get(arenaName);
        if (data == null) return;
        data.restore();
    }

    public boolean hasArena(String arenaName) {
        return Arrays.stream(getDataFolder().listFiles()).anyMatch(x -> getArenaName(x).equals(arenaName));
    }

    private FileConfiguration loadConfig(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        arenaFiles.put(file, config);
        return config;
    }

    private File getFile(String arenaName) {
        return new File(getDataFolder().getAbsolutePath() + File.separator + arenaName + ".yml");
    }

    private Location normalize(Location location) {
        location.setYaw(0f);
        location.setPitch(0f);
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }

    private String getArenaName(File file) {
        return file.getName().substring(0, file.getName().length() - 4);
    }

}
