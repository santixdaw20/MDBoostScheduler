package com.md.boostscheduler;

/**
 * Representa una entrada configurable de booster programado,
 * leida directamente del config.yml.
 */
public class BoosterSchedule {

    private final String id;
    private final String boosterType;
    private final int multiplier;
    private final int durationMinutes;
    private final int intervalMinutes;
    private final boolean enabled;

    private int taskId = -1;
    private long nextTriggerEpochMillis = -1;

    public BoosterSchedule(String id, String boosterType, int multiplier,
                            int durationMinutes, int intervalMinutes, boolean enabled) {
        this.id = id;
        this.boosterType = boosterType;
        this.multiplier = multiplier;
        this.durationMinutes = durationMinutes;
        this.intervalMinutes = intervalMinutes;
        this.enabled = enabled;
    }

    public String getId() {
        return id;
    }

    public String getBoosterType() {
        return boosterType;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getNextTriggerEpochMillis() {
        return nextTriggerEpochMillis;
    }

    public void setNextTriggerEpochMillis(long nextTriggerEpochMillis) {
        this.nextTriggerEpochMillis = nextTriggerEpochMillis;
    }

    /**
     * Construye el comando de consola que AxBoosters espera:
     * /axboosteradmin activateserver <tipo> <multiplicador> <duracion>m
     */
    public String buildActivateCommand() {
        return "axboosteradmin activateserver " + boosterType + " " + multiplier + " " + durationMinutes + "m";
    }
}
