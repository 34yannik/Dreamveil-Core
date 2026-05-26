package de.yannik.dreamveilCore.api.service.impl;

import de.yannik.dreamveilCore.api.service.IFamilyService;
import de.yannik.dreamveilCore.family.model.*;
import de.yannik.dreamveilCore.family.service.FamilyService;

import java.util.List;
import java.util.function.Consumer;

public class FamilyServiceAdapter implements IFamilyService {

    @Override
    public void loadFamilyByNameAsync(String name, Consumer<Family> callback) {
        FamilyService.loadFamilyByNameAsync(name, callback);
    }

    @Override
    public void getAllFamiliesAsync(Consumer<List<Family>> callback) {
        FamilyService.getAllFamiliesAsync(callback);
    }

    @Override
    public void loadFamilyByPlayerAsync(String playerUuid, Consumer<Family> callback) {
        FamilyService.loadFamilyByPlayerAsync(playerUuid, callback);
    }

    @Override
    public void loadFamilyByIdAsync(String familyId, Consumer<Family> callback) {
        FamilyService.loadFamilyByIdAsync(familyId, callback);
    }

    @Override
    public Family getFamily(String familyId) {
        return FamilyService.getFamily(familyId);
    }

    @Override
    public String getFamilyIdForPlayer(String playerUuid) {
        return FamilyService.getFamilyIdForPlayer(playerUuid);
    }

    @Override
    public void createFamilyAsync(String ownerUuid, String ownerName,
                                  String name, String tag, String description,
                                  Consumer<String> callback) {
        FamilyService.createFamilyAsync(ownerUuid, ownerName, name, tag, description, callback);
    }

    @Override
    public void deleteFamilyAsync(String familyId, Runnable callback) {
        FamilyService.deleteFamilyAsync(familyId, callback);
    }

    @Override
    public void loadMembersAsync(String familyId, Consumer<List<FamilyMember>> callback) {
        FamilyService.loadMembersAsync(familyId, callback);
    }

    @Override
    public void addMemberAsync(String familyId, String playerUuid,
                               String playerName, Consumer<Boolean> callback) {
        FamilyService.addMemberAsync(familyId, playerUuid, playerName, callback);
    }

    @Override
    public void removeMemberAsync(String familyId, String playerUuid, Runnable callback) {
        FamilyService.removeMemberAsync(familyId, playerUuid, callback);
    }

    @Override
    public void updateMemberRoleAsync(String familyId, String playerUuid,
                                      FamilyRole role, Runnable callback) {
        FamilyService.updateMemberRoleAsync(familyId, playerUuid, role, callback);
    }

    @Override
    public void loadHomeAsync(String familyId, Consumer<FamilyHome> callback) {
        FamilyService.loadHomeAsync(familyId, callback);
    }

    @Override
    public void setHomeAsync(String familyId, String world,
                             double x, double y, double z,
                             float yaw, float pitch,
                             String setByUuid, Runnable callback) {
        FamilyService.setHomeAsync(familyId, world, x, y, z, yaw, pitch, setByUuid, callback);
    }

    @Override
    public void loadActiveBuffsAsync(String familyId, Consumer<List<FamilyActiveBuff>> callback) {
        FamilyService.loadActiveBuffsAsync(familyId, callback);
    }

    @Override
    public void activateBuffAsync(String familyId, FamilyBuff buffType,
                                  int level, String activatedByUuid,
                                  Runnable callback) {
        FamilyService.activateBuffAsync(familyId, buffType, level, activatedByUuid, callback);
    }

    @Override
    public FamilyBuff[] getAllBuffTypes() {
        return FamilyBuff.values();
    }

    @Override
    public FamilyRole[] getAllRoles() {
        return FamilyRole.values();
    }
}