package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.ITitleService;
import de.yannik.dreamveilCore.player.model.Title;
import de.yannik.dreamveilCore.player.service.TitleService;

import java.util.List;
import java.util.function.Consumer;

public class TitleServiceAdapter implements ITitleService {

    @Override
    public void loadUnlockedAsync(String uuid, Consumer<List<Title>> callback) {
        TitleService.loadUnlockedAsync(uuid, callback);
    }

    @Override
    public void hasTitleAsync(String uuid, Title title, Consumer<Boolean> callback) {
        TitleService.hasTitleAsync(uuid, title, callback);
    }

    @Override
    public void unlockAsync(String uuid, Title title, Runnable callback) {
        TitleService.unlockAsync(uuid, title, callback);
    }

    @Override
    public void revokeAsync(String uuid, Title title, Runnable callback) {
        TitleService.revokeAsync(uuid, title, callback);
    }

    @Override
    public void revokeAllAsync(String uuid, Runnable callback) {
        TitleService.revokeAllAsync(uuid, callback);
    }

    @Override
    public Title[] getAllTitles() {
        return TitleService.getAllTitles();
    }
}