package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "food_template")
@DiscriminatorValue("FOOD")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodTemplate extends ItemTemplate {

    private int hpRecovery;
    private int mpRecovery;
    private int apRecovery;

    @Builder
    public FoodTemplate(String name, String description, int weight, boolean stackable,
                        int hpRecovery, int mpRecovery, int apRecovery) {
        super(name, description, weight, ItemType.FOOD, stackable);
        this.hpRecovery = hpRecovery;
        this.mpRecovery = mpRecovery;
        this.apRecovery = apRecovery;
    }

    public void update(ItemTemplateRequest request) {
        updateCommon(request.name(), request.description(), request.weight(), request.stackable());
        this.hpRecovery = request.hpRecovery() != null ? request.hpRecovery() : 0;
        this.mpRecovery = request.mpRecovery() != null ? request.mpRecovery() : 0;
        this.apRecovery = request.apRecovery() != null ? request.apRecovery() : 0;
    }

    @Override
    public void initializeAssociatedEntities() {
        // 초기화할 LAZY 컬렉션 없음
    }
}
