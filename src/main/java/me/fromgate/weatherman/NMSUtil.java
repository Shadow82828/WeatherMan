/*  
 *  WeatherMan, Minecraft bukkit plugin
 *  (c)2012-2016, fromgate, fromgate@gmail.com
 *  http://dev.bukkit.org/server-mods/weatherman/
 *    
 *  This file is part of WeatherMan.
 *  
 *  WeatherMan is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  WeatherMan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with WeatherMan.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */


package me.fromgate.weatherman;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class NMSUtil {

    private static Logger log;
    private static String [] tested_versions = {"v1_9_R1"};
    private static String version ="";
    private static boolean blocked = false;
    private static String cboPrefix = "org.bukkit.craftbukkit.";
    private static String nmsPrefix = "net.minecraft.server.";
    private static Class<?> CraftWorld;
    private static Method craftWorld_getHandle;
    private static Class<?> NmsWorld;
    private static Class<?> NmsWorldServer;
    private static Field field_worldProvider;
    private static Class<?> WorldProvider;
    private static Class<?> WorldChunkManager;
    private static Field field_WorldProvider_d;
    private static Method getBiome;
    private static Class<?> CraftBlock;
    private static Class<?> BiomeBase;
    private static Method biomeBaseToBiome;
    private static Method biomeToBiomeBase;
    private static Method BiomeBase_getTemperature;
    private static Class<?> CraftChunk;
    private static Method craftChunk_getHandle;
    private static Field field_NmsChunk_done;
    private static Field nms_chunk_world;
    private static Method getChunkProvider;
    private static Class<?> NmsChunk;
    private static Class<?> ChunkProviderServer;
    private static Method saveChunk;
    private static Method saveChunkNOP;
    private static Class<?> BlockPosition;
    private static Constructor<?> constructBlockPosition;
    private static Class<?> CraftPlayer;
    private static Method craftPlayer_getHandle;
    private static Class<?> Packet;
    private static Class<?> PacketPlayOutMapChunk;
    private static Constructor<?> newPacket;
    private static Class<?> EntityPlayer;
    private static Field playerConnection;
    private static Class<?> PlayerConnection;
    private static Method sendPacket;

    public static void init(){
        log = Logger.getLogger("Minecraft");
        try{
            Object s = Bukkit.getServer();
            Method m = s.getClass().getMethod("getHandle");
            Object cs = m.invoke(s);
            String className = cs.getClass().getName();
            String [] v = className.split("\\.");
            if (v.length==5){
                version = v[3];
                cboPrefix = "org.bukkit.craftbukkit."+version+".";
                nmsPrefix = "net.minecraft.server."+version+".";;
            }
        } catch (Exception e){
            e.printStackTrace();
        }


        try {
            CraftWorld = cboClass("CraftWorld");
            craftWorld_getHandle = CraftWorld.getMethod("getHandle");
            NmsWorld = nmsClass("World");
            field_worldProvider = NmsWorld.getDeclaredField("worldProvider");
            WorldProvider = nmsClass("WorldProvider");
            field_WorldProvider_d = WorldProvider.getDeclaredField("c");
            field_WorldProvider_d.setAccessible(true);
            BlockPosition = nmsClass("BlockPosition");
            constructBlockPosition = BlockPosition.getConstructor(int.class,int.class,int.class);
            WorldChunkManager = nmsClass("WorldChunkManager");
            getBiome = WorldChunkManager.getDeclaredMethod("getBiome", BlockPosition);
            CraftBlock = cboClass("block.CraftBlock");
            BiomeBase = nmsClass("BiomeBase");
            biomeBaseToBiome = CraftBlock.getDeclaredMethod("biomeBaseToBiome", BiomeBase);
            biomeToBiomeBase= CraftBlock.getDeclaredMethod("biomeToBiomeBase", Biome.class);
            BiomeBase_getTemperature = BiomeBase.getDeclaredMethod("getTemperature");
            CraftChunk = cboClass("CraftChunk");
            craftChunk_getHandle = CraftChunk.getMethod("getHandle");
            NmsChunk=nmsClass("Chunk");
            field_NmsChunk_done = NmsChunk.getDeclaredField("done");
            nms_chunk_world = NmsChunk.getDeclaredField("world");
            NmsWorldServer = nmsClass("WorldServer");
            getChunkProvider = NmsWorldServer.getMethod("getChunkProvider");
            ChunkProviderServer = nmsClass("ChunkProviderServer");
            saveChunk = ChunkProviderServer.getDeclaredMethod("saveChunk",NmsChunk);
            saveChunkNOP = ChunkProviderServer.getDeclaredMethod("saveChunkNOP",NmsChunk);
            CraftPlayer= cboClass("entity.CraftPlayer");
            craftPlayer_getHandle = CraftPlayer.getMethod("getHandle");
            EntityPlayer = nmsClass("EntityPlayer");
            playerConnection = EntityPlayer.getField("playerConnection");
            Packet = nmsClass("Packet");
            PacketPlayOutMapChunk = nmsClass("PacketPlayOutMapChunk");
            newPacket = PacketPlayOutMapChunk.getConstructor(NmsChunk, boolean.class, int.class);
            PlayerConnection = nmsClass("PlayerConnection");
            sendPacket = PlayerConnection.getMethod("sendPacket", Packet);
        }catch (Exception e){
            blocked = true;
            log.info("[WeatherMan] his version of WeatherMan is not compatible with CraftBukkit "+Bukkit.getVersion());
            log.info("[WeatherMan] Features depended to craftbukkit version will be disabled!");
            log.info("[WeatherMan] + It is strongly recommended to update WeatherMan to latest version!");
            log.info("[WeatherMan] + Check updates at http://dev.bukkit.org/server-mods/weatherman/");
            log.info("[WeatherMan] + or use this version at your own risk.");
            e.printStackTrace();
        }
        if ((!blocked)&&(!isTestedVersion())){
            log.info("[WeatherMan] +-------------------------------------------------------------------+");
            log.info("[WeatherMan] + This version of WeatherMan was not tested with CraftBukkit "+getMinecraftVersion().replace('_', '.')+" +");
            log.info("[WeatherMan] + Check updates at http://dev.bukkit.org/server-mods/weatherman/    +");
            log.info("[WeatherMan] + or use this version at your own risk                              +");
            log.info("[WeatherMan] +-------------------------------------------------------------------+");
        }
    }
    private static Class<?> nmsClass(String classname) throws Exception{
        return Class.forName(nmsPrefix+classname);
    }

    private static Class<?> cboClass(String classname) throws Exception{
        return Class.forName(cboPrefix+classname);
    }

    public static String getMinecraftVersion(){
        return version;
    }

    public static boolean isTestedVersion(){
        for (int i = 0; i< tested_versions.length;i++){
            if (tested_versions[i].equalsIgnoreCase(version)) return true;
        }
        return false;
    }

    public static boolean isBlocked(){
        return blocked;
    }

    public static Biome getOriginalBiome (Location loc){
        return getOriginalBiome (loc.getBlockX(), loc.getBlockZ(), loc.getWorld());
    }

    public static double getBiomeTemperature(Biome biome){
        if (blocked) return 100;
        try {
            Object biomebase = biomeToBiomeBase.invoke(null, biome);
            Object temperature = BiomeBase_getTemperature.invoke(biomebase);
            return (Double) temperature;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 100;
    }

    public static Biome getOriginalBiome (int x, int z, World w){
        if (blocked) return null;
        try {
            Object nmsWorldServer = craftWorld_getHandle.invoke(w);
            Object worldProvider = field_worldProvider.get(nmsWorldServer);
            Object d = field_WorldProvider_d.get(worldProvider);
            Object blockPosition = constructBlockPosition.newInstance(x,0,z);
            Object biomeBase = getBiome.invoke(d, blockPosition);
            Object biome = biomeBaseToBiome.invoke(null, biomeBase);
            Biome b = (Biome) biome;
            return b;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void repopulateChunk(Chunk chunk){
        if (blocked) return;
        try{
            Object craftchunk = CraftChunk.cast(chunk);
            Object nmsChunk = craftChunk_getHandle.invoke(craftchunk);
            field_NmsChunk_done.setAccessible(true);
            field_NmsChunk_done.set(nmsChunk, false);
            refreshChunk (chunk);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void saveChunk(Chunk ch){
        if (blocked) return;
        if (ch == null) WeatherMan.instance.getLogger().info("!!!!! NULLL");
        try {
            Object nms_chunk = craftChunk_getHandle.invoke(ch);
            Object nms_world = nms_chunk_world.get(nms_chunk);
            getChunkProvider.invoke(nms_world);
            Object chunkProvider = getChunkProvider.invoke(nms_world);
            saveChunk.invoke(chunkProvider,nms_chunk);
            saveChunkNOP.invoke(chunkProvider,nms_chunk);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void refreshChunk(Chunk ch){
        World w = ch.getWorld();
        for (Player p : w.getPlayers()) {
            if (p.isOnline()) {
                Location loc = ch.getBlock(7, p.getLocation().getBlockY(), 7).getLocation();
                if (p.getLocation().distance(loc) <= Bukkit.getServer().getViewDistance() * 16) {
                    try {
                        Object nmsPlayer = craftPlayer_getHandle.invoke(p);
                        Object nmsChunk = craftChunk_getHandle.invoke(ch);
                        Object nmsPlayerConnection = playerConnection.get(nmsPlayer);
                        Object chunkPacket = newPacket.newInstance(nmsChunk,true,65535);
                        sendPacket.invoke(nmsPlayerConnection, chunkPacket);
                    } catch (Exception e) {
                    }
                }
            }
            ch.unload(true);
            ch.load();
        }
    }
}