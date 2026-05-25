package de.yannik.dreamveilCore.family.model;

import java.time.LocalDateTime;

/**
 * Core data for a Found Family.
 * Mirrors the {@code families} table.
 */
public class Family {

    public static final int MAX_TAG_LENGTH = 5;

    private final String        id;           // UUID string
    private       String        name;
    private       String        tag;          // short identifier, max 5 chars, unique
    private       String        ownerUuid;
    private       String        description;
    private final LocalDateTime createdAt;
    private       int           memberCount;  // denormalized; refreshed on load

    public Family(String id, String name, String tag, String ownerUuid,
                  String description, LocalDateTime createdAt, int memberCount) {
        this.id          = id;
        this.name        = name;
        this.tag         = tag;
        this.ownerUuid   = ownerUuid;
        this.description = description;
        this.createdAt   = createdAt;
        this.memberCount = memberCount;
    }

    /**
     * Validate a tag string.
     * Rules: not null/blank, 1–5 characters, alphanumeric only.
     *
     * @throws IllegalArgumentException with a human-readable message on violation
     */
    public static void validateTag(String tag) {
        if (tag == null || tag.isBlank())
            throw new IllegalArgumentException("Family tag must not be empty.");
        if (tag.length() > MAX_TAG_LENGTH)
            throw new IllegalArgumentException(
                    "Family tag '" + tag + "' exceeds the maximum of " + MAX_TAG_LENGTH + " characters.");
        if (!tag.matches("[a-zA-Z0-9]+"))
            throw new IllegalArgumentException(
                    "Family tag '" + tag + "' must only contain letters and numbers.");
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String        getId()          { return id;          }
    public String        getName()        { return name;        }
    public String        getTag()         { return tag;         }
    public String        getOwnerUuid()   { return ownerUuid;   }
    public String        getDescription() { return description; }
    public LocalDateTime getCreatedAt()   { return createdAt;   }
    public int           getMemberCount() { return memberCount; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setName(String name)            { this.name        = name;        }
    public void setTag(String tag)              { this.tag         = tag;         }
    public void setOwnerUuid(String ownerUuid)  { this.ownerUuid   = ownerUuid;   }
    public void setDescription(String desc)     { this.description = desc;        }
    public void setMemberCount(int count)       { this.memberCount = count;       }

    @Override
    public String toString() {
        return "Family{id='" + id + "', name='" + name + "', owner='" + ownerUuid
                + "', members=" + memberCount + "}";
    }
}