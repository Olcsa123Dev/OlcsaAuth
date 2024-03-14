package me.olcsa.auth;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

public class Main extends JavaPlugin implements Listener {

    private FileConfiguration config;
    private Set<String> authenticatedPlayers;

    @Override
    public void onEnable() {
        
        config = getConfig();

       
        getServer().getPluginManager().registerEvents(this, this);

        
        authenticatedPlayers = new HashSet<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("register")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 1) {
                    String password = args[0];
                    register(player, password); 
                    return true;
                } else {
                    player.sendMessage("Használat: /register <jelszó>");
                }
            } else {
                sender.sendMessage("Ezt a parancsot csak játékosok használhatják!");
            }
        } else if (command.getName().equalsIgnoreCase("login")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 1) {
                    String password = args[0];
                    if (authenticate(player, password)) {
                        authenticatedPlayers.add(player.getName()); 
                        player.sendMessage("Sikeres bejelentkezés!");
                        return true;
                    } else {
                        player.sendMessage("Hibás jelszó! Kérlek próbáld újra.");
                    }
                } else {
                    player.sendMessage("Használat: /login <jelszó>");
                }
            } else {
                sender.sendMessage("Ezt a parancsot csak játékosok használhatják!");
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!config.contains("players." + player.getName())) {
            player.sendMessage("A belépéshez regisztrálj a /register parancs segítségével!");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!authenticatedPlayers.contains(player.getName())) {
            event.setCancelled(true); 
            player.sendMessage("A mozgás csak regisztráció vagy bejelentkezés után lehetséges!");
        }
    }

    private void register(Player player, String password) {
        config.set("players." + player.getName(), password);
        saveConfig(); 
        authenticatedPlayers.add(player.getName());
        player.sendMessage("Sikeres regisztráció!");
    }

    private boolean authenticate(Player player, String password) {
        String storedPassword = config.getString("players." + player.getName());
        return storedPassword != null && storedPassword.equals(password);
    }
}
