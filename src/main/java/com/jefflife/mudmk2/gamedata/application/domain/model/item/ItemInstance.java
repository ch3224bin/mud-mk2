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
