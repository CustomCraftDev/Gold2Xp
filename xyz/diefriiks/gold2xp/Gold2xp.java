package xyz.diefriiks.gold2xp;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


public class Gold2xp extends JavaPlugin implements Listener, CommandExecutor{

	private boolean update = false;					
	private int version = 1;

	private String[] msg;
	private FileConfiguration config;
	private Object[] list;
	
	
	/*
	 *  Bukkit default onEnable function.
	 */
	@SuppressWarnings("deprecation")
	public void onEnable(){
		
		//Load configuration
		load();
		
		//Setup Events
		this.getServer().getPluginManager().registerEvents(this, this);
		
		//Setup Commands
		getCommand("g2x").setExecutor(this);
				
		//Test for Update
		update();
		if(update) {
			for(Player p : getServer().getOnlinePlayers()){
				check(p);
			}
		}
		
	}
	
	
	/*
	 *  Load function to Load the configuration file.
	 */
	private void load() {
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		list = new Object[3];
			list[0] = Material.valueOf(config.getString("event.block"));
			list[1] = Material.valueOf(config.getString("event.item"));
			list[2] = config.getInt("event.xp");
					
		msg = new String[5];
		
		msg[0] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.nopermission"));
		if(msg[0].startsWith("%no%")){
			msg[0] = "";
		}
		msg[1] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.disable"));
		if(msg[1].startsWith("%no%")){
			msg[1] = "";
		}
		msg[2] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reset"));
		if(msg[2].startsWith("%no%")){
			msg[2] = "";
		}
		msg[3] = ChatColor.translateAlternateColorCodes('&', config.getString("msg.reload"));
		if(msg[3].startsWith("%no%")){
			msg[3] = "";
		}
	}
	
	
	@EventHandler
	public void actionPerformed(PlayerInteractEvent e) {
		if(e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if(e.hasItem()) {
				if(e.getPlayer().hasPermission("g2x.use")) {
					if(e.getClickedBlock().getType() == list[0] && e.getItem().getType() == list[1]) {
						execute(e.getPlayer(), e.getItem());
					}
				}
			}
		}
	}
	

	private void execute(final Player player, ItemStack item) {
		if(item.getAmount() > 1) {
			player.setItemInHand(new ItemStack((Material) list[1], item.getAmount()-1));
		}else {
			player.setItemInHand(null);
		}
		((ExperienceOrb) player.getWorld().spawn(player.getLocation(), ExperienceOrb.class)).setExperience((int) list[2]);
	}


	@EventHandler
	public void login(PlayerLoginEvent e) {
		if(update) {
			check(e.getPlayer());
		}
	}
	
	
	private void check(Player p) {
		if(p.hasPermission("g2x.update")) {
		   	p.sendMessage(ChatColor.RED + "[Gold2Xp] ------------------------------------------------");
		   	p.sendMessage(ChatColor.RED + "[Gold2Xp] Gold2Xp is outdated. Get the new Version here:");
		   	p.sendMessage(ChatColor.RED + "[Gold2Xp] http://www.pokemon-online.xyz/plugin/Gold2Xp.jar");
		   	p.sendMessage(ChatColor.RED + "[Gold2Xp] ------------------------------------------------");
		   	this.setEnabled(false);
		}
	}


	/*
	 *  Reset function to reset the configuration file.
	 */
	public void reset() {
		File configFile = new File(getDataFolder(), "config.yml");
	    configFile.delete();
	    saveDefaultConfig();
		reload();
	}
	
	
	/*
	 *  Update function to check for updates.
	 */
	private void update() {
		String sourceLine = null;
        try {
	        URL address = new URL("http://www.pokemon-online.xyz/plugin/g2x_version.html");
	        InputStreamReader pageInput = new InputStreamReader(address.openStream());
	        BufferedReader source = new BufferedReader(pageInput);
            sourceLine = source.readLine();
        } catch (IOException e) {e.printStackTrace();}
        
	    if(sourceLine != null && Integer.parseInt(sourceLine) != version){
	    	update = true;
	    	say("[Gold2Xp] ------------------------------------------------");
	    	say("[Gold2Xp] Gold2Xp is outdated. Get the new Version here:");
	    	say("[Gold2Xp] http://www.pokemon-online.xyz/plugin/Gold2Xp.jar");
	    	say("[Gold2Xp] ------------------------------------------------");
	    	this.setEnabled(false);
	    }
		
	}
	
	
	/*
	 *  Bukkits default onConmmand function.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] split) {
		boolean isplayer = false;
		Player p = null;
		
		if(sender instanceof Player) {
			p = (Player) sender;
			isplayer = true;
		}
			
		if(cmd.getName().equalsIgnoreCase("g2x")){
				if(split.length == 1) {
					if(split[0].equalsIgnoreCase("reload")) {
						if(isplayer) {
							if(p.hasPermission("g2x.reload")) {
									reload();
								p.sendMessage(msg[3]);
								say(msg[3] + " by " + p.getName());
							}else {
								p.sendMessage(msg[0]);
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
								p.sendMessage(msg[2]);
								say(msg[2] + " by " + p.getName());
							}else {
								p.sendMessage(msg[0]);
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
								p.sendMessage(msg[1]);
								say(msg[1] + " by " + p.getName());
							}else {
								p.sendMessage(msg[0]);
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
	
	
	/*
	 *  Reload function to reload the whole Plugin.
	 */
	private void reload(){
 	   	try {
 	   		// Remove unused variables and objects
			    config = null;
			    msg = null;
			    list = null;

			// Run java garbage collector to delete unused things
			    System.gc();
			
			// load everything again
				reloadConfig();
				load();
			
 	   	} catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	
	/*
	 *  Debug function to print to console.
	 */
	public void say(String msg){
		System.out.println(ChatColor.stripColor(msg));
	}
		
    	
}
