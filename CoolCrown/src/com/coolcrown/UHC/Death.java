package com.coolcrown.UHC;

import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.PacketPlayOutEntityStatus;
import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;


public class Death implements Listener {
    HashMap<UUID, Float> saturation = new HashMap<UUID, Float>();
    HashMap<UUID, Integer> foodLevel = new HashMap<UUID, Integer>();
    HashMap<UUID, String> deathMessages = new HashMap<UUID, String>();
    public static HashMap<UUID, BossBar> progressTrackers = new HashMap<UUID, BossBar>();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        if (CoolCrownUHC.injured.contains(player.getUniqueId())){
            event.setDeathMessage(deathMessages.get(player.getUniqueId()));
            deathMessages.remove(player.getUniqueId());

            player.setSneaking(false);
            player.setGlowing(false);

            CoolCrownUHC.injured.remove(player.getUniqueId());
            progressTrackers.get(player.getUniqueId()).removeAll();
            progressTrackers.remove(player.getUniqueId());

            player.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You died!",
                    ChatColor.BLUE + "Please join the spectator call. Remember to not ghost as a spectator!",
                    0,120,20);

            for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 200, 3);
            }

            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        BossBar progress = Bukkit.createBossBar(player.getDisplayName() + "'s Revival Progress",
                BarColor.RED, BarStyle.SOLID);
        progress.setProgress(0);
        progress.addPlayer(player);

        CoolCrownUHC.injured.add(player.getUniqueId());
        progressTrackers.put(player.getUniqueId(), progress);
        deathMessages.put(player.getUniqueId(), event.getDeathMessage());

        Team playerTeam = null;

        for (Team t: player.getScoreboard().getTeams()){
            if (t.hasEntry(player.getDisplayName())){
                playerTeam = t;
                break;
            }
        }

        boolean teamEliminated = true;
        for (String name: playerTeam.getEntries()) {
            Player teamMember = null;
            try {
                teamMember = Bukkit.getPlayer(name);
            } catch (Exception e) {
                continue;
            }

            if (teamMember.getGameMode() != GameMode.SPECTATOR &&
                    !CoolCrownUHC.injured.contains(teamMember.getUniqueId())) {
                teamEliminated = false;
            }
        }

        if (teamEliminated) {
            for (String name : playerTeam.getEntries()) {
                Player teamMember = null;
                try {
                    teamMember = Bukkit.getPlayer(name);
                } catch (Exception e) {
                    continue;
                }

                if (CoolCrownUHC.injured.contains(teamMember.getUniqueId())) {
                    teamMember.setHealth(0);
                }
            }

            event.setDeathMessage(null);
            return;
        }

        player.sendTitle(ChatColor.DARK_RED + "" + ChatColor.BOLD + "You're injured!" ,
                ChatColor.GOLD + "A team member can revive you with 7 hearts and a golden block!",
                0,120,20);
        player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 200, 0);
        event.setDeathMessage(null);

        saturation.put(player.getUniqueId(), player.getSaturation());
        foodLevel.put(player.getUniqueId(), player.getFoodLevel());

        player.setHealth(20);
        player.setFoodLevel(1);
        player.setGlowing(true);
        player.setSneaking(true);

        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 10000000, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10000000, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 10000000, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 10));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 10000000, 255));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 10000000, 255));

        event.setKeepInventory(true);
        event.getDrops().clear();

        event.setKeepLevel(true);
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (entity instanceof Player) {
            if (CoolCrownUHC.injured.contains(entity.getUniqueId())) {
                Player injured = (Player) event.getRightClicked();
                BossBar progress = progressTrackers.get(injured.getUniqueId());

                if (CoolCrownUHC.injured.contains(player.getUniqueId())) return;
                Team playerTeam = null;

                for (Team t: player.getScoreboard().getTeams()){
                    if (t.hasEntry(player.getDisplayName())){
                        playerTeam = t;
                        break;
                    }
                }

                if (!playerTeam.hasEntry(injured.getDisplayName())) return;

                if (player.getHealth() < 13.75){
                    player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "" ,
                            ChatColor.GOLD + "You need to have at least 7 hearts to revive a player!",
                            0,50,20);
                    return;
                }

                if (player.getInventory().getItemInMainHand().getType() != Material.GOLD_BLOCK &&
                        player.getInventory().getItemInOffHand().getType() != Material.GOLD_BLOCK)
                {
                    player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "",
                            ChatColor.GOLD + "You need to hold a gold block to revive a player!",
                            0,50,20);
                    return;
                }

                progress.addPlayer(player);
                progress.setProgress(Math.min(progress.getProgress() + 0.02, 1));

                injured.getWorld().spawnParticle(Particle.HEART,
                        injured.getLocation().add((
                                new Random().nextFloat() * 2) - 1,
                                1,
                                (new Random().nextFloat() * 2) - 1), 1
                );

                if (progress.getProgress() == 1) {
                    for (PotionEffect pe : injured.getActivePotionEffects()) {
                        injured.removePotionEffect(pe.getType());
                    }

                    player.setHealth(player.getHealth() - 6);
                    injured.setHealth(1);
                    injured.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 20, 0));
                    injured.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 5 * 20, 1));

                    injured.setSneaking(false);
                    injured.setGlowing(false);

                    EntityPlayer ep = ((CraftPlayer)injured).getHandle();
                    PacketPlayOutEntityStatus status = new PacketPlayOutEntityStatus(ep, (byte) 35);
                    ep.playerConnection.sendPacket(status);

                    injured.sendTitle(ChatColor.GREEN + "" +ChatColor.BOLD + "" ,
                            ChatColor.GREEN + "You have been revived by " + player.getDisplayName(),
                            0,50,20);
                    player.sendTitle(ChatColor.GREEN + "" + ChatColor.BOLD + "" ,
                            ChatColor.GREEN + "You have revived " + injured.getDisplayName(),
                            0,50,20);

                    player.getInventory().removeItem(new ItemStack(Material.GOLD_BLOCK,1));

                    CoolCrownUHC.injured.remove(injured.getUniqueId());
                    progress.removeAll();
                    progressTrackers.remove(player.getUniqueId());

                    injured.setFoodLevel(foodLevel.get(injured.getUniqueId()));
                    injured.setSaturation(saturation.get(injured.getUniqueId()));
                }
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        if (CoolCrownUHC.injured.contains(event.getPlayer().getUniqueId())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerItemPickUp(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        if (CoolCrownUHC.injured.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}