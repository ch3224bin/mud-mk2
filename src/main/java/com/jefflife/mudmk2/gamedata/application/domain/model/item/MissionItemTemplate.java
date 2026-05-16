package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mission_item_template")
@DiscriminatorValue("MISSION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionItemTemplate extends ItemTemplate {

    @Enumerated(EnumType.STRING)
    private MissionItemType missionItemType;

    private String targetRef;

    @Builder
    public MissionItemTemplate(String name, String description, int weight, boolean stackable,
                               MissionItemType missionItemType, String targetRef) {
        super(name, description, weight, ItemType.MISSION, stackable);
        this.missionItemType = missionItemType;
        this.targetRef = targetRef;
    }

    public void update(ItemTemplateRequest request) {
        updateCommon(request.name(), request.description(), request.weight(), request.stackable());
        this.missionItemType = request.missionItemType();
        this.targetRef = request.targetRef();
    }

    @Override
    public void initializeAssociatedEntities() {
        // 초기화할 LAZY 컬렉션 없음
    }

    @Override
    public boolean requiresImmediateDeletion() {
        return false;
    }
}
