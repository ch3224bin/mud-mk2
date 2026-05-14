package com.jefflife.mudmk2.gameplay.application.service;

public interface MonsterRespawnService {
    /**
     * 죽었고 리스폰 가능한 모든 몬스터를 일괄 부활시킨다.
     * @return 부활시킨 몬스터 수
     */
    int respawnAll();
}
