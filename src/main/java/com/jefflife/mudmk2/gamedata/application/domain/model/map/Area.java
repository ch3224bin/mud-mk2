package com.jefflife.mudmk2.gamedata.application.domain.model.map;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.util.Assert;

@Getter @EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Area {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	@Enumerated(EnumType.STRING)
	private AreaType type;

	@Builder
	public Area(final Long id, final String name, final AreaType type) {
		Assert.hasText(name, "Area name cannot be null or empty");
		Assert.notNull(type, "Area type cannot be null");

		this.id = id;
		this.name = name;
		this.type = type;
	}

	public void changeName(String name) {
		Assert.hasText(name, "Area name cannot be null or empty");
		this.name = name;
	}
}
