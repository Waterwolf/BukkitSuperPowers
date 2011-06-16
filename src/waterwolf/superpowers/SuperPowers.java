package waterwolf.superpowers;

/**
 * Temp changelog
 * 
 *  0.4 -> 0.5
 *   - some work to disable specific superpowers
 * 
 *  0.3 -> 0.4
 *   - ability to change flying multiplier and glowstone placing per user
 *   - hover mode
 * 
 *  0.2 -> 0.3
 *  - superpowers toggling from console
 *  - disabled location+inventory saving
 *  - enabled optional location returning
 *  - command to list sp users
 *  - glowstone is placed on flying scheduler instead of onPlayerMove which makes everything smoother!
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class SuperPowers extends JavaPlugin {
	
	SPEntityListener el = new SPEntityListener(this);
	SPBlockListener bl = new SPBlockListener(this);
	SPPlayerListener pl = new SPPlayerListener(this);

	public void onDisable() {
		for (final SPData spd : superModePlayers.values()) {
			final Block b = spd.lastGlowstone;
			if (b != null && b.getType() == Material.GLOWSTONE) {
				b.setType(Material.AIR);
			}
		}
	}
	
	public HashMap<Player, SPData> superModePlayers = new HashMap<Player, SPData>();

	PermissionHandler Permissions = null;
	
	public final static double DefaultFlyingMultiplier = 1.5;
	public final static boolean DefaultGlowStonePlacement = false;
	
	public final static boolean DefaultEnableFlying = true;
	public final static boolean DefaultEnableInstaBreak = true;
	public final static boolean DefaultEnableInstaKill = true;
	public final static boolean DefaultEnableInfiniteHP = true;
	public final static boolean DefaultEnableGlow = true;
	public final static boolean DefaultEnableItemSpawn = false;
	
	  
	public Object pModLock = new Object();
	
	Player banhammerOn = null;
	
	public void giveBanhammer(final Player p) {
	    if (banhammerOn != null) {
	        p.sendMessage("Oh noes someone else is banhammering already");
	        return;
	    }
	    
	    SPData spd;
	    synchronized (pModLock) {
	        spd = superModePlayers.get(p);
	    }
	    
	    if (spd == null)
            return;
	    
	    banhammerOn = p;
	    
	    spd.enableBANHAMMER = true;
	    getServer().broadcastMessage("OMG BANHAMMER INCOMING");
	}
	
	public void stopBanHammer(final Player p, final Player kicked) {
	    if (banhammerOn == null || !banhammerOn.equals(p))
	        return;
	    
	    
	    
	    SPData spd;
        synchronized (pModLock) {
            spd = superModePlayers.get(p);
        }
        
        if (spd == null)
            return;
        
        banhammerOn = null;
        
        spd.enableBANHAMMER = false;
        if (kicked != null) {
            getServer().broadcastMessage("lol " + kicked.getName() + " kicked");
        }
        
        for (final Location loc : spd.fireBrix) {
            loc.getBlock().setType(Material.AIR);
        }
        spd.fireBrix.clear();
	}
	
	public void onEnable() {
		
		final PluginManager pm = getServer().getPluginManager();

		pm.registerEvent(Event.Type.PLAYER_INTERACT, pl ,Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, pl ,Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, pl ,Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_PORTAL, pl, Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, pl, Priority.Normal, this);
		
		pm.registerEvent(Event.Type.BLOCK_IGNITE, bl, Priority.Normal, this);
		
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, el, Priority.Normal, this);
		
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, bl, Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Priority.Normal, this);
		
		final Plugin permissionsPlugin = getServer().getPluginManager().getPlugin("Permissions");
		
		if (this.Permissions == null) {
			if (permissionsPlugin != null) {
				this.Permissions = ((Permissions) permissionsPlugin).getHandler();
			}
			else {
				System.out.println("[SuperPowers] Couldn't find permissions plugin. Defaulting to ops.txt");
			}
		}
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Flyer(), 20, 2);
		
		System.out.println("SuperPowers v" + getDescription().getVersion() + " loaded!");
	}
	
	final static ChatColor chatCol = ChatColor.DARK_GREEN;
	final static int maxLight = 7;
	
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String commandLabel, final String[] args) {
    	if (command == null || command.getName() == null)
    		return true;
    	
    	Player player = null;
    	final String cmdName = command.getName();
    	
    	boolean consoleCommand = false;
    	
    	if (sender instanceof ConsoleCommandSender) {
    		if (cmdName.equalsIgnoreCase("superpowers")) {
    			if (args.length == 0) {
        			System.out.println("[SP] usage \"sp playername\"");
        			return true;
        		}
    			
        		final Player p = getServer().getPlayer(args[0]);
        		if (p == null) {
        			System.out.println("[SP] No player \"" + args[0] + "\" found");
        			return true;
        		}
        		
        		player = p;
        		if (args.length >= 2) {
        			args[0] = args[1]; // second arg to first arg so if tpback is given, it will be applied
        		}
        		boolean rm;
        		synchronized (pModLock) {
        		    rm = superModePlayers.containsKey(player);
        		}
        		
        		System.out.println("[SP] " + (rm ? "Removing" : "Giving") + " player superpowers");
        		if (!rm) {
        			System.out.println("Use \"sp " + args[0] + "\" to disable superpowers and \"sp " + args[0] + " tpback\" to disable+tele back.");
        		}
    		}
    		else if (cmdName.equalsIgnoreCase("listsp")) {
        		String plist = "";
        		synchronized (pModLock) {
        			for (final Player p : superModePlayers.keySet()) {
        				plist += p.getName() + " ";
        			}
        		}
    			
        		System.out.println("Players using superpowers: " + plist);
        		
        		return true;
        	}
    		else if (cmdName.equalsIgnoreCase("spdebug")) {
                
    		    final long sinceLastRun = (System.currentTimeMillis() - lastFlyerRun);
    		    
                System.out.println("LastFlyerRun: " + sinceLastRun);
                if (sinceLastRun > 2000) {
                    System.out.println("Apparently the flying thread got killed. Use \"reload plugins\" to fix flying");
                }
                
                return true;
            }
    		
    		consoleCommand = true;
    	}
    	else if (sender instanceof Player) {
    		player = (Player) sender;
    	}
    		
    	if (player == null)
    		return true;
    	
    	SPData spd = null;
    	synchronized (pModLock) {
    	    spd = superModePlayers.get(player);
    	}
    	
    	if (cmdName.equalsIgnoreCase("setflyspeed") && spd != null && spd.enableFlying) {
    		if (args.length == 1) {
    			double multiplier = 2.0;
    			try {
    				multiplier = Double.valueOf(args[0]);
    			}
    			catch (final Exception e) {
    				player.sendMessage(chatCol + args[0] + " is not a valid double value");
    				return true;
    			}
    			if (multiplier > 7 || multiplier < 0.5) {
    				player.sendMessage(chatCol + " Multiplier must be in between 0.5 and 7.0 to prevent extra lag");
    			}
    			else {
    				spd.flyingMultiplier = multiplier;
        			player.sendMessage(chatCol + " Your flying speed multiplier is now " + multiplier + ". Use \"/setflyspeed\" to default it");
    			}
    		}
    		else {
    			if (spd.flyingMultiplier == DefaultFlyingMultiplier) {
    				player.sendMessage(chatCol + " Use \"/setflyspeed flySpeedMultiplier\". For example \"/setflyspeed 2.5\"");
    			}
    			else {
    				spd.flyingMultiplier = DefaultFlyingMultiplier;
        			player.sendMessage(chatCol + " Flying multiplier set to default");
    			}
    		}
    	}
    	else if (cmdName.equalsIgnoreCase("listsp") && hasPermission(player, "superpowers.use")) {
    		String plist = "";
			for (final Player p : superModePlayers.keySet()) {
				plist += p.getName() + " ";
			}
			
			player.sendMessage(chatCol + "Players using superpowers: " + plist);
    	}
    	else if (cmdName.equalsIgnoreCase("superpowers") && (spd != null || hasPermission(player, "superpowers.use") || consoleCommand)) {
    		if (spd != null) {

    			if (spd.lastGlowstone != null) {
    				spd.lastGlowstone.setType(Material.AIR);
    				spd.lastGlowstone = null;
    			}
    			
    			final Location loc = spd.loc;
    			
    			if (args.length > 0 && args[0].equalsIgnoreCase("tpback") && loc != null) {
    				player.teleport(loc);
    			}
    			synchronized (pModLock) {
    			    superModePlayers.remove(player);
    			}
    			player.sendMessage(chatCol + " Your superpowers mode has been disabled");
    		}
    		else {
    			player.sendMessage(chatCol + " You're in superpower mode. Use shift to fly around and hold a feather to hover.");
    			player.sendMessage(chatCol + " Use \"/sp\" to disable superpowers mode and \"/sp tpback\" to disable+teleport to enabling location");
    			synchronized (pModLock) {
        			if (consoleCommand) {
                        superModePlayers.put(player, new SPData(player.getLocation()));
                    } else {
                        
                        final boolean canFly = hasPermission(player, "superpowers.fly");
                        final boolean canInstaBreak = hasPermission(player, "superpowers.instabreak");
                        final boolean canInstaKill = hasPermission(player, "superpowers.instakill");
                        final boolean infiniteHp = hasPermission(player, "superpowers.infinitehp");
                        final boolean glow = hasPermission(player, "superpowers.glow");
                        final boolean canSpawnItems = hasPermission(player, "superpowers.itemspawn");
                        
        			    superModePlayers.put(player, new SPData(player.getLocation(), canFly, canInstaBreak, canInstaKill, infiniteHp, glow, canSpawnItems));
        			}
    			}
    		}
    	}
    	else if (cmdName.startsWith("t") && spd != null) {
            
    	    final String mainCmd = cmdName.substring(1).toLowerCase();
    	    
    	    final String firstUp = uppercaseFirst(mainCmd);
    	    
    	    int cmdState = 0; // 0 = unknown 1 = enabled 2 = disabled 3 = no permissions
    	    
    	    if (mainCmd.equals("fly")) {
    	        if (spd.enableFlying) {
                    spd.enableFlying = false;
                    cmdState = 2;
                } else if (hasPermission(player, "superpowers.fly")) {
                    spd.enableFlying = true;
                    cmdState = 1;
                }
                else {
                    cmdState = 3;
                }
    	    }
    	    else if (mainCmd.equals("break")) {
                if (spd.enableInstaBreak) {
                    spd.enableInstaBreak = false;
                    cmdState = 2;
                } else if (hasPermission(player, "superpowers.instabreak")) {
                    spd.enableInstaBreak = true;
                    cmdState = 1;
                }
                else {
                    cmdState = 3;
                }
            }
    	    else if (mainCmd.equals("kill")) {
                if (spd.enableInstaKill) {
                    spd.enableInstaKill = false;
                    cmdState = 2;
                } else if (hasPermission(player, "superpowers.instakill")) {
                    spd.enableInstaKill = true;
                    cmdState = 1;
                }
                else {
                    cmdState = 3;
                }
            }
    	    else if (mainCmd.equals("hp")) {
                if (spd.enableInfiniteHP) {
                    spd.enableInfiniteHP = false;
                    cmdState = 2;
                } else if (hasPermission(player, "superpowers.infinitehp")) {
                    spd.enableInfiniteHP = true;
                    cmdState = 1;
                }
                else {
                    cmdState = 3;
                }
            }
    	    else if (mainCmd.equals("glow")) {
    	        if (spd.placeGlowstone) {
                    spd.placeGlowstone = false;
                    
                    final Block def = spd.lastGlowstone;
                    if (def != null && def.getType() == Material.GLOWSTONE) {
                        def.setType(Material.AIR);
                        spd.lastGlowstone = null;
                    }
                    
                    cmdState = 2;
                } else if (spd.enableGlow) {
                    spd.placeGlowstone = true;
                    cmdState = 1;
                }
                else {
                    cmdState = 3;
                }
    	    }
    	    
    	    switch (cmdState) {
    	        case 0:
    	            player.sendMessage(chatCol + cmdName + " not recognized as a SuperPowers toggle command");
    	            break;
    	        case 1:
                    player.sendMessage(chatCol + firstUp + " succesfully enabled");
                    break;
    	        case 2:
                    player.sendMessage(chatCol + firstUp + " succesfully disabled");
                    break;
    	        case 3:
                    player.sendMessage(ChatColor.RED + "You don't have permissions to use " + cmdName);
                    break;
    	    }
    	    return true;
        }
    	
    	if (cmdName.equalsIgnoreCase("spban") && hasPermission(player, "superpowers.banhammer")) {
    	    
    	    if (banhammerOn != null && banhammerOn.equals(player)) {
    	        stopBanHammer(player, null);
    	    } else {
                giveBanhammer(player);
            }
    	    
    	}
    	
    	if (cmdName.equalsIgnoreCase("spitem") && args.length >= 1 && spd != null && spd.enableItemSpawn) {
    		final Material mat = Material.getMaterial(args[0].toUpperCase());
    		if (mat != null) {
    			int amount = 1;
    			Player toPlayer = null;
    			if (args.length > 1 && args[1] != null) {
    				try {
    					amount = Integer.valueOf(args[1]);
    				}
    				catch (final Exception e) {}
    			}
    			if (args.length > 2 && args[2] != null) {
    			    toPlayer = getServer().getPlayer(args[2]);
    			    if (toPlayer == null) {
    			        player.sendMessage("Player \"" + args[2] + "\" not found");
    			        return true;
    			    }
    			}
    			final ItemStack is = new ItemStack(mat, amount, (short) 0);
    			final String betterName = uppercaseFirst(mat.name());
    			if (toPlayer == null) {
    			    player.getInventory().addItem(is);
                    player.sendMessage(ChatColor.DARK_AQUA + "Some " + betterName + " added");
    			}
    			else {
    			    toPlayer.getInventory().addItem(is);
    			    toPlayer.sendMessage(ChatColor.GREEN + player.getName() + " gave you some " + betterName);
                    player.sendMessage(ChatColor.YELLOW + "Some " + betterName + " added to " + toPlayer.getName() + "'s inventory");
    			}
    		}
    		else {
    			player.sendMessage("No materials found with " + args[0]);
    			String msg = "Did you mean ";
    			
    			for (final Material matt : Material.values()) {
    				if (matt.name().startsWith(args[0].toUpperCase())) {
                        msg += matt.name().toLowerCase() + " ";
                    }
    			}
    			player.sendMessage(msg);
    		}
    	}
    	
    	if (cmdName.equalsIgnoreCase("spdebug") && spd != null) {
    	    
            final long sinceLastRun = (System.currentTimeMillis() - lastFlyerRun);
            
            player.sendMessage(chatCol + "LastFlyerRun: " + sinceLastRun);
            if (sinceLastRun > 2000) {
                player.sendMessage(chatCol + "Apparently the flying thread got killed. Use \"reload plugins\" to fix flying");
            }
            
    	}
    	
        if (cmdName.equalsIgnoreCase("spwool") && args.length >= 1 && spd != null && spd.enableItemSpawn) {
            
            DyeColor col = null;
            try {
                col = DyeColor.valueOf(args[0].toUpperCase());
            }
            catch (final Exception e) {}
            
            if (col != null) {
                int amount = 1;
                if (args.length > 1 && args[1] != null) {
                    try {
                        amount = Integer.valueOf(args[1]);
                    }
                    catch (final Exception e) {}
                }
                final ItemStack is = new ItemStack(Material.WOOL, amount, (short) 0, col.getData());
                player.getInventory().addItem(is);
                player.sendMessage("Some " + col.name() + " added");
            }
            else {
                player.sendMessage("No materials found with " + args[0]);
                String msg = "Did you mean ";
                
                for (final ChatColor matt : ChatColor.values()) {
                    if (matt.name().startsWith(args[0].toUpperCase())) {
                        msg += matt.name().toLowerCase() + " ";
                    }
                }
                player.sendMessage(msg);
            }
        }
    	return true;
    }
    
    public static String uppercaseFirst(final String input) {
        if (input.length() < 2)
            return input.toUpperCase();
        final String up = input.substring(0, 1).toUpperCase();
        return up + input.substring(1).toLowerCase();
    }
    
    public boolean hasPermission (final Player p, final String permission) {
    	if (Permissions == null)
    		return p.isOp();
    	return Permissions.has(p, permission);
    }
    
    long lastFlyerRun = 0;
    
    public double distance(final Location one, final Location two) {
        return Math.sqrt(Math.pow(two.getX()-one.getX(), 2)+Math.pow(two.getY()-one.getY(), 2)+Math.pow(two.getZ()-one.getZ(), 2));
    }
    
    public class Flyer implements Runnable {

		public void run() {
			if (superModePlayers.size() == 0)
				return;
			
			final Iterator<Entry<Player, SPData>> epsa;
			synchronized (pModLock) {
			    epsa = superModePlayers.entrySet().iterator();
			}
			
			while (epsa.hasNext()) {
			    
			    final Entry<Player, SPData> eps;
			    synchronized (pModLock) {
			        eps = epsa.next();
			    }
			    
				final Player p = eps.getKey();
				
				if (!p.isOnline()) {
				    System.out.println(p.getName() + " not online anymore");
                    continue;
                }
				
				final SPData pd = eps.getValue();
				
				final Block pb = p.getLocation().getBlock();
				final byte lightLevel = pb.getLightLevel();
				
				if (p.isSneaking() && pd.enableFlying) {
					
					// HUGE credits to weasel5i2 for flying and hovering methods!!
					
				    final ItemStack item = p.getItemInHand();
				    
					if (item != null && item.getType() == Material.FEATHER) {

						/**
						 * 		hoverMultiplier = getDblSetting( "hoverMultiplier", 0.0012410253 );
								hoverBoostMultiplier = getDblSetting( "hoverBoostMultiplier", 0.00555 );
						 */
						
						//final double M = 0.0013710253;
						final double M = 0.0014910253;
					    
						final Location pl = p.getLocation();
						
						final double Y = pl.getY();
						final double X = pl.getDirection().getX();
						final double Z = pl.getDirection().getZ();
						
						
						final Vector pVector = new Vector( X, Y , Z );
						final Vector hVector = new Vector( 0.1, M, 0.1 );
						final Vector hover = pVector.multiply( hVector );
						
						p.setVelocity( new Vector( 0,0,0 ) );
						p.setVelocity( hover );
					}
					else {
					    final Location location = p.getLocation();
                        final Vector facing = location.getDirection().multiply(pd.flyingMultiplier);
                        
                        p.setVelocity( facing );
					}
				}
				else if (pd.enableGlow && pd.placeGlowstone && lightLevel < maxLight && pb.getType() == Material.AIR) {
					Block upup = pb.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getRelative(BlockFace.UP);
					final Block downdown = pb.getRelative(BlockFace.DOWN);
					
					if (downdown.getType() != Material.AIR) {// we're not on air, so glowstone can be placed
						if (upup == null || upup.getType() != Material.AIR) {
                            upup = pb; // block two blocks above current block is null or not air so let's place glowstone on current block
                        }
						upup.setType(Material.GLOWSTONE);
						if (pd.lastGlowstone != null && pd.lastGlowstone.getType() == Material.GLOWSTONE) {
                            pd.lastGlowstone.setType(Material.AIR);
                        }
						
						pd.lastGlowstone = upup;
					}
				}
				
				if (pd.enableBANHAMMER && pd.lastMove > 2) {
				    for (final Location loc : pd.fireBrix) {
                        loc.getBlock().setType(Material.AIR);
                    }
                    pd.fireBrix.clear();
				    if (pd.fireRadius > 10) {
				        pd.fireRadius = 1;
				    }
				    
				    pd.fireRadius++;
				        
				    final Location sLoc = p.getLocation();
				    pd.fireBrix.addAll(circleFrom(sLoc, pd.fireRadius, Material.FIRE));
				        
				    pd.lastMove = 0;
				}
				pd.lastMove++;
				lastFlyerRun = System.currentTimeMillis();
			}
		}
    }
    
    public ArrayList<Location> circleFrom(final Location loc, final int radius, final Material mat) {
        final ArrayList<Location> ret = new ArrayList<Location>();
        circler:
        for (int i = -180;i < 180; i+= 5) {
            final double radians = Math.toRadians(i);
            final Location targ = getDrawTarget(loc, radians, radius);
            Block bAt = targ.getBlock();
            int tries = 0;
            while (bAt != null && bAt.getY() > 2 && bAt.getType() == Material.AIR) {
                if (tries++ > 4) {
                    continue circler;
                }
                final Block sRelative = bAt.getRelative(BlockFace.DOWN);
                if (sRelative.getType() != Material.AIR) {
                    break;
                }
                bAt = sRelative;
            }
            if (bAt != null && bAt.getType() == Material.AIR) {
                bAt.setType(mat);
                ret.add(bAt.getLocation());
            }
        }
        return ret;
    }
    
    public Location getDrawTarget(final Location loc1, final double angle, final int radius) {
        final double addToOneX = Math.cos(angle);
        final double addToOneZ = Math.sin(angle);
        
        double curx = loc1.getBlockX();
        double curz = loc1.getBlockZ();
        
            curx += radius*addToOneX;
            curz += radius*addToOneZ;
            
        final Location curLoc = new Location(loc1.getWorld(), curx, loc1.getY(), curz);
        return curLoc;
    }

    public int magicTrigFunctionX (final double pointRatio){
        return (int) Math.cos(pointRatio*2D*Math.PI);
    }
    public int magicTrigFunctionY (final double pointRatio){
        return (int) Math.sin(pointRatio*2D*Math.PI);
    }
}
