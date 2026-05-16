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

    /**
     * 소비/사용 시점에 ItemInstance 행을 즉시 DB에서 삭제하고 player 상태도 즉시 저장해야 하는지 여부.
     * <p>true 반환 시 인벤토리에서 빠진 시점에 같은 트랜잭션 안에서 player save 까지 마쳐 — 서버 재기동 시
     * 부활(아이템 복사) 위험을 차단한다. 영구 효과를 부여하는 아이템(예: 무공서 학습)이 이 값을 true 로 둔다.</p>
     */
    public abstract boolean requiresImmediateDeletion();
}
