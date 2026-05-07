package com.jefflife.mudmk2.gamedata.application.domain.service;

import com.jefflife.mudmk2.gamedata.application.domain.model.player.BaseCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NPCType;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.NonPlayerCharacter;
import com.jefflife.mudmk2.gamedata.application.domain.model.player.PlayableCharacter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NonPlayerCharacterFactory {

    /**
     * Creates a NonPlayerCharacter using the Builder pattern
     * @return a new Builder instance to configure the NPC
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates an NPC based on the specified type
     * @param name The name of the NPC
     * @param npcType The type of NPC
     * @param uniqueId A unique identifier for the NPC
     * @param roomId The room where the NPC is located
     * @return A new NonPlayerCharacter instance of the specified type
     */
    public static NonPlayerCharacter createNPC(String name, NPCType npcType, String uniqueId, Long roomId) {
        return switch (npcType) {
            case MERCHANT -> createMerchant(name, uniqueId, roomId);
            case QUEST_GIVER -> createQuestGiver(name, uniqueId, roomId);
            case TRAINER -> createTrainer(name, uniqueId, roomId);
            case GUARD -> createGuard(name, uniqueId, roomId);
            case INNKEEPER -> createInnkeeper(name, uniqueId, roomId);
            case COMMON -> createCommonNPC(name, uniqueId, roomId);
            default -> createBasicNPC(name, npcType, uniqueId, roomId);
        };
    }

    private static BaseCharacter buildBaseCharacter(
            String name, String background, int hp, int mp, int ap,
            int vigor, int physique, int agility, int intellect, int will, int meridian,
            Long roomId) {
        return BaseCharacter.builder()
                .name(name)
                .background(background)
                .hp(hp).mp(mp).ap(ap)
                .vigor(vigor).physique(physique).agility(agility)
                .intellect(intellect).will(will).meridian(meridian)
                .innerPower(0).specialTechnique(0).lightStep(0)
                .fistsAndPalms(0).swordMethod(0).bladeMethod(0)
                .longWeapon(0).esotericWeapon(0).archery(0)
                .roomId(roomId)
                .alive(true)
                .build();
    }

    /**
     * Creates a basic NPC with minimal required fields
     * @param name The name of the NPC
     * @param npcType The type of NPC
     * @param uniqueId A unique identifier for the NPC
     * @param roomId The room where the NPC is located
     * @return A new NonPlayerCharacter instance
     */
    public static NonPlayerCharacter createBasicNPC(String name, NPCType npcType, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "", 100, 50, 80, 10, 10, 10, 10, 10, 10, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(1)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(npcType)
                .persona("")
                .spawnRoomId(roomId)
                .essential(false)
                .build();
    }

    /**
     * Creates a merchant NPC with predefined characteristics
     * @param name The name of the merchant
     * @param uniqueId A unique identifier for the merchant
     * @param roomId The room where the merchant is located
     * @return A new merchant NonPlayerCharacter
     */
    public static NonPlayerCharacter createMerchant(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "A local merchant selling various goods.",
                        80, 20, 80, 8, 10, 12, 14, 8, 16, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(5)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.MERCHANT)
                .persona("I am a friendly merchant who sells various goods. I'm always looking for a good deal and enjoy haggling with customers.")
                .spawnRoomId(roomId)
                .essential(true)
                .build();
    }

    /**
     * Creates a quest giver NPC with predefined characteristics
     * @param name The name of the quest giver
     * @param uniqueId A unique identifier for the quest giver
     * @param roomId The room where the quest giver is located
     * @return A new quest giver NonPlayerCharacter
     */
    public static NonPlayerCharacter createQuestGiver(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "An important figure with tasks for adventurers.",
                        120, 80, 80, 12, 12, 10, 16, 14, 14, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(10)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.QUEST_GIVER)
                .persona("I have important tasks that need to be completed. I'm looking for capable adventurers to help me with these quests.")
                .spawnRoomId(roomId)
                .essential(true)
                .build();
    }

    /**
     * Creates a trainer NPC with predefined characteristics
     * @param name The name of the trainer
     * @param uniqueId A unique identifier for the trainer
     * @param roomId The room where the trainer is located
     * @return A new trainer NonPlayerCharacter
     */
    public static NonPlayerCharacter createTrainer(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "A skilled trainer who can teach various abilities.",
                        150, 100, 120, 15, 15, 15, 18, 15, 12, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(15)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.TRAINER)
                .persona("I am a master of my craft and can teach you valuable skills. Training requires dedication and practice.")
                .spawnRoomId(roomId)
                .essential(true)
                .build();
    }

    /**
     * Creates a guard NPC with predefined characteristics
     * @param name The name of the guard
     * @param uniqueId A unique identifier for the guard
     * @param roomId The room where the guard is located
     * @return A new guard NonPlayerCharacter
     */
    public static NonPlayerCharacter createGuard(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "A vigilant guard protecting the area.",
                        200, 50, 112, 18, 18, 14, 10, 8, 10, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(8)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.GUARD)
                .persona("I am sworn to protect this area. I keep a watchful eye for any trouble and maintain order.")
                .spawnRoomId(roomId)
                .essential(true)
                .build();
    }

    /**
     * Creates an innkeeper NPC with predefined characteristics
     * @param name The name of the innkeeper
     * @param uniqueId A unique identifier for the innkeeper
     * @param roomId The room where the innkeeper is located
     * @return A new innkeeper NonPlayerCharacter
     */
    public static NonPlayerCharacter createInnkeeper(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "A hospitable innkeeper providing lodging and food.",
                        90, 30, 96, 10, 12, 12, 12, 8, 18, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(6)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.INNKEEPER)
                .persona("Welcome to my inn! I provide comfortable lodging, good food, and am a source of local gossip and information.")
                .spawnRoomId(roomId)
                .essential(true)
                .build();
    }

    /**
     * Creates a common NPC with predefined characteristics
     * @param name The name of the common NPC
     * @param uniqueId A unique identifier for the common NPC
     * @param roomId The room where the common NPC is located
     * @return A new common NonPlayerCharacter
     */
    public static NonPlayerCharacter createCommonNPC(String name, String uniqueId, Long roomId) {
        return builder()
                .baseCharacter(buildBaseCharacter(name, "A common resident of the area.",
                        70, 20, 64, 8, 8, 8, 8, 8, 8, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(3)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(NPCType.COMMON)
                .persona("I'm just a regular person living my life. I can tell you about the local area and recent happenings.")
                .spawnRoomId(roomId)
                .essential(false)
                .build();
    }

    /**
     * Creates an NPC with a custom persona
     * @param name The name of the NPC
     * @param npcType The type of NPC
     * @param uniqueId A unique identifier for the NPC
     * @param roomId The room where the NPC is located
     * @param persona The custom persona for the NPC
     * @return A new NonPlayerCharacter with the specified persona
     */
    public static NonPlayerCharacter createNPCWithPersona(String name, NPCType npcType, String uniqueId, Long roomId, String persona) {
        NonPlayerCharacter npc = createNPC(name, npcType, uniqueId, roomId);
        return builder()
                .id(npc.getId())
                .baseCharacter(npc.getBaseCharacterInfo())
                .playableCharacter(npc.getPlayableCharacterInfo())
                .npcType(npc.getNpcType())
                .persona(persona)
                .dialogueStates(npc.getDialogueStates())
                .spawnRoomId(npc.getSpawnRoomId())
                .essential(npc.isEssential())
                .build();
    }

    /**
     * Creates an NPC with a custom background
     * @param name The name of the NPC
     * @param npcType The type of NPC
     * @param uniqueId A unique identifier for the NPC
     * @param roomId The room where the NPC is located
     * @param background The custom background for the NPC
     * @return A new NonPlayerCharacter with the specified background
     */
    public static NonPlayerCharacter createNPCWithBackground(String name, NPCType npcType, String uniqueId, Long roomId, String background) {
        NonPlayerCharacter npc = createNPC(name, npcType, uniqueId, roomId);
        BaseCharacter baseInfo = npc.getBaseCharacterInfo();

        BaseCharacter newBaseInfo = BaseCharacter.builder()
                .name(baseInfo.getName())
                .background(background)
                .hp(baseInfo.getHp())
                .mp(baseInfo.getMp())
                .ap(baseInfo.getAp())
                .vigor(baseInfo.getVigor())
                .physique(baseInfo.getPhysique())
                .agility(baseInfo.getAgility())
                .intellect(baseInfo.getIntellect())
                .will(baseInfo.getWill())
                .meridian(baseInfo.getMeridian())
                .innerPower(baseInfo.getInnerPower())
                .specialTechnique(baseInfo.getSpecialTechnique())
                .lightStep(baseInfo.getLightStep())
                .fistsAndPalms(baseInfo.getFistsAndPalms())
                .swordMethod(baseInfo.getSwordMethod())
                .bladeMethod(baseInfo.getBladeMethod())
                .longWeapon(baseInfo.getLongWeapon())
                .esotericWeapon(baseInfo.getEsotericWeapon())
                .archery(baseInfo.getArchery())
                .roomId(baseInfo.getRoomId())
                .alive(baseInfo.isAlive())
                .build();

        return builder()
                .id(npc.getId())
                .baseCharacter(newBaseInfo)
                .playableCharacter(npc.getPlayableCharacterInfo())
                .npcType(npc.getNpcType())
                .persona(npc.getPersona())
                .dialogueStates(npc.getDialogueStates())
                .spawnRoomId(npc.getSpawnRoomId())
                .essential(npc.isEssential())
                .build();
    }

    /**
     * Creates an NPC with custom stats
     * @param name The name of the NPC
     * @param npcType The type of NPC
     * @param roomId The room where the NPC is located
     * @param hp The HP stat
     * @param mp The MP stat
     * @param vigor The vigor stat
     * @param physique The physique stat
     * @param agility The agility stat
     * @param intellect The intellect stat
     * @param will The will stat
     * @param meridian The meridian stat
     * @return A new NonPlayerCharacter with the specified stats
     */
    public static NonPlayerCharacter createNPCWithStats(
            String name, NPCType npcType, Long roomId,
            int hp, int mp, int vigor, int physique, int agility, int intellect, int will, int meridian) {
        int ap = agility * 8;
        return builder()
                .baseCharacter(buildBaseCharacter(name, "", hp, mp, ap, vigor, physique, agility, intellect, will, meridian, roomId))
                .playableCharacter(PlayableCharacter.builder()
                        .level(1)
                        .experience(0)
                        .nextLevelExp(100)
                        .conversable(true)
                        .build())
                .npcType(npcType)
                .persona("")
                .spawnRoomId(roomId)
                .essential(false)
                .build();
    }

    /**
     * Builder class for creating NonPlayerCharacter instances
     */
    public static class Builder {
        private UUID id;
        private BaseCharacter baseCharacterInfo;
        private PlayableCharacter playableCharacterInfo;
        private String persona = "";
        private NPCType npcType;
        private Map<String, String> dialogueStates = new HashMap<>();
        private Long spawnRoomId;
        private boolean essential = false;

        private Builder() {
            // Private constructor to enforce the use of the static factory method
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder baseCharacter(BaseCharacter baseCharacterInfo) {
            this.baseCharacterInfo = baseCharacterInfo;
            return this;
        }

        public Builder playableCharacter(PlayableCharacter playableCharacterInfo) {
            this.playableCharacterInfo = playableCharacterInfo;
            return this;
        }

        public Builder persona(String persona) {
            this.persona = persona;
            return this;
        }

        public Builder npcType(NPCType npcType) {
            this.npcType = npcType;
            return this;
        }

        public Builder dialogueStates(Map<String, String> dialogueStates) {
            this.dialogueStates = dialogueStates;
            return this;
        }

        public Builder addDialogueState(String key, String value) {
            this.dialogueStates.put(key, value);
            return this;
        }

        public Builder spawnRoomId(Long spawnRoomId) {
            this.spawnRoomId = spawnRoomId;
            return this;
        }

        public Builder essential(boolean essential) {
            this.essential = essential;
            return this;
        }

        public NonPlayerCharacter build() {
            // Validate required fields
            if (baseCharacterInfo == null) {
                throw new IllegalStateException("BaseCharacter information is required");
            }
            if (playableCharacterInfo == null) {
                throw new IllegalStateException("PlayableCharacter information is required");
            }
            if (npcType == null) {
                throw new IllegalStateException("NPCType is required");
            }
            if (spawnRoomId == null) {
                throw new IllegalStateException("SpawnRoomId is required");
            }

            return new NonPlayerCharacter(
                    id,
                    baseCharacterInfo,
                    playableCharacterInfo,
                    persona,
                    npcType,
                    dialogueStates,
                    spawnRoomId,
                    essential
            );
        }
    }
}
