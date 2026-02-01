package pl.pwr.gogame.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import pl.pwr.gogame.persistence.entity.GameEntity;

public interface GameRepository extends JpaRepository<GameEntity, Long> {
   
}