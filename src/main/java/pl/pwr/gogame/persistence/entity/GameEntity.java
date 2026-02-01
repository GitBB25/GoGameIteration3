package pl.pwr.gogame.persistence.entity;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import pl.pwr.gogame.model.Board;
import pl.pwr.gogame.model.GamePlayer;
import pl.pwr.gogame.model.StoneColor;
import jakarta.persistence.GeneratedValue;

//Entity w Springu to rzeczy, które zostaną zapisane
//i istnieją po zakończeniu programu, czyli są persistent.
//Są to np. utworzone tabele sqlowe
//To entity przechowuje dane o grze

@Entity
@Table(name = "moves")
public class GameEntity {
    
    @Id
    @GeneratedValue
    private Long id;

    private int boardSize;

    
    @Column(nullable = false)
    private String blackPlayerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoneColor blackPlayerColor;

    @Column(nullable = false)
    private String whitePlayerName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoneColor whitePlayerColor;


    private String winner;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @OrderBy("moveNumber ASC")
    private List<MoveEntity> moves = new ArrayList<>();

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    };

    public void setBlackPlayerName(String blackPlayer) {
        this.blackPlayerName = blackPlayer;
    };
     public void setBlackPlayerColor(StoneColor blackPlayer) {
        this.blackPlayerColor = blackPlayer;
    };
    public void setWhitePlayerName(String whitePlayer) {
        this.whitePlayerName = whitePlayer;
    };
     public void setWhitePlayerColor(StoneColor whitePlayer) {
        this.whitePlayerColor = whitePlayer;
    };

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    };
    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    };
    public void setWinner(String winner) {
        this.winner = winner;
    };

    public int getBoardSize() {
        return this.boardSize;
    };

    public LocalDateTime getStartedAt() {
        return this.startedAt;
    };
    public LocalDateTime getFinishedAt() {
        return this.finishedAt;
    };
    public String getWinner() {
        return this.winner;
    };

    public Long getId() {
        return this.id;
    }
}
