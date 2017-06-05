package fxmlbrowser;

import java.util.Arrays;
import java.util.LinkedList;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

/**
 * FXML Controller class
 *
 * @author Giuseppe Serra 
 */
public class FXMLBrowserController {

    @FXML
    private Button back;
    @FXML
    private Button home;
    @FXML
    private Button forth;
    @FXML
    private TextField urlField;
    @FXML
    private WebView wView;
    
    private WebEngine engine;
    private WebHistory history;
    private String uri;

    /**
     * Initializes the controller class.
     */
    public void initialize() {
        engine=wView.getEngine();
        history=engine.getHistory();
        ObservableList<WebHistory.Entry> hList= history.getEntries();
        engine.load("http://www.google.com");
        
        //initialize urlField;
        urlField.textProperty().addListener(new ChangeListener<String>(){
            @Override
            public void changed(ObservableValue observable, String oldValue, String newValue) {
                
                uri=parseURLField(newValue);
            }    
        });
        
        
        //disable property;
        back.disableProperty().bind(history.currentIndexProperty().isEqualTo(0));
        forth.disableProperty().bind(Bindings.createBooleanBinding(()->
            history.getCurrentIndex()>=hList.size()-1,
            history.currentIndexProperty()));
        
        //apply context menu;
        back.setOnContextMenuRequested(e->{
            int curr=history.getCurrentIndex();
            if(curr<1)return;
            ContextMenu cm=new ContextMenu();
            for(int i=curr-1;i>=0;i--){
                int offset=curr-1;
                MenuItem mi=new MenuItem(hList.get(i).getUrl());
                mi.setOnMenuValidation(v->history.go(offset));
                cm.getItems().add(mi);
            }
            cm.setAutoHide(true);
            cm.show(back.getScene().getWindow(),e.getScreenX(),e.getScreenY());
        });
        
        forth.setOnContextMenuRequested(e->{
            int curr=history.getCurrentIndex();
            if (curr >= history.getEntries().size() - 1) return;
            ContextMenu cm=new ContextMenu();
            for(int i=curr+1;i<=hList.size()-1;i++){
                int offset=i-curr;
                MenuItem mi=new MenuItem(hList.get(i).getUrl());
                mi.setOnMenuValidation(v->history.go(offset));
                cm.getItems().add(mi);
            }
            cm.setAutoHide(true);
            cm.show(forth.getScene().getWindow(),e.getScreenX(), e.getScreenY());
        });
        
        //load a Worker for corect URI;
        engine.getLoadWorker().stateProperty().addListener((o,ov,nv)->{
          if(nv==Worker.State.SUCCEEDED){
              urlField.setText(engine.getLocation());
          }else if(nv==Worker.State.FAILED||nv==Worker.State.CANCELLED){}
        });
    }

    @FXML
    private void handleBack(ActionEvent e){
      history.go(-1);
    }
    
    @FXML
    private void handleForth(ActionEvent e){
        history.go(+1);
    }
    
    @FXML
    private void handlHome(ActionEvent e){
        engine.load("http://www.google.com");
    }
    
    @FXML
    private void goURL(ActionEvent e){
        engine.load(uri);
    }
    
    private String  parseURLField(String uri){
        String regex ="http\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?";
        String regex1="w{2}[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}(/\\S*)?";
        if(uri.matches(regex))return uri;
        if(uri.matches(regex1)){
                       return String.format("http://%s", uri); 
        }
        String [] splits =uri.split("\\s");
        LinkedList<String> strings = new LinkedList<>();
        strings.addAll(Arrays.asList(splits));
        String query= String.join("+", strings);
            return String.format(
        "https://www.google.it/search?q=%s",query);
    }
}
