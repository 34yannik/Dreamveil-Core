package de.yannik.dreamveilCore.family.model;

import java.time.LocalDateTime;

/**
 * Home-point data for a Found Family.
 * Mirrors the {@code family_home} table.
 */
public class FamilyHome {

    private final String        familyId;
    private       String        world;
    private       double        x;
    private       double        y;
    private       double        z;
    private       float         yaw;
    private       float         pitch;
    private final String        setByUuid;
    private final LocalDateTime setAt;

    public FamilyHome(String familyId, String world,
                      double x, double y, double z,
                      float yaw, float pitch,
                      String setByUuid, LocalDateTime setAt) {
        this.familyId  = familyId;
        this.world     = world;
        this.x         = x;
        this.y         = y;
        this.z         = z;
        this.yaw       = yaw;
        this.pitch     = pitch;
        this.setByUuid = setByUuid;
        this.setAt     = setAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getFamilyId()  { return familyId;  }
    public String        getWorld()     { return world;     }
    public double        getX()         { return x;         }
    public double        getY()         { return y;         }
    public double        getZ()         { return z;         }
    public float         getYaw()       { return yaw;       }
    public float         getPitch()     { return pitch;     }
    public String        getSetByUuid() { return setByUuid; }
    public LocalDateTime getSetAt()     { return setAt;     }
}