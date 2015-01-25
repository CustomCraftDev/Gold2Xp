package xyz.diefriiks.gold2xp;

import java.io.File;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Gold2xp extends JavaPlugin implements Listener, CommandExecutor{

	protected boolean update = false;
	protected String prefix;
	private String[] msg;
	private FileConfiguration config;
	private Object[] list;
	private boolean drop;

	public void onEnable() {
		load();
		new Updater(this);
		this.getServer().getPluginManager().registerEvents(this, this);
		getCommand("g2x").setExecutor(this);
	}

	private void load() {
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		list = new Object[3];
			list[0] = Material.valueOf(config.getString("event.block"));
			list[1] = Material.valueOf(config.getString("event.item"));
			list[2] = config.getInt("event.xp");
			
		drop = config.getBoolean("event.location");
					
		msg = new String[5];
		
		msg[0] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.nopermission"));
		msg[1] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.disable"));
		msg[2] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reset"));
		msg[3] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reload"));
		msg[4] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.wrongitem"));
		prefix = ChatColor.translateAlternateColorCodes('&', config.getString("msg.prefix"));
	}
	
	@EventHandler
	public void actionPerformed(PlayerInteractEvent e) {
		if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.hasItem()) {
				if(e.getClickedBlock().getType() == list[0]) {
					if(e.getPlayer().hasPermission("g2x.use")) {
						if(e.getItem().getType() == list[1]) {
							execute(e.getPlayer(), e.getItem(), e.getClickedBlock());
						}else {
							e.getPlayer().sendMessage(prefix + msg[4]);
						}
					}
				}
			}
		}
	}

	private void execute(final Player player, ItemStack item, Block block) {
		if(item.getAmount() > 1) {
			player.setItemInHand(new ItemStack((Material) list[1], item.getAmount()-1));
		}else {
			player.setItemInHand(null);
		}
		if(drop) {
			Location center = block.getLocation().add(0.5, 1.0, 0.5);
			((ExperienceOrb) player.getWorld().spawn(center, ExperienceOrb.class)).setExperience((int) list[2]);
		}else {
			((ExperienceOrb) player.getWorld().spawn(player.getLocation(), ExperienceOrb.class)).setExperience((int) list[2]);
		}
	}

	public void reset() {
		File configFile = new File(getDataFolder(), "config.yml");
	    configFile.delete();
	    saveDefaultConfig();
		reload();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] split) {
		boolean isplayer = false;
		Player p = null;
		
		if(sender instanceof Player) {
			p = (Player) sender;
			isplayer = true;
			
			if(update) {
				tell(p);
			}
		}
		
		if(cmd.getName().equalsIgnoreCase("g2x")){
				if(split.length == 1) {
					if(split[0].equalsIgnoreCase("reload")) {
						if(isplayer) {
							if(p.hasPermission("g2x.reload")) {
									reload();
								p.sendMessage(prefix + msg[3]);
								say(msg[3] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + msg[0]);
							}
						}else {
								reload();
							say(msg[3] + " by Console");
						}
						return true;
					}else if(split[0].equalsIgnoreCase("reset")) {
						if(isplayer) {
							if(p.hasPermission("g2x.reset")) {
									reset();
								p.sendMessage(prefix + msg[2]);
								say(msg[2] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + prefix + msg[0]);
							}
						}else {
								reset();
							say(msg[2] + " by Console");
						}
						return true;
					}else if(split[0].equalsIgnoreCase("disable")) {
						if(isplayer) {
							if(p.hasPermission("g2x.disable")) {
									this.setEnabled(false);
								p.sendMessage(prefix + msg[1]);
								say(msg[1] + " by " + p.getName());
							}else {
								p.sendMessage(prefix + msg[0]);
							}
						}else {
								this.setEnabled(false);
							say(msg[1] + " by Console");
						}
						return true;
					}
				}
		}
		return false;
	}

	private void reload(){
 	   	try {
			reloadConfig();
			load();
 	   	} catch (Exception e) {}
	}

	public void say(String msg){
		System.out.println(ChatColor.stripColor(prefix + msg));
	}

	public void tell(Player p) {
		   	p.sendMessage(prefix + "-------------------------------------------------");
		   	p.sendMessage(prefix + "Gold2Xp is outdated. Get the new Version here:");
		   	p.sendMessage(prefix + "http://www.pokemon-online.xyz/plugin");
		   	p.sendMessage(prefix + "-------------------------------------------------");
	}
			
}
