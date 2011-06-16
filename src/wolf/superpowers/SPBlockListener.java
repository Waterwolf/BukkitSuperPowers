package wolf.superpowers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockListener;

public class SPBlockListener extends BlockListener {
	
	SuperPowers plugin;
	
	public SPBlockListener(final SuperPowers plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onBlockDamage(final BlockDamageEvent event) {
		final SPData spd = plugin.superModePlayers.get(event.getPlayer());
		if (spd != null && spd.enableInstaBreak) {
			event.setInstaBreak(true);
		}
	}
	
	@Override
    public void onBlockIgnite(final BlockIgniteEvent event) {
        if (plugin.banhammerOn != null) {
            event.setCancelled(true);
        }
    }
	
	
	public boolean wasGlowstone(final Block b) {
		if (b == null)
			return false;
		for (final SPData spd : plugin.superModePlayers.values()) {
			if (b.equals(spd.lastGlowstone))
				return true;
		}
		return false;
	}
	
	@Override
	public void onBlockBreak(final BlockBreakEvent event) {
		if (event.getBlock().getType() == Material.GLOWSTONE && wasGlowstone(event.getBlock())) {
			
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			
		}
	}
}
