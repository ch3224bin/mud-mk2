package com.jefflife.mudmk2.gamedata.application.domain.model.item;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "item_instance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private ItemTemplate template;

    private int quantity;

    // EquippedItems → ItemInstance 단방향 매핑(@JoinColumn + @MapKeyEnumerated)을 위한 컬럼.
    // 직접 setter 노출하지 않음 — EquippedItems의 @OneToMany 관계가 채워 넣는다.
    @Column(name = "equipped_items_id", insertable = false, updatable = false)
    private java.util.UUID equippedItemsId;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipped_slot", insertable = false, updatable = false)
    private EquipmentSlot equippedSlot;

    public ItemInstance(ItemTemplate template, int quantity) {
        this.template = template;
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    /**
     * 인메모리 캐시 적재 시점에 LAZY 관계를 강제 초기화한다.
     * detached 상태에서 template 접근 시 LazyInitializationException을 방지.
     */
    public void initializeAssociatedEntities() {
        this.template.initializeAssociatedEntities();
    }
}
