package de.yannik.dreamveilCore.family.model;

import java.time.LocalDateTime;

/**
 * Core data for a Found Family.
 * Mirrors the {@code families} table.
 */
public class Family {

    private final String        id;           // UUID string
    private       String        name;
    private       String        ownerUuid;
    private       String        description;
    private final LocalDateTime createdAt;
    private       int           memberCount;  // denormalized; refreshed on load

    public Family(String id, String name, String ownerUuid,
                  String description, LocalDateTime createdAt, int memberCount) {
        this.id          = id;
        this.name        = name;
        this.ownerUuid   = ownerUuid;
        this.description = description;
        this.createdAt   = createdAt;
        this.memberCount = memberCount;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getId()          { return id;          }
    public String        getName()        { return name;        }
    public String        getOwnerUuid()   { return ownerUuid;   }
    public String        getDescription() { return description; }
    public LocalDateTime getCreatedAt()   { return createdAt;   }
    public int           getMemberCount() { return memberCount; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setName(String name)            { this.name        = name;        }
    public void setOwnerUuid(String ownerUuid)  { this.ownerUuid   = ownerUuid;   }
    public void setDescription(String desc)     { this.description = desc;        }
    public void setMemberCount(int count)       { this.memberCount = count;       }

    @Override
    public String toString() {
        return "Family{id='" + id + "', name='" + name + "', owner='" + ownerUuid
                + "', members=" + memberCount + "}";
    }
}