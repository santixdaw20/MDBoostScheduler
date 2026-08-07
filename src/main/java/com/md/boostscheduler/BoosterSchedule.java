package com.md.boostscheduler;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class MDBoostScheduler extends JavaPlugin {

    private final List<BoosterSchedule> schedules = new ArrayList<>();
    private boolean running = false;
    private Logger log;

    @Override
    public void onEnable() {
        this.log = getLogger();
        saveDefaultConfig();
        loadSchedules();

        MDBoostCommand executor = new MDBoostCommand(this);
        getCommand("mdboost").setExecutor(executor);
        getCommand("mdboost").setTabCompleter(executor);

        log.info("MDBoostScheduler cargado. " + schedules.size() + " booster(s) configurado(s).");

        if (getConfig().getBoolean("auto-start", false)) {
            startAll();
            log.info("auto-start esta en true: ciclo de boosters iniciado automaticamente.");
        } else {
            log.info("Usa /mdboost start para iniciar el ciclo de boosters.");
        }
    }

    @Override
    public void onDisable() {
        stopAll();
    }

    /** Vuelve a leer config.yml y reconstruye la lista de schedules (sin arrancar tareas). */
    public void loadSchedules() {
        boolean wasRunning = running;
        stopAll();

        reloadConfig();
        schedules.clear();

        ConfigurationSection boostersSection = getConfig().getConfigurationSection("boosters");
        if (boostersSection == null) {
            log.warning("No se encontro la seccion 'boosters' en config.yml");
            return;
        }

        for (String id : boostersSection.getKeys(false)) {
            ConfigurationSection entry = boostersSection.getConfigurationSection(id);
            if (entry == null) continue;

            String boosterType = entry.getString("booster-type", id);
            int multiplier = entry.getInt("multiplier", 10);
            int durationMinutes = entry.getInt("duration-minutes", 10);
            int intervalMinutes = entry.getInt("interval-minutes", 60);
            boolean enabled = entry.getBoolean("enabled", true);

            if (intervalMinutes <= 0) {
                log.warning("Booster '" + id + "' tiene interval-minutes invalido (" + intervalMinutes + "), se omite.");
                continue;
            }
            if (durationMinutes <= 0) {
                log.warning("Booster '" + id + "' tiene duration-minutes invalido (" + durationMinutes + "), se omite.");
                continue;
            }
            if (intervalMinutes < durationMinutes) {
                log.warning("Booster '" + id + "' tiene interval-minutes (" + intervalMinutes
                        + ") menor a duration-minutes (" + durationMinutes
                        + "). Esto haria que se solapen 2 boosts a la vez, asi que se omite."
                        + " Pone interval-minutes igual o mayor a duration-minutes.");
                continue;
            }

            schedules.add(new BoosterSchedule(id, boosterType, multiplier, durationMinutes, intervalMinutes, enabled));
        }

        if (wasRunning) {
            startAll();
        }
    }

    /** Arranca las tareas repetitivas para todos los boosters habilitados. */
    public void startAll() {
        if (running) return;

        int started = 0;
        for (BoosterSchedule schedule : schedules) {
            if (!schedule.isEnabled()) continue;
            startSchedule(schedule);
            started++;
        }
        running = true;
        log.info("Ciclo de boosters iniciado. " + started + " booster(s) activo(s).");
    }

    private void startSchedule(BoosterSchedule schedule) {
        long periodTicks = schedule.getIntervalMinutes() * 60L * 20L;

        int taskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
            String command = schedule.buildActivateCommand();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);

            if (getConfig().getBoolean("log-on-trigger", true)) {
                log.info("[" + schedule.getId() + "] Ejecutado: /" + command);
            }

            schedule.setNextTriggerEpochMillis(System.currentTimeMillis() + schedule.getIntervalMinutes() * 60_000L);
        }, 0L, periodTicks).getTaskId();

        schedule.setTaskId(taskId);
        schedule.setNextTriggerEpochMillis(System.currentTimeMillis() + schedule.getIntervalMinutes() * 60_000L);
    }

    /** Detiene todas las tareas activas. */
    public void stopAll() {
        for (BoosterSchedule schedule : schedules) {
            if (schedule.getTaskId() != -1) {
                Bukkit.getScheduler().cancelTask(schedule.getTaskId());
                schedule.setTaskId(-1);
                schedule.setNextTriggerEpochMillis(-1);
            }
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public List<BoosterSchedule> getSchedules() {
        return schedules;
    }
}
