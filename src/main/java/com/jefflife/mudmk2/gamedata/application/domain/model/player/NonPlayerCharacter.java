package com.jefflife.mudmk2.gamedata.application.domain.model.player;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NonPlayerCharacter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private BaseCharacter baseCharacterInfo;

    @Embedded
    private PlayableCharacter playableCharacterInfo;
    
    // NPC 고유 속성
    @Column(length = 2000)
    private String persona;  // LLM 대화용 페르소나
    
    @Enumerated(EnumType.STRING)
    private NPCType npcType; // MERCHANT, QUEST_GIVER, TRAINER 등
    
    // 대화 트리거 및 상태
    @ElementCollection
    @CollectionTable(name = "npc_dialogue_states")
    private Map<String, String> dialogueStates = new HashMap<>();
    
    // 스폰 위치 (죽었을 때 리스폰)
    private Long spawnRoomId;
    
    // 중요도 (중요한 NPC는 죽지 않거나 리스폰)
    private boolean essential = false;

    public NonPlayerCharacter(
            final Long id,
            final BaseCharacter baseCharacterInfo,
            final PlayableCharacter playableCharacterInfo,
            final String persona,
            final NPCType npcType,
            final Map<String, String> dialogueStates,
            final Long spawnRoomId,
            final boolean essential
    ) {
        this.id = id;
        this.baseCharacterInfo = baseCharacterInfo;
        this.playableCharacterInfo = playableCharacterInfo;
        this.persona = persona;
        this.npcType = npcType;
        this.dialogueStates = dialogueStates;
        this.spawnRoomId = spawnRoomId;
        this.essential = essential;
    }
}