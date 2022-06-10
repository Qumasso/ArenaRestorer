package com.github.qumasso.arenarestorer.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class ArenaData implements ConfigurationSerializable {

    private final String AIR_DATA_STRING = cutNamespace(Bukkit.createBlockData("minecraft:air").getAsString());

    private final BlockData AIR_BLOCK_DATA = Bukkit.createBlockData("minecraft:air");

    public static ArenaData deserialize(Map<String, Object> map) {
        ArenaData arenaData = new ArenaData();
        arenaData.first = (Location) map.get("1");
        arenaData.second = (Location) map.get("2");
        String datas = (String) map.get("data");
        if (arenaData.first == null || arenaData.second == null || datas == null) return arenaData;
        Location first = arenaData.first, second = arenaData.second;
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        BlockData[][][] data = new BlockData[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
        String[] splitted = datas.split(" ");
        splitted[0] = splitted[0].substring(1);
        splitted[splitted.length - 1] = splitted[splitted.length - 1].substring(0, splitted[splitted.length - 1].length() - 1);
        int i = 0;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (i == splitted.length) continue;
                    String currentData = splitted[i++];
                    if (currentData.equals("")) {
                        data[x - minX][y - minY][z - minZ] = arenaData.AIR_BLOCK_DATA;
                        continue;
                    }
                    data[x - minX][y - minY][z - minZ] = Bukkit.createBlockData(arenaData.addNamespace(currentData));
                }
            }
        }
        arenaData.data = data;
        return arenaData;
    }

    private Location first, second;

    private BlockData[][][] data;

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        if (first != null) map.put("1", first);
        if (second != null) map.put("2", second);
        if (data == null) return map;
        StringBuilder builder = new StringBuilder();
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        if (first.getWorld() != second.getWorld()) return map;
        save();
        builder.append("{");
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    String blockDataString = cutNamespace(data[x - minX][y - minY][z - minZ].getAsString());
                    if (blockDataString.equals(AIR_DATA_STRING)) {
                        builder.append(" ");
                        if (x == maxX && y == maxY && z == maxZ) builder.append("}");
                        continue;
                    }
                    else builder.append(cutNamespace(data[x - minX][y - minY][z - minZ].getAsString()));
                    if (x == maxX && y == maxY && z == maxZ) builder.append("}");
                    else builder.append(" ");
                }
            }
        }
        map.put("data", builder.toString());
        return map;
    }

    public void save() {
        if (first == null || second == null) return;
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        World world = first.getWorld();
        data = new BlockData[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    data[x - minX][y - minY][z - minZ] = world.getBlockData(x, y, z);
                }
            }
        }
    }

    public void restore() {
        if (first == null || second == null || data == null) return;
        int minX = Math.min(first.getBlockX(), second.getBlockX());
        int minY = Math.min(first.getBlockY(), second.getBlockY());
        int minZ = Math.min(first.getBlockZ(), second.getBlockZ());
        int maxX = Math.max(first.getBlockX(), second.getBlockX());
        int maxY = Math.max(first.getBlockY(), second.getBlockY());
        int maxZ = Math.max(first.getBlockZ(), second.getBlockZ());
        World world = first.getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    world.setBlockData(x, y, z, data[x - minX][y - minY][z - minZ]);
                }
            }
        }
    }

    public Location getFirst() {
        return first;
    }

    public void setFirst(Location first) {
        this.first = first;
    }

    public Location getSecond() {
        return second;
    }

    public void setSecond(Location second) {
        this.second = second;
    }

    private String cutNamespace(String data) {
        return data.substring(10);
    }

    private String addNamespace(String data) {
        return "minecraft:" + data;
    }

}
