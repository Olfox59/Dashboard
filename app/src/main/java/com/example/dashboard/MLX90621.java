package com.example.dashboard;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fle on 19/12/2016.
 */

public class MLX90621 {

    //Attribut
    private int[][] matrixTempStr;
    private int[] tempOneRow;
    private int[] tempColor;
    private float gradientPosition[] = new float[] {0,0.0625f,0.125f,0.1875f,0.25f,0.3125f,0.375f,0.4375f,0.5f,0.5625f,0.6875f,0.75f,0.8125f,0.875f,0.9375f,1};

    //Methode

    public MLX90621() {
    }

    public void JsonToColor(JSONObject json){

        ParseJSON(json);
        MeanRow();
        TempToColor();
    }

    public int[] getColorGradient(){

        return tempColor;
    }

    public float[] getGradientpostion(){

        return gradientPosition;
    }

    private void ParseJSON(JSONObject json){

        for(int y=0;y<4;y++){                                                       //rempli la table de temperature du sensor tyre
            for(int x=0;x<16;x++) {
                String cell = Character.toString((char)(int)(97+y))+x;              //creer le champ a chercher ex: c14
                try {
                    matrixTempStr[y][x] = Integer.parseInt(json.getString(cell) );
                } catch (JSONException e) {
                    e.printStackTrace();                                            //erreur s'il ne trouve pas le champ
                }
            }
        }
    }

    //fait la moyenne de chque collonne car la temp doit etre uniforme
    //rempli la table de temperature du sensor tyre front

    private void MeanRow(){

        for(int x=0;x<16;x++) {
            tempOneRow[x]= (    matrixTempStr[0][x]+
                                matrixTempStr[1][x]+
                                matrixTempStr[2][x]+
                                matrixTempStr[3][x])/4;
        }

    }

    private void TempToColor(){

        float hueCOLD = 150.0f; //BLEU      valeur hsv
        float hueHOT = 0.0f;   //ROUGE
        float tempCOLD = 30.0f;  //en degC correspondant au BLEU
        float tempHOT = 100.0f;  //en degC correspondant au BLEU

        float a = (hueCOLD / (tempCOLD - tempHOT));
        float b = hueHOT - (tempHOT*a);

        float value = 1.0f;
        float saturation = 1.0f;

        for(int x=0;x<16;x++) {

            tempColor[x] = (int)((-a* tempOneRow[x]) + b);  //le cast en int va aroundir a l'inferieur

            if(     tempColor[x]>hueCOLD){  tempColor[x]=(int)hueCOLD;   }         //triger pour clamper la valeur des couleur
            else if(tempColor[x]<hueHOT){   tempColor[x]=(int)hueHOT;    }

            tempColor[x] = Color.HSVToColor( new float[]{tempColor[x],saturation,value} );
        }

    }


}
