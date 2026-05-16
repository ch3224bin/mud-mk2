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
     *
     * <p>JOINED 상속 전략에서 Hibernate는 base class 레벨의 프록시를 생성하기 때문에
     * {@code instanceof EquippableItemTemplate} 같은 체크가 항상 false를 반환한다.
     * {@link org.hibernate.Hibernate#unproxy} 로 실제 subtype 인스턴스로 교체한 뒤
     * 서브클래스의 초기화 로직을 호출한다.</p>
     */
    public void initializeAssociatedEntities() {
        // Hibernate JOINED inheritance lazy proxy를 actual subtype으로 교체.
        // 이렇게 해야 downstream의 instanceof 체크(EquippableItemTemplate, WeaponTemplate 등)가
        // 정상 동작한다.
        this.template = (ItemTemplate) org.hibernate.Hibernate.unproxy(this.template);
        this.template.initializeAssociatedEntities();
    }
}
