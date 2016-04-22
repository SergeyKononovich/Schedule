package com.grsu.konon.schedule.Helpers;

import java.util.HashMap;
import java.util.Map;

public class SettingsContainer {
    public Map<Integer, String> faculties = new HashMap<>();
    public Map<Integer, String> departments = new HashMap<>();
    public Map<Integer, String> groups = new HashMap<>();
    public int[] course = {1, 2, 3, 4, 5, 6};
    public Map<String, Integer> defaultIds = new HashMap<>();
}