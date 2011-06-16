package wolf.superpowers;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SPPlayerListener extends PlayerListener {
	
	SuperPowers plugin;
	
	public SPPlayerListener(final SuperPowers plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onPlayerQuit(final PlayerQuitEvent event) {
		synchronized (plugin.pModLock) {
		    final SPData spd = plugin.superModePlayers.remove(event.getPlayer());
		    if (spd != null && spd.enableBANHAMMER) {
		        plugin.banhammerOn = null; //TODO
		    }
		        
		}
	}
	
	@Override
	public void onPlayerPortal(final PlayerPortalEvent event) {
	    final Player eventPlayer = event.getPlayer();
	    synchronized (plugin.pModLock) {
    	    final SPData spd = plugin.superModePlayers.remove(eventPlayer);
    	    if (spd != null) {
    	        plugin.superModePlayers.put(eventPlayer, spd);
    	        event.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + " Your superpowers have been returned after the world transportation");
    	    }
	    }
	}
	
	
	@Override
    public void onItemHeldChange(final PlayerItemHeldEvent event) {
	    synchronized (plugin.pModLock) {
            final Player eventPlayer = event.getPlayer();
            final SPData spd = plugin.superModePlayers.get(eventPlayer);
            if (spd != null && spd.enableBANHAMMER) {
                plugin.stopBanHammer(eventPlayer, null);
                event.getPlayer().sendMessage(ChatColor.RED + " Your banhammer has been disabled");
            }
        }
	}
	

	@Override
	public void onPlayerInteract(final PlayerInteractEvent event) {
	    
	   
		if (event.getAction() != Action.RIGHT_CLICK_AIR)
			return;
		/*
		final Block clickedBlock = event.getPlayer().getTargetBlock(null, 50);
        final ItemStack item = event.getItem();

        if (clickedBlock != null && item != null) {
            final BlockFace face = faceTo(event.getPlayer(), clickedBlock);
            final Block target = clickedBlock.getFace(face);
            if (target != null) {
                target.setType(item.getType());
            }
            System.out.println(face + " " + target);
        }
        System.out.println(clickedBlock + " " + item);
        
		*/
		/*
		final ItemStack item = event.getItem();
		if (item.getType() == Material.BOW)
			return;

		if (!plugin.superModePlayers.containsKey(event.getPlayer()))
			return;

		final boolean setto = !plugin.superModePlayers.get(event.getPlayer()).flying;
		event.getPlayer().sendMessage(SuperPowers.chatCol + "You are now in " + (setto ? "flying" : "hovering") + " mode");
		plugin.superModePlayers.get(event.getPlayer()).flying = setto;
		*/
	}
	
	public BlockFace faceTo(final Player player, final Block b) {
	    final float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - b.getX(), b.getZ() - player.getLocation().getBlockZ()));
	    return getClosestFace(dir);
	}
	
	public final BlockFace getClosestFace(float direction){

        direction = direction % 360;

        if(direction < 0) {
            direction += 360;
        }

        direction = Math.round(direction / 45);

        switch((int)direction){

            case 0:
                return BlockFace.WEST;
            case 1:
                return BlockFace.NORTH_WEST;
            case 2:
                return BlockFace.NORTH;
            case 3:
                return BlockFace.NORTH_EAST;
            case 4:
                return BlockFace.EAST;
            case 5:
                return BlockFace.SOUTH_EAST;
            case 6:
                return BlockFace.SOUTH;
            case 7:
                return BlockFace.SOUTH_WEST;
            default:
                return BlockFace.WEST;

        }
    }
	
	/*
     * 
     * horiz only http://forums.bukkit.org/threads/from-which-direction-is-player-looking-at-the-block.14153/#post-235823
    
    */
}
