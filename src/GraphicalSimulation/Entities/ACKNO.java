package GraphicalSimulation.Entities;

import GraphicalSimulation.Transfer.FXMLDocumentController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

// Acknowledgement Graphics
public class ACKNO {
    public int s;
    public  boolean run = false;
    Button button;
    public Timeline timeline = new Timeline();
    public synchronized void res() {
        if(run)
            timeline.stop();
        run = false;
        button.setLayoutX(FXMLDocumentController.sd[s].getLayoutX());
        button.setLayoutY(FXMLDocumentController.sd[s].getLayoutY());
        button.setStyle("-fx-background-color: #009999");
        button.setVisible(false);
        FXMLDocumentController.ackst[s-FXMLDocumentController.Packetnumber] = false;
        FXMLDocumentController.recst[s-FXMLDocumentController.Packetnumber] = false;
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setTextFill(Paint.valueOf("#FF0000"));
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setFont(new Font(28));
        FXMLDocumentController.li[FXMLDocumentController.currentL].setStartX(423);
        FXMLDocumentController.li[FXMLDocumentController.currentL].setEndX((80+422)/2);
        FXMLDocumentController.li[FXMLDocumentController.currentL].setStartY(FXMLDocumentController.star);
        FXMLDocumentController.li[FXMLDocumentController.currentL].setStrokeWidth(5);
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutY((2*FXMLDocumentController.star+20)/2-35);
        FXMLDocumentController.star+=20;
        FXMLDocumentController.li[FXMLDocumentController.currentL].setEndY(FXMLDocumentController.star);
        FXMLDocumentController.star+=30;
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setText(Integer.toString((s-FXMLDocumentController.Packetnumber)%(int)Math.pow(2, (FXMLDocumentController.sqlimit)))/*+"<-"*/);
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutX((66+422)/2);
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setMinSize(40, 40);
        FXMLDocumentController.labels[FXMLDocumentController.currentL].setStyle(" -fx-font-family: \"TOXIA\" ;");
        FXMLDocumentController.li[FXMLDocumentController.currentL].setStroke(Paint.valueOf("#ff0000"));
        ++FXMLDocumentController.currentL;
    }
    public ACKNO(Button nice,int source) {
        timeline.stop();
        timeline.setCycleCount(Timeline.INDEFINITE);
        button = nice;
        button.setOnAction((event) -> {
        try {
            FXMLDocumentController.pp[s-FXMLDocumentController.Packetnumber].res(true);  res();
        } catch (Exception ex) {
            Logger.getLogger(ACKNO.class.getName()).log(Level.SEVERE, null, ex);
        }
    });
        s = source;
        button.setText(Integer.toString((s-FXMLDocumentController.Packetnumber)%(int)Math.pow(2, FXMLDocumentController.sqlimit)));
        button.setLayoutX(FXMLDocumentController.sd[s].getLayoutX());
        button.setLayoutY(FXMLDocumentController.sd[s].getLayoutY());
        button.setStyle("-fx-background-color: #009999");
        button.setMinSize(10, 40);
        KeyFrame Frame=new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
        run = true;
        if(button.getLayoutY()<=FXMLDocumentController.sd[s-FXMLDocumentController.Packetnumber].getLayoutY()) {
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setTextFill(Paint.valueOf("#00ff00"));
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setFont(new Font(28));
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartX(423);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndX(80);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartY(FXMLDocumentController.star);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStrokeWidth(5);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutY((2*FXMLDocumentController.star+20)/2-35);
            FXMLDocumentController.star+=20;
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndY(FXMLDocumentController.star);
            FXMLDocumentController.star+=30;
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setText(Integer.toString((s-FXMLDocumentController.Packetnumber)%(int)Math.pow(2, (FXMLDocumentController.sqlimit)))/*+"<-"*/);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutX((66+422)/2);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setMinSize(40, 40);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setStyle(" -fx-font-family: \"TOXIA\" ;");
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStroke(Paint.valueOf("#00ff00"));
            ++FXMLDocumentController.currentL;
            button.setStyle("-fx-background-color: #6666ff  ");
            FXMLDocumentController.ackst[s-FXMLDocumentController.Packetnumber]=true;
            try {
                 FXMLDocumentController.Move_Window();
            } catch (Exception ex) {
                Logger.getLogger(ACKNO.class.getName()).log(Level.SEVERE, null, ex);
            }
            run=false;
            timeline.stop();
        }
        else
        button.setLayoutY(button.getLayoutY()-20);
            }
        });
        timeline.getKeyFrames().add(Frame);
    }
   public synchronized Button getp() {
        return button;
    }
    public synchronized void run() {
        run=true;
        timeline.playFromStart();
        }
}
