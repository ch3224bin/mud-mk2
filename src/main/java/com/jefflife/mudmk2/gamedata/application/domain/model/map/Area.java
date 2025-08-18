package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import static java.util.Objects.requireNonNull;

@Getter @EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Area {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    @Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
    @Column(nullable = false)
	private AreaType type;

    public static Area create(CreateAreaRequest createAreaRequest) {
        Area area = new Area();
        area.name = requireNonNull(createAreaRequest.name());
        area.type = requireNonNull(createAreaRequest.type());

        return area;
    }

    public void changeName(String name) {
		Assert.hasText(name, "Area name cannot be null or empty");
		this.name = name;
	}
}
