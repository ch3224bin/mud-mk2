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
@Table(name = "martial_arts_book_template")
@DiscriminatorValue("MARTIAL_ARTS_BOOK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MartialArtsBookTemplate extends ItemTemplate {

    private String skillRef;

    @Builder
    public MartialArtsBookTemplate(String name, String description, int weight, boolean stackable,
                                   String skillRef) {
        super(name, description, weight, ItemType.MARTIAL_ARTS_BOOK, stackable);
        this.skillRef = skillRef;
    }

    public void update(ItemTemplateRequest request) {
        updateCommon(request.name(), request.description(), request.weight(), request.stackable());
        this.skillRef = request.skillRef();
    }
}
