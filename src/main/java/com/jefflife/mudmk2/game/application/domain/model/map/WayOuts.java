package com.jefflife.mudmk2.game.application.domain.model.map;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Embeddable
public class WayOuts {
  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WayOut> wayOuts = new ArrayList<>();

  public WayOuts() {
    wayOuts = new ArrayList<>();
  }

  public WayOuts(List<WayOut> wayOuts) {
    this.wayOuts = wayOuts;
  }

  public List<WayOut> getSortedWayOuts() {
    if (wayOuts.isEmpty()) {
      return List.of();
    }
    return wayOuts.stream()
      .sorted()
      .collect(Collectors.toList());
  }

  public String getExitString() {
    String exitString = wayOuts.stream()
      .filter(WayOut::isShow)
      .map(WayOut::toString)
      .collect(Collectors.joining(" "));
    return Optional.ofNullable(exitString.strip())
            .orElse("없음");
  }

  public Optional<WayOut> getWayOutByDirection(Direction direction) {
    return wayOuts.stream()
      .filter(wayOut -> wayOut.getDirection() == direction)
      .findFirst();
  }

  public void add(WayOut wayOut) {
    if (wayOut == null) {
      throw new NullPointerException("argument is null");
    }

    wayOuts.add(wayOut);
  }
}
