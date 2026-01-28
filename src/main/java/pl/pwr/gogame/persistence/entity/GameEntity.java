package pl.pwr.gogame.persistence.entity;



import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import pl.pwr.gogame.model.Board;
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

    private String blackPlayer;
    private String whitePlayer;

    private String winner;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL)
    @OrderBy("moveNumber ASC")
    private List<MoveEntity> moves = new ArrayList<>();

    public void setBoardSize(int boardSize) {
        this.boardSize = boardSize;
    };
    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    };
    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
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
}
