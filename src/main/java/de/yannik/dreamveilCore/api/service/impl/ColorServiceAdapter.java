package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IColorService;
import de.yannik.dreamveilCore.player.model.PlayerColor;
import de.yannik.dreamveilCore.player.service.ColorService;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Adapter that implements {@link IColorService} using the internal {@link ColorService}.
 * Keeps the public API decoupled from implementation details.
 */
public class ColorServiceAdapter implements IColorService {

    @Override
    public void loadUnlockedColorsAsync(String uuid, Consumer<Set<PlayerColor>> callback) {
        ColorService.loadUnlockedColorsAsync(uuid, callback);
    }

    @Override
    public boolean isColorUnlocked(String uuid, PlayerColor color) {
        return ColorService.isColorUnlocked(uuid, color);
    }

    @Override
    public void isColorUnlockedAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        ColorService.isColorUnlockedAsync(uuid, color, callback);
    }

    @Override
    public void unlockColorAsync(String uuid, PlayerColor color, Runnable callback) {
        ColorService.unlockColorAsync(uuid, color, callback);
    }

    @Override
    public Set<PlayerColor> getUnlockedColors(String uuid) {
        return ColorService.getUnlockedColors(uuid);
    }

    @Override
    public void setChatMsgColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        ColorService.setChatMsgColorAsync(uuid, color, callback);
    }

    @Override
    public void setChatNameColorAsync(String uuid, PlayerColor color, Consumer<Boolean> callback) {
        ColorService.setChatNameColorAsync(uuid, color, callback);
    }

    @Override
    public PlayerColor getChatMsgColor(String uuid) {
        return ColorService.getChatMsgColor(uuid);
    }

    @Override
    public PlayerColor getChatNameColor(String uuid) {
        return ColorService.getChatNameColor(uuid);
    }

    @Override
    public PlayerColor[] getAllColors() {
        return PlayerColor.values();
    }
}