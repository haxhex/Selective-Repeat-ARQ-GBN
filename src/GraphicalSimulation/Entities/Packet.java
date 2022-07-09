package GraphicalSimulation.Entities;

import GraphicalSimulation.Transfer.FXMLDocumentController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

// Packet Graphics
public class Packet{
    public int s;
    public boolean newpa = true;
    public boolean run = false;
    public Timeline timeline = new Timeline();
    Button button;
    public synchronized void res(boolean f) {
        timeline.pause();
        run=false;
        button.setLayoutY(FXMLDocumentController.sd[s].getLayoutY());
        button.setStyle("-fx-background-color: #990099");
        button.setVisible(false);
        if(!f) {
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setTextFill(Paint.valueOf("#ff0000"));
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setFont(new Font(28));
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartX(80);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartY(FXMLDocumentController.star);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndX((423+80)/2);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutY((2*FXMLDocumentController.star+20)/2-60);
            FXMLDocumentController.star+=20;
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndY(FXMLDocumentController.star);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStrokeWidth(5);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStroke(Paint.valueOf("#ff0000"));
            FXMLDocumentController.star+=30;
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setText(Integer.toString(s%(int)Math.pow(2, (FXMLDocumentController.sqlimit)))/*+"->"*/);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutX((66+422)/2);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setMinSize(100, 100);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setStyle(" -fx-font-family: \"TOXIA\" ;");
            ++FXMLDocumentController.currentL;
              }
    }
    public Packet (int source) {
        timeline.stop();
        timeline.setCycleCount(Timeline.INDEFINITE);
        button =new Button();
        button.setOnAction((event) -> {
        button.setVisible(false);
        try {
            res(false);
            } catch (Exception ex) {
                Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        s=source;
        button.setText(Integer.toString(s%(int)Math.pow(2, (FXMLDocumentController.sqlimit))));
        button.setStyle("-fx-background-color: #990099");
        button.setMinSize(10, 40);
        button.setLayoutX(FXMLDocumentController.sd[s].getLayoutX());
        button.setLayoutY(FXMLDocumentController.sd[s].getLayoutY());
        KeyFrame Frame=new KeyFrame(Duration.millis(100), new EventHandler<javafx.event.ActionEvent>() {
        @Override
        public void handle(javafx.event.ActionEvent event) {
        if(button.getLayoutY()>=FXMLDocumentController.sd[s+FXMLDocumentController.Packetnumber].getLayoutY()) {
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setTextFill(Paint.valueOf("#00ff00"));
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setFont(new Font(28));
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartX(80);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStartY(FXMLDocumentController.star);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndX(423);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutY((2*FXMLDocumentController.star+20)/2-60);
            FXMLDocumentController.star+=20;
            FXMLDocumentController.li[FXMLDocumentController.currentL].setEndY(FXMLDocumentController.star);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStrokeWidth(5);
            FXMLDocumentController.li[FXMLDocumentController.currentL].setStroke(Paint.valueOf("#00ff00"));
            FXMLDocumentController.star+=30;
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setText(Integer.toString(s%(int)Math.pow(2, (FXMLDocumentController.sqlimit)))/*+"->"*/);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setLayoutX((66+422)/2);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setMinSize(100, 100);
            FXMLDocumentController.labels[FXMLDocumentController.currentL].setStyle(" -fx-font-family: \"TOXIA\" ;");
            ++FXMLDocumentController.currentL;
            FXMLDocumentController.sd[s+FXMLDocumentController.Packetnumber].setStyle("-fx-background-color: #33ff33");
            button.setStyle("-fx-background-color: #33ff33");
            try {
                FXMLDocumentController.move_move(s+FXMLDocumentController.Packetnumber);
                FXMLDocumentController.recst[s]=true;
                FXMLDocumentController.Move_Window();
            } catch (Exception ex) {
                Logger.getLogger(Packet.class.getName()).log(Level.SEVERE, null, ex);
            }
                   run=false;
                    timeline.stop();
                }
        else
        button.setLayoutY(button.getLayoutY()+20);
    }
        });
           timeline.getKeyFrames().add(Frame);
    }
public synchronized Button getp()  {
    return button;
}
    public synchronized void run() {
        run=true;
        button.setVisible(true);
        if(newpa==true)
            timeline.playFromStart();
        else
            timeline.play();
            newpa=true;
        }
}
