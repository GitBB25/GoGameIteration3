package pl.pwr.gogame.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.pwr.gogame.model.StoneColor;

//To entity przechowuje dane o ruchach zapisując do tabeli SQL
@Entity
@Table(name = "moves")

public class MoveEntity {
   
    @Id
    @GeneratedValue
    private Long id;

    private int moveNumber;

    @Enumerated(EnumType.STRING)
    private StoneColor playerColor;

    private int col;
    private int row;

    @Enumerated(EnumType.STRING)
    private MoveType type;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private GameEntity game;

    
    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    };
    public void setPlayerColor( StoneColor playerColor) {
        this.playerColor = playerColor;
    };
    public void setCol(int col) {
        this.col = col;
    };
    public void setRow(int row) {
        this.row = row;
    };
    public void setType(MoveType type) {
        this.type = type;
    };
    public void setGame(GameEntity game) {
        this.game = game;
    };


}
