package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_template")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "item_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ItemTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private int weight;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", insertable = false, updatable = false)
    private ItemType itemType;

    private boolean stackable;

    protected ItemTemplate(String name, String description, int weight, ItemType itemType, boolean stackable) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.itemType = itemType;
        this.stackable = stackable;
    }

    protected void updateCommon(String name, String description, int weight, boolean stackable) {
        this.name = name;
        this.description = description;
        this.weight = weight;
        this.stackable = stackable;
    }

    public abstract void update(com.jefflife.mudmk2.gamedata.application.service.model.request.ItemTemplateRequest request);

    /**
     * 인메모리 캐시 적재 시점에 서브클래스별 LAZY 컬렉션을 강제 초기화한다.
     * Hibernate 프록시 호출 시 실제 서브클래스 구현이 polymorphic dispatch 된다.
     */
    public abstract void initializeAssociatedEntities();
}
