package pl.pwr.gogame.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pwr.gogame.persistence.entity.GameEntity;
import pl.pwr.gogame.persistence.entity.MoveEntity;

public interface MoveRepository extends JpaRepository<GameEntity, Long> {
    
      List<MoveEntity> findByGameIdOrderByMoveNumber(Long gameId);
}
