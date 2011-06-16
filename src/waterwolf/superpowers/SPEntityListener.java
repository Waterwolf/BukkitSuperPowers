package waterwolf.superpowers;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SPEntityListener extends EntityListener {
	
	SuperPowers plugin;
	
	public SPEntityListener(final SuperPowers plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public void onEntityDamage(final EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player p = (Player) event.getEntity();
			final SPData spd = plugin.superModePlayers.get(p);
			if (spd != null && spd.enableInfiniteHP) {
                event.setCancelled(true);
            }
		}
		if (event instanceof EntityDamageByEntityEvent) {

			final EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent)event;
			final Entity damager = sub.getDamager();
			
			if (damager instanceof Player) {
				final SPData spd = plugin.superModePlayers.get(damager);
				final Entity damaged = sub.getEntity();
				if (spd != null && spd.enableBANHAMMER && damaged instanceof Player) {
				    plugin.stopBanHammer(((Player)damager), ((Player)damaged));
				    ((Player) damaged).kickPlayer("BANHAMMERED BY " + ((Player)damager).getName());
				}
				else if (spd != null && spd.enableInstaKill) {
                    sub.setDamage(20);
                }
			}
			
		}
	}
}
