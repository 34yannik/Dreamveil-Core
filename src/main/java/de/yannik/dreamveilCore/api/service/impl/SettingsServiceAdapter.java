package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.ISettingsService;
import de.yannik.dreamveilCore.player.model.PlayerSettings;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.player.service.SettingsService;

import java.util.function.Consumer;

public class SettingsServiceAdapter implements ISettingsService {

    @Override
    public void loadSettingsAsync(String uuid, Consumer<PlayerSettings> callback) {
        SettingsService.loadSettingsAsync(uuid, callback);
    }

    @Override
    public void setPronounsAsync(String uuid, String pronouns, Runnable callback) {
        SettingsService.setPronounsAsync(uuid, pronouns, callback);
    }

    @Override
    public void setShowPronounsAsync(String uuid, boolean showPronouns, Runnable callback) {
        SettingsService.setShowPronounsAsync(uuid, showPronouns, callback);
    }

    @Override
    public void setSelectedTitleAsync(String uuid, Title title, Runnable callback) {
        SettingsService.setSelectedTitleAsync(uuid, title, callback);
    }

    @Override
    public void saveAsync(PlayerSettings settings, Runnable callback) {
        SettingsService.saveAsync(settings, callback);
    }
}