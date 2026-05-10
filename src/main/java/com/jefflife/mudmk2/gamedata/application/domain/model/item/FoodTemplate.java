package com.jefflife.mudmk2.gamedata.application.domain.model.item;

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
}
