package waterwolf.superpowers;

import static waterwolf.superpowers.SuperPowers.DefaultEnableFlying;
import static waterwolf.superpowers.SuperPowers.DefaultEnableGlow;
import static waterwolf.superpowers.SuperPowers.DefaultEnableInfiniteHP;
import static waterwolf.superpowers.SuperPowers.DefaultEnableInstaBreak;
import static waterwolf.superpowers.SuperPowers.DefaultEnableInstaKill;
import static waterwolf.superpowers.SuperPowers.DefaultEnableItemSpawn;
import static waterwolf.superpowers.SuperPowers.DefaultFlyingMultiplier;
import static waterwolf.superpowers.SuperPowers.DefaultGlowStonePlacement;

import java.util.Vector;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class SPData {

    Location loc;
	double flyingMultiplier;
	boolean placeGlowstone;
	
	Block lastGlowstone = null;
	
	// perms start
	
	boolean enableFlying;
	boolean enableInstaBreak;
	boolean enableInstaKill;
	boolean enableInfiniteHP;
	boolean enableGlow;
	boolean enableItemSpawn;
	
	boolean enableBANHAMMER;
	Vector<Location> fireBrix = new Vector<Location>();
	int fireRadius = 0;
	
	int lastMove = 0;
	
	// perms end
	
	public SPData (final Location loc) {
	    this.loc = loc;
		this.flyingMultiplier = DefaultFlyingMultiplier;
		this.placeGlowstone = DefaultGlowStonePlacement;
		
		this.enableFlying = DefaultEnableFlying;
		this.enableInstaBreak = DefaultEnableInstaBreak;
		this.enableInstaKill = DefaultEnableInstaKill;
		this.enableInfiniteHP = DefaultEnableInfiniteHP;
		this.enableGlow = DefaultEnableGlow;
		this.enableItemSpawn = DefaultEnableItemSpawn;
		this.enableBANHAMMER = false;
	}
	
	public SPData (final Location loc, final boolean enableFlying, final boolean enableInstaBreak, final boolean enableInstaKill, final boolean enableInfiniteHp, final boolean enableGlow, final boolean enableItemSpawn) {
        this.loc = loc;
        
        this.flyingMultiplier = DefaultFlyingMultiplier;
        this.placeGlowstone = DefaultGlowStonePlacement;
        
        this.enableFlying = enableFlying;
        this.enableInstaBreak = enableInstaBreak;
        this.enableInstaKill = enableInstaKill;
        this.enableInfiniteHP = enableInfiniteHp;
        this.enableGlow = enableGlow;
        this.enableItemSpawn = enableItemSpawn;
        this.enableBANHAMMER = false;
    }
}