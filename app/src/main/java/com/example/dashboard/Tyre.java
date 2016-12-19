package com.example.dashboard;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fle on 19/12/2016.
 */

public class Tyre {

    //******  Attribut

    MLX90621 Mlx90621 = new MLX90621();

    //******  Methode


    public Tyre() {
        //constructor
    }

    public void JsonToColor(JSONObject json){          //retourne un tableau unedimansion avec les couleurs format HSV

       Mlx90621.JsonToColor(json);
    }

    public int[] GetGradient(){          //retourne un tableau unedimansion avec les couleurs format HSV

        return Mlx90621.getColorGradient();
    }
}
