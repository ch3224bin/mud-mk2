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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("")
                        .hp(100)
                        .maxHp(100)
                        .mp(50)
                        .maxMp(50)
                        .str(10)
                        .dex(10)
                        .con(10)
                        .intelligence(10)
                        .pow(10)
                        .cha(10)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("A local merchant selling various goods.")
                        .hp(80)
                        .maxHp(80)
                        .mp(20)
                        .maxMp(20)
                        .str(8)
                        .dex(12)
                        .con(10)
                        .intelligence(14)
                        .pow(8)
                        .cha(16)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("An important figure with tasks for adventurers.")
                        .hp(120)
                        .maxHp(120)
                        .mp(80)
                        .maxMp(80)
                        .str(12)
                        .dex(10)
                        .con(12)
                        .intelligence(16)
                        .pow(14)
                        .cha(14)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("A skilled trainer who can teach various abilities.")
                        .hp(150)
                        .maxHp(150)
                        .mp(100)
                        .maxMp(100)
                        .str(15)
                        .dex(15)
                        .con(15)
                        .intelligence(18)
                        .pow(15)
                        .cha(12)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("A vigilant guard protecting the area.")
                        .hp(200)
                        .maxHp(200)
                        .mp(50)
                        .maxMp(50)
                        .str(18)
                        .dex(14)
                        .con(18)
                        .intelligence(10)
                        .pow(8)
                        .cha(10)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("A hospitable innkeeper providing lodging and food.")
                        .hp(90)
                        .maxHp(90)
                        .mp(30)
                        .maxMp(30)
                        .str(10)
                        .dex(12)
                        .con(12)
                        .intelligence(12)
                        .pow(8)
                        .cha(18)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("A common resident of the area.")
                        .hp(70)
                        .maxHp(70)
                        .mp(20)
                        .maxMp(20)
                        .str(8)
                        .dex(8)
                        .con(8)
                        .intelligence(8)
                        .pow(8)
                        .cha(8)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
                .maxHp(baseInfo.getMaxHp())
                .mp(baseInfo.getMp())
                .maxMp(baseInfo.getMaxMp())
                .str(baseInfo.getStr())
                .dex(baseInfo.getDex())
                .con(baseInfo.getCon())
                .intelligence(baseInfo.getIntelligence())
                .pow(baseInfo.getPow())
                .cha(baseInfo.getCha())
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
     * @param str The strength stat
     * @param dex The dexterity stat
     * @param con The constitution stat
     * @param intelligence The intelligence stat
     * @param pow The power stat
     * @param cha The charisma stat
     * @return A new NonPlayerCharacter with the specified stats
     */
    public static NonPlayerCharacter createNPCWithStats(
            String name, NPCType npcType, Long roomId,
            int hp, int mp, int str, int dex, int con, int intelligence, int pow, int cha) {

        return builder()
                .baseCharacter(BaseCharacter.builder()
                        .name(name)
                        .background("")
                        .hp(hp)
                        .maxHp(hp)
                        .mp(mp)
                        .maxMp(mp)
                        .str(str)
                        .dex(dex)
                        .con(con)
                        .intelligence(intelligence)
                        .pow(pow)
                        .cha(cha)
                        .roomId(roomId)
                        .alive(true)
                        .build())
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
