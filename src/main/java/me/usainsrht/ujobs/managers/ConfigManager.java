package me.usainsrht.ujobs.managers;

import lombok.Getter;
import me.usainsrht.ujobs.UJobsPlugin;
import me.usainsrht.ujobs.yaml.YamlMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Getter
public class ConfigManager {

    private UJobsPlugin plugin;
    private YamlConfiguration jobsConfig;
    private YamlConfiguration leaderboardConfig;
    private Map<String, YamlMessage> messages;
    private volatile List<Pattern> blacklistPatterns = List.of();
    public static final YamlMessage EMPTY_YAML_MESSAGE = new YamlMessage(null);

    public ConfigManager(UJobsPlugin plugin) {
        this.plugin = plugin;

    }

    public void reload() {
        plugin.reloadConfig();
        //reload jobs yml too

        loadConfigs();
    }

    public void loadConfigs() {
        // Load config.yml
        plugin.saveDefaultConfig();

        loadBlacklistPatterns();
        loadMessages();

        // Load jobs.yml
        File jobsFile = new File(plugin.getDataFolder(), "jobs.yml");
        if (!jobsFile.exists()) {
            plugin.saveResource("jobs.yml", false);
        }
        jobsConfig = YamlConfiguration.loadConfiguration(jobsFile);

        // Load leaderboard.yml
        File leaderBoardFile = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!leaderBoardFile.exists()) {
            try {
                leaderBoardFile.createNewFile();
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create leaderboard.yml: " + e.getMessage());
            }
        }
        leaderboardConfig = YamlConfiguration.loadConfiguration(leaderBoardFile);
    }

    private void loadBlacklistPatterns() {
        List<Pattern> compiledPatterns = new ArrayList<>();
        for (String regex : plugin.getConfig().getStringList("blacklist.regex")) {
            try {
                compiledPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE));
            } catch (PatternSyntaxException e) {
                plugin.getLogger().warning("Invalid blacklist regex '" + regex + "': " + e.getMessage());
            }
        }
        blacklistPatterns = List.copyOf(compiledPatterns);
    }

    public boolean isBlacklisted(String playerName) {
        if (playerName == null) return false;
        return blacklistPatterns.stream().anyMatch(pattern -> pattern.matcher(playerName).matches());
    }

    public void loadMessages() {
        this.messages = new HashMap<>();
        ConfigurationSection messagesSection = plugin.getConfig().getConfigurationSection("messages");
        if (messagesSection == null) return;
        for (String key : messagesSection.getKeys(false)) {
            YamlMessage yamlMessage = new YamlMessage(messagesSection.get(key));
            messages.put(key, yamlMessage);
        }
    }

    public YamlMessage getMessage(String key) {
        return messages.getOrDefault(key, EMPTY_YAML_MESSAGE);
    }

    public void saveLeaderboard() {
        try {
            leaderboardConfig.save(new File(plugin.getDataFolder(), "leaderboard.yml"));
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save leaderboard.yml: " + e.getMessage());
        }
    }

}
