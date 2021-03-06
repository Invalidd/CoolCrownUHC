package com.coolcrown.UHC;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CoolCrownUHC extends JavaPlugin {
    public static double HEAL_RADIUS = 10;
    public static double HEAL_RATE = 0.0005;
    public static ArrayList<UUID> injured = new ArrayList<UUID>();
    private final Death deathListener = new Death();
    public void onEnable(){
        Bukkit.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "CoolCrownUHC plugin enabled!");
        Bukkit.getPluginManager().registerEvents(deathListener, this);

        this.getCommand("healradius").setExecutor(new Radius());
        this.getCommand("healrate").setExecutor(new Rate());

        //Running player healer
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
            public void run() {
                for(Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (p.getGameMode() != GameMode.SURVIVAL) continue;
                    if (p.getHealth() > 20.00 - HEAL_RATE || p.getHealth() <= 0) continue;

                    //Getting all players in 10 block radius
                    List<Entity> playersInRadius = p.getNearbyEntities(HEAL_RADIUS, HEAL_RADIUS, HEAL_RADIUS);
                    Team playerTeam = null;
                    for (Team t: p.getScoreboard().getTeams()){
                            if (t.hasEntry(p.getDisplayName())){
                                playerTeam = t;
                        }
                    }

                    if (playerTeam == null) continue;
                    for (Entity entity : playersInRadius){
                        if (entity instanceof Player) {
                            Player radiusPlayer = (Player) entity;

                            if (injured.contains(radiusPlayer.getUniqueId())) continue;
                            if (radiusPlayer.getGameMode() != GameMode.SURVIVAL) continue;

                            if (playerTeam.hasEntry(radiusPlayer.getDisplayName()) &&
                                    p.getHealth() < radiusPlayer.getHealth() - HEAL_RATE ) {
                                    p.setHealth(p.getHealth() + HEAL_RATE);
                            }
                        }

                    }

                }

                for (BossBar progress: Death.progressTrackers.values()) {
                    progress.setProgress(Math.max(progress.getProgress() - 0.005, 0));
                }
            }

        }, 0L, 1L);
    }

}
