package GraphicalSimulation.Transfer;

import GraphicalSimulation.Entities.ACKNO;
import GraphicalSimulation.Entities.Packet;
import GraphicalSimulation.Entities.SDbt;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import javax.swing.*;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

// FXML Document controller
public class FXMLDocumentController implements Initializable  {
    static int swindow = 0, windowsz = 4, rwindow = 0;
    public static Packet pp[];
    int rtime=75;
    public static int sqlimit=2;
    public static int currentL=0;
    public static int star=50;
    static ACKNO acknos[];
    public static Label labels[];
    public static Line li[];
    public static int Packetnumber=16;
    static Rectangle RWindow, SWindow;
    public static SDbt sd[]=new SDbt[32];
    public static Button ackbt[];
    public static boolean recst[], ackst[];
    Timeline timeline = new Timeline();
    @FXML
    public AnchorPane AP1;
    @FXML
    private Button strt;
    @FXML
    private Pane canup;
    @FXML
    private TextField timetxt;
    @FXML
    private ScrollPane sp;
    @FXML
    private Pane drpane;
    @FXML
    private Button senderbt;
    @FXML
    private Button recieverbt;
    @FXML
    private Button resume;
    @FXML
    private Button stop;
    private synchronized void setsdbt() {
        li =new Line[100];
         for (int i = 0; i < 100; i++) {
             li[i]=new Line(0, 0, 0, 0);
             drpane.getChildren().add(li[i]);
         }
         labels =new Label[100];
         for (int i = 0; i < 100; i++) {
             labels[i]=new Label();
             drpane.getChildren().add(labels[i]);
         }
        for (int i = 0; i < 32; i++) {
            sd[i]=new SDbt();
            canup.getChildren().add(sd[i]);
            sd[i].setMinSize(10, 40);
        }
        for (int i = 0; i < 16; i++) {
             sd[i].setText(Integer.toString(i%(int)Math.pow(2, (FXMLDocumentController.sqlimit))));
        }
        for (int i = 16; i < 32; i++) {
             sd[i].setText(Integer.toString((i-16)%(int)Math.pow(2, (FXMLDocumentController.sqlimit))));
        }
        int fx = 6, fy = 10;
        for(int i=0;i<16;++i) {
            sd[i].setLayoutX(fx);
            sd[i].setLayoutY(fy);
            fx+=30;
        }
        fx=6;fy=550;
        for(int i=16;i<32;++i) {
            ackbt[i-16]=new Button();
            ackbt[i-16].setVisible(false);
            ackbt[i-16].setMinSize(10, 40);
            ackbt[i-16].setLayoutX(fx);
            ackbt[i-16].setLayoutY(fy);
            canup.getChildren().add(ackbt[i-16]);
            sd[i].setLayoutX(fx);
            sd[i].setLayoutY(fy);
            fx += 30;
        }
    }
    @Override
    public synchronized void initialize(URL url, ResourceBundle rb)  {
        try {
            init();
        } catch (Exception ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    synchronized void  init() throws Exception {
        windowsz=DataEntryController.windowsz;
        sqlimit=DataEntryController.sqlimit;
        strt.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        stop.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        resume.setStyle("-fx-background-color: transparent; -fx-background-radius: 10;");
        timeline.stop();
        timeline.setCycleCount(Timeline.INDEFINITE);
        senderbt.setStyle(" -fx-background-color: #000000");
        recieverbt.setStyle(" -fx-background-color: #000000");
        drpane.setMinHeight(5000);senderbt.setMinHeight(5000);recieverbt.setMinHeight(5000);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setContent(drpane);
        ackbt =new Button[Packetnumber];
        setsdbt();
        pp = new Packet[Packetnumber];
        acknos = new ACKNO[Packetnumber];
        for (int i = 0; i < Packetnumber; i++) {
            pp[i]=new Packet(i);
            pp[i].getp().setVisible(false);
            canup.getChildren().add(pp[i].getp());
            acknos[i]=new ACKNO(ackbt[i], i+Packetnumber);
        }
        ackst=new boolean[Packetnumber];
        recst=new boolean[Packetnumber];
        RWindow =new Rectangle();
        RWindow.setFill(null);
        RWindow.setStroke(Paint.valueOf("#ff0000"));
        SWindow=new Rectangle();
        SWindow.setFill(null);
        SWindow.setStroke(Paint.valueOf("#ff0000"));
        SWindow.setStrokeWidth(5);
        RWindow.setStrokeWidth(5);
        Update_Window(RWindow, 0);
        Update_Window(SWindow, Packetnumber);
        canup.getChildren().addAll(RWindow,SWindow);
        KeyFrame Frame=new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            timetxt.setText(Integer.toString(rtime));
            if(rtime==0){
               for (int i = swindow; i < (swindow+windowsz); i++) {
                    if(!ackst[i]) {
                       try {                
                           pp[i].run();                
                       } catch (Exception ex) {
                           Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                       }
            }
        }
        rtime=75;
            } else {
               rtime-=1;
           }
            }
        });
   timeline.getKeyFrames().add(Frame);
      
    }
   public synchronized static void move_move(int i) {
        acknos[i-16].getp().setVisible(true);
        acknos[i-16].run();
    }
   public synchronized static void Move_Window() throws Exception {
       while(swindow < 16 && ackst[swindow])
           ++swindow;
       while(rwindow < 16 && recst[rwindow])
           ++rwindow;
       if(swindow + windowsz >= 16)
           windowsz = 16 - swindow;
        if(rwindow + windowsz >= 16)
           windowsz = 16 - rwindow;
        if(swindow > 15) {
              JOptionPane.showMessageDialog(null,"Simulation Done","Success",1);
        }else
        {
       Update_Window(SWindow, swindow);
       if(rwindow < 16)
       Update_Window(RWindow, rwindow+16);
        }
   }
static synchronized void Update_Window(Rectangle R, int strt) {
    R.setLayoutX(sd[strt].getLayoutX());
    R.setLayoutY(sd[strt].getLayoutY()-5);
    R.setHeight(50);
    R.setWidth(windowsz * 30);
}
    @FXML
    private void strt(ActionEvent event) {
        strt.setVisible(false);stop.setVisible(true);
        timeline.playFromStart();
    }
    @FXML
    private synchronized void stop(ActionEvent event) throws Exception  {
        timeline.stop();stop.setVisible(false);
        for (int i = 0; i < Packetnumber; i++) {
            if(pp[i].run)
            pp[i].timeline.pause();
            if(acknos[i].run)
            acknos[i].timeline.pause();
        }
        resume.setVisible(true);
    }
    @FXML
    private synchronized void resume(ActionEvent event) {
         timeline.play();
         resume.setVisible(false);
         stop.setVisible(true);
         for (int i = 0; i < Packetnumber; i++) {
            if(acknos[i].run)
                acknos[i].timeline.play();
             if(pp[i].run)
                pp[i].timeline.play();
        }
    }
}
