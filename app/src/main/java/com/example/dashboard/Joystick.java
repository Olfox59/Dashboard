package com.example.dashboard;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fle on 19/12/2016.
 */

public class Joystick {

    //Attributs

    private int x=512;
    private int y=512;
    private boolean click=false;

    private int smTab=0;        //machine d'etat qui gere la la relache d'une action (droite gauche click)

    //Methodes


    public Joystick() {
    }

    public void JsonParser(JSONObject json){

        //JOYSTICK- NAVIGATION
        try {
            x = Integer.parseInt(json.getString("JOYX") );
            y = Integer.parseInt(json.getString("JOYY") );
            click = Boolean.parseBoolean(json.getString("JOYCLICK"));

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public int ChangeScreen(int currentScreen){
        switch (smTab){
            case 0: //wait action

                if( (x>900)&&(currentScreen <2) ){
                    currentScreen = currentScreen +1;
                    smTab = 1;
                }
                else{
                    if( (x<200)&&(x!=0)&&(currentScreen >0)){
                        currentScreen = currentScreen -1;
                        smTab = 2;
                    }
                }
                break;

            case 1: //wait realease droit
                if( (x<700) ){ smTab = 0;  }         //release du joystick
                break;

            case 2: //wait realease gauche
                if( (x>400) ){smTab = 0;  }        //release du joystick
                break;

            default:
                //error
                break;

        }
        return smTab;
    }
}
