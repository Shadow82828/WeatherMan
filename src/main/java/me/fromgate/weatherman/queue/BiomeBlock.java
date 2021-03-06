/*  
 *  WeatherMan, Minecraft bukkit plugin
 *  ©2012-2018, fromgate, fromgate@gmail.com
 *  https://www.spigotmc.org/resources/weatherman.43379/
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

package me.fromgate.weatherman.queue;

import me.fromgate.weatherman.util.NmsUtil;
import me.fromgate.weatherman.util.Repopulator;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

public class BiomeBlock {
    World world;
    int x;
    int z;
    WMChunk wmChunk;

    Biome biome;
    //boolean setBiomeOrDepopulate = true; //true - setBiome, false - depopulate


    public BiomeBlock(Location loc, Biome b) {
        this.world = loc.getWorld();
        this.x = loc.getBlockX();
        this.z = loc.getBlockZ();
        this.biome = b;
        this.wmChunk = new WMChunk(this.world, this.x, this.z, true);
    }

    public Biome getBiome() {
        return this.biome;
    }

    public BiomeBlock(World world, int x, int z, Biome b) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.biome = b;
        this.wmChunk = new WMChunk(this.world, this.x, this.z, true);
    }

    public void processBiomeBlock(boolean setBiomeOrDepopulate) {
        if (setBiomeOrDepopulate) this.setBiome();
        else Repopulator.depopulateColumn(this.getLocation());
    }

    private void setBiome() {
        Biome biome = this.biome;
        if (this.biome == null) biome = NmsUtil.getOriginalBiome(this.x, this.z, this.world);
        if (biome != null) this.world.setBiome(this.x, this.z, biome);
    }

    public Location getLocation() {
        return new Location(world, x, 0, z);
    }

    public boolean isInChunk(Chunk ch) {
        return (getLocation().getChunk().equals(ch));
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((world == null) ? 0 : world.hashCode());
        result = prime * result + x;
        result = prime * result + z;
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BiomeBlock)) {
            return false;
        }
        BiomeBlock other = (BiomeBlock) obj;
        if (world == null) {
            if (other.world != null) {
                return false;
            }
        } else if (!world.equals(other.world)) {
            return false;
        }
        if (x != other.x) {
            return false;
        }
        return z == other.z;
    }


}
