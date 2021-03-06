package net.problemzone.bashit.scoreboard;

import net.problemzone.bashit.Main;
import net.problemzone.bashit.modules.GameManager;
import net.problemzone.bashit.util.Language;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ScoreboardManager {

    private static final String TIME = "timeCounter";

    //private final Map<Player, Integer> playerDeaths = new HashMap<>();
    private final Map<Player, Integer> playerKills = new HashMap<>();
    private final Map<Player, Integer> playerKillStreak = new HashMap<>();
    private final Map<Player, Integer> playerPoints = new HashMap<>();


    private BukkitTask scoreboardTime;

    public void setGameScoreboard(Player player){
        Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("Infos", "dummy", ChatColor.RED + "" + ChatColor.BOLD +"Problem" + ChatColor.WHITE + "" + ChatColor.BOLD + "Zone");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.getScore("").setScore(15);

        //Countdown
        objective.getScore(ChatColor.RED + "Rundenende:").setScore(14);

        Team timeCounter = scoreboard.registerNewTeam(TIME);
        timeCounter.addEntry(ChatColor.RED + " " + ChatColor.WHITE);
        timeCounter.setPrefix(formatTimeSeconds(GameManager.START_TIME));
        objective.getScore(ChatColor.RED + " " + ChatColor.WHITE).setScore(13);

        objective.getScore("  ").setScore(12);

        //Kills
        objective.getScore(ChatColor.WHITE + "Kills:").setScore(11);

        Team killCounter = scoreboard.registerNewTeam("killCounter");
        killCounter.addEntry(ChatColor.RED + "  " + ChatColor.WHITE);
        killCounter.setPrefix(ChatColor.GOLD + "" + playerKills.get(player));
        objective.getScore(ChatColor.RED + "  " + ChatColor.WHITE).setScore(10);

        objective.getScore("   ").setScore(9);

        //Peanut Leader
        objective.getScore(ChatColor.WHITE + "Leader:").setScore(8);

        Team deathCounter = scoreboard.registerNewTeam("showLeader");
        deathCounter.addEntry(ChatColor.RED + "   " + ChatColor.WHITE);
        deathCounter.setPrefix(ChatColor.GOLD + ""+ playerPoints.get(player));
        objective.getScore(ChatColor.RED + "   " + ChatColor.WHITE).setScore(7);

        objective.getScore("    ").setScore(6);

        //Players Peanuts
        objective.getScore(ChatColor.WHITE + "Peanuts:").setScore(5);

        Team kdCounter = scoreboard.registerNewTeam("peanutCounter");
        kdCounter.addEntry(ChatColor.RED + "    " + ChatColor.WHITE);
        kdCounter.setPrefix(ChatColor.GOLD + "" + playerPoints.get(player));
        objective.getScore(ChatColor.RED + "    " + ChatColor.WHITE).setScore(4);

        objective.getScore("     ").setScore(3);

        //Killstreak
        objective.getScore(ChatColor.WHITE + "Killstreak:").setScore(2);

        Team killStreak = scoreboard.registerNewTeam("killStreak");
        killStreak.addEntry(ChatColor.RED + "     " + ChatColor.WHITE);
        killStreak.setPrefix(ChatColor.GOLD + "0");
        objective.getScore(ChatColor.RED + "     " + ChatColor.WHITE).setScore(1);

        player.setScoreboard(scoreboard);

    }

//TODO: Methoden

    public void startTimeCountdown(int seconds) {
        if (scoreboardTime != null && !scoreboardTime.isCancelled()) scoreboardTime.cancel();

        AtomicInteger remaining = new AtomicInteger(seconds);
        scoreboardTime = new BukkitRunnable() {
            @Override
            public void run() {
                if (remaining.get() <= 0) {
                    if (!this.isCancelled()) this.cancel();
                    return;
                }
                updateTime(remaining.getAndDecrement());
            }
        }.runTaskTimer(Main.getJavaPlugin(), 0, 20);
    }

    private void updateTime(int seconds) {
        Bukkit.getOnlinePlayers().forEach(player -> Objects.requireNonNull(player.getScoreboard().getTeam(TIME)).setPrefix(formatTimeSeconds(seconds)));
    }

    private void updateKill(Player player) {
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("killCounter")).setPrefix(ChatColor.GOLD + "" + playerKills.get(player));
    }

    private void updateKillstreak(Player player) {
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("killStreak")).setPrefix(ChatColor.GOLD + "" + playerKillStreak.get(player));
    }

    private void updatePoints(Player player){
        Scoreboard board = player.getScoreboard();
        Objects.requireNonNull(board.getTeam("peanutCounter")).setPrefix(ChatColor.GOLD + "" + playerPoints.get(player));
    }

    public void updateScoreboard() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            updateKill(player);
            updateKillstreak(player);
            updatePoints(player);
        }
    }

    public void increaseKillCounter(Player player) {
        playerKills.put(player, playerKills.get(player) + 1);

        playerKillStreak.put(player, playerKillStreak.get(player) + 1);
        if (playerKillStreak.get(player) > 0 && playerKillStreak.get(player) % 5 == 0) {
            Bukkit.broadcastMessage(String.format(Language.GLOBAL_KILLSTREAK.getFormattedText(), player.getName(), playerKillStreak.get(player)));
        }
    }

    public void increasePoints(Player player){
        playerPoints.put(player, playerPoints.get(player) + 5);
    }
    public void decreasePoints(Player player){
        playerPoints.put(player, playerPoints.get(player) - 2);
    }

    public void initPlayer(Player player) {
        playerKills.put(player, 0);
        playerPoints.put(player, 0);
        playerKillStreak.put(player, 0);
    }

    private String formatTimeSeconds(int seconds) {
        ChatColor color = seconds < 60 ? ChatColor.RED : ChatColor.GOLD;
        return color + formatTimeNumber((seconds / 60) % 60) + ChatColor.GRAY +  ":" + color + formatTimeNumber(seconds % 60);
    }

    private String formatTimeNumber(int number) {
        return (number < 10 ? "0" : "") + number;
    }

    public Player getLeader(){
         return Collections.max(playerPoints.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

}
