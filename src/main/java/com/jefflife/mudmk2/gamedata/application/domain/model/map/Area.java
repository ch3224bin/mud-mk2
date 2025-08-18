package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Getter @EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Area {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Embedded
	private AreaName name;

	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private AreaType type;

    public static Area create(AreaCreateRequest areaCreateRequest) {
        Area area = new Area();
        area.name = AreaName.of(areaCreateRequest.name());
        area.type = requireNonNull(areaCreateRequest.type());

        return area;
    }

    public void changeName(String name) {
		this.name = AreaName.of(name);
	}

    public String getName() {
        return this.name.getName();
    }
}
