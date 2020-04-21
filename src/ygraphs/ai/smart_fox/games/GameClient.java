package ygraphs.ai.smart_fox.games;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import state.Position;
import state.Team;

import static ygraphs.ai.smart_fox.games.GameMessage.AI_TEAM;

public class GameClient {
    public static GameClient CLIENT_INSTANCE = new GameClient();
    public Amazon amazon = new Amazon();
    private int[] start = null;
    private int[] move = null;
    private int[] arrow = null;

    public void reset(){
        amazon = new Amazon();
    }

    public List<Position> askAIForMove(Team team){
        start = null;
        move = null;
        arrow = null;

        Map<String, Object> msgDetails = new HashMap<>(); // no parameters required
        msgDetails.put(AI_TEAM, team.equals(Team.WHITE));
        amazon.handleGameMessage(GameMessage.GAME_ACTION_START, msgDetails);
        if(start != null && move != null && arrow != null) {
            Position startPos = new Position(start[1], start[0]);
            Position movePos = new Position(move[1], move[0]);
            Position arrowPos = new Position(arrow[1], arrow[0]);

            List<Position> aiResult = new ArrayList<>();
            aiResult.add(startPos);
            aiResult.add(movePos);
            aiResult.add(arrowPos);
            return aiResult;
        }else{
            System.out.println("no return value from AI? " + start + "" + move + "" + arrow);
            System.exit(1);
            return null;
        }
    }

    // The amazon AI will call this, so we just set the variable here and pick it up in the return statement of askAIForMove
    public void sendMoveMessage(int[] start, int[] move, int[] arrow) {
        this.start = start;
        this.move = move;
        this.arrow = arrow;
    }

    public List<Position> receiveMove(Team team, ArrayList<Integer> qcurr, ArrayList<Integer> qnew, ArrayList<Integer> arrowL){
        Map<String, Object> msgDetails = new HashMap<>();
        msgDetails.put(GameMessage.QUEEN_POS_CURR, qcurr);
        msgDetails.put(GameMessage.Queen_POS_NEXT, qnew);
        msgDetails.put(GameMessage.ARROW_POS, arrowL);
        msgDetails.put(AI_TEAM, team.equals(Team.WHITE));

        start = null;
        move = null;
        arrow = null;

        // the ai will handle the new move and generate a response, which will get logged into sendMoveMesage
        amazon.handleGameMessage(GameMessage.GAME_ACTION_MOVE, msgDetails);
        if(start != null && move != null && arrow != null) {
            Position startPos = new Position(start[1], start[0]);
            Position movePos = new Position(move[1], move[0]);
            Position arrowPos = new Position(arrow[1], arrow[0]);

            List<Position> aiResult = new ArrayList<>();
            aiResult.add(startPos);
            aiResult.add(movePos);
            aiResult.add(arrowPos);
            return aiResult;
        }else{
            System.out.println("no return value from AI? " + start + "" + move + "" + arrow);
            System.exit(1);
            return null;
        }
    }
}
