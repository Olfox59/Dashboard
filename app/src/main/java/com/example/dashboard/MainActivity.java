package com.example.dashboard;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dashboard.databinding.FragmentDashBinding;
import com.example.dashboard.databinding.FragmentSensorBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private static final String debugString="debug";
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    //Attribut debug
    public  int compteur=0;
    public int compteurErreur=0;
    public int IhmTab =-1;

    //Attribut Architecture
    Handler mHandler;
    int myRefreshViewPeriod = 500;
    int myTCPTimeOut = myRefreshViewPeriod - 100;
int mycol=0;
    //Attribut Dashboard
    final static Dashboard myDashboard = new Dashboard("1500");
    final static Sensors mySensors = new Sensors();

    //Attribut connection tcp
    public static final String SERVER_IP = "192.168.4.1";
    public int SERVER_PORT = 333;
    Socket clientSocket= null;
    Thread m_ObjThreadClient;
    //Wifi
    String ssid;
    WifiInfo wifiInfo;
    //WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);


    //Attribut Joystick
    int smTab=0;

    //attribut mlx90621
    String [][]tempTyreFront = new String[4][16];
    int [][]tempTyreFrontInt = new int[4][16];
    int []tempTyreFrontMean = new int[16];


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);



        //fle lance le timer
        useHandler();
        Log.i(debugString,"fin oncreate");

    }

    public void startClient(){


        m_ObjThreadClient = new Thread(new Runnable() {
            @Override
            public void run() {



            try {

                Log.i(debugString,"Input Thread"+compteur);

                compteur++;

                //OPEN SOKET
                clientSocket = new Socket(SERVER_IP,SERVER_PORT);

                Log.i(debugString,"connexion OK"+compteur);


                //WRITE
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                bw.write(""+compteur );
                bw.newLine();
                bw.flush();
                Log.i(debugString,"SEND: "+compteur);



                // READ
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream() ) );

                clientSocket.setSoTimeout(myTCPTimeOut);
                Log.i(debugString,"wait data from serveur...");
                String dataMoto=""+br.readLine();

                Log.i(debugString,"RECEIVE: "+dataMoto);

                mySensors.setText(""+dataMoto+" compteur="+compteur+""+"compteurErreur="+compteurErreur);
                try {
                    JSONObject json_obj = new JSONObject(dataMoto);

                    //RPM ************************************
                    //myDashboard.setRpm(""+json_obj.getString("RPM")+"/"+compteur );
                    myDashboard.setRpm(""+json_obj.getString("RPM") );

                    //TYRE*************************************



                    //rempli la table de temperature du sensor tyre front
                    for(int y=0;y<4;y++){
                        for(int x=0;x<16;x++) {
                            String cell = Character.toString((char)(int)(97+y))+x;                  //creer le champ a chercher ex: c14
                            //tempTyreFront[y][x]= json_obj.getString(cell);                          //get la valeur du champ dans json
                            //tempTyreFrontInt[y][x] = Integer.parseInt(tempTyreFront[y][x]);         //convert string to int
                            tempTyreFrontInt[y][x] = Integer.parseInt(json_obj.getString(cell) );
                        }
                    }

                    //fait la moyenne de chque collonne car la temp doit etre uniforme
                    //rempli la table de temperature du sensor tyre front

                    for(int x=0;x<16;x++) {
                            tempTyreFrontMean[x]= (  tempTyreFrontInt[0][x]+
                                                    tempTyreFrontInt[1][x]+
                                                    tempTyreFrontInt[2][x]+
                                                    tempTyreFrontInt[3][x])/4;
                    }






                    //myDashboard.setRpmprogress(Integer.parseInt(myDashboard.getRpm()) );
                    Log.i(debugString,"rpm recu "+myDashboard.getRpm());

                    //JOYSTICK- NAVIGATION
                    int joyX = Integer.parseInt(json_obj.getString("JOYX") );
                    int joyY = Integer.parseInt(json_obj.getString("JOYY") );
                    int joyClick = Integer.parseInt(json_obj.getString("JOYCLICK") );

                    IhmTab = mViewPager.getCurrentItem();

                    switch (smTab){
                        case 0: //wait action

                            if( (joyX>900)&&(IhmTab <2) ){
                                IhmTab = IhmTab +1;
                                smTab = 1;
                            }
                            else{
                                if( (joyX<200)&&(joyX!=0)&&(IhmTab >0)){
                                    IhmTab = IhmTab -1;
                                    smTab = 2;
                                }
                            }
                            break;

                        case 1: //wait realease droit
                            if( (joyX<700) ){ smTab = 0;  }         //release du joystick
                            break;

                        case 2: //wait realease gauche
                            if( (joyX>400) ){smTab = 0;  }        //release du joystick
                            break;

                        default:
                            //error
                            break;

                    }

                } catch (JSONException e) {
                    Log.i(debugString,"erreur JSON:"+e);
                    e.printStackTrace();
                }


                //CLOSE SOCKET
                clientSocket.close();
                Log.i(debugString,"Socket closed");

            } catch (IOException e) {
                compteurErreur++;
                Log.e(debugString,""+e+""+compteurErreur);
                e.printStackTrace();
                Log.e(debugString,"Stat="+(float)(((float)compteurErreur/(float)compteur)*100.0)+"%" );
                mySensors.setText(" compteur="+compteur+""+"compteurErreur="+compteurErreur);

                try {
                    clientSocket.close();

                }
                catch (IOException e1) {
                    e1.printStackTrace();
                    Log.i(debugString,"FAIL close"+compteurErreur);
                }
            }

            }
        });

        m_ObjThreadClient.start();
    }

    public void useHandler() {
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, myRefreshViewPeriod);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            //Update Sensor color Gradient
            //color Hue in HSL format = 1.74 * temperateure cellule - 52.2
            //if color<0 , color = 0
            //if color>87 (vert fluo), color=87
//             final View vTyreF = (View)findViewById(R.id.tyreFGradient);
            //myDrawGradientTempToColorScale(tempTyreFrontMean);        //rescale les couleurs avant d'afficher
//            float hueCOLD = 226.0f; //BLEU
//            float hueHOT = 0.0f;   //ROUGE
//            float tempCOLD = 30.0f;  //en degC correspondant au BLEU
//            float tempHOT = 100.0f;  //en degC correspondant au BLEU
//
//            float a = (hueCOLD / (tempCOLD - tempHOT));
//            float b = hueHOT - (tempHOT*a);
//
//            for(int x=0;x<16;x++) {
//                tempTyreFrontMean[x] = (int)((a*tempTyreFrontMean[x]) + b);  //le cast en int va aroundir a l'inferieur
//                if(tempTyreFrontMean[x]>hueCOLD){
//                    tempTyreFrontMean[x]=(int)hueCOLD;
//                }
//                else if(tempTyreFrontMean[x]<hueHOT){
//                    tempTyreFrontMean[x]=(int)hueHOT;
//                }
//            }
//
//            ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
//                @Override
//                public Shader resize(int width, int height) {
//                    LinearGradient lg = new LinearGradient(0, vTyreF.getHeight(), vTyreF.getWidth(), vTyreF.getHeight(),
//                            new int[]   {
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[0],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[1],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[2],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[3],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[4],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[5],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[6],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[7],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[8],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[9],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[10],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[11],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[12],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[13],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[14],240.0f,127.0f}),
//                                    Color.HSVToColor(new float[]{tempTyreFrontMean[15],240.0f,127.0f}),
//
//                            }, //substitute the correct colors for these
//                            new float[] {0,0.0625f,0.125f,0.1875f,0.25f,0.3125f,0.375f,0.4375f,0.5f,0.5625f,0.6875f,0.75f,0.8125f,0.875f,0.9375f,1},
//                            //new float[] {0,0.5f,1},
//
//                            Shader.TileMode.REPEAT);
//                    return lg;
//                }
//            };
//            PaintDrawable p = new PaintDrawable();
//            p.setShape(new RectShape());
//            p.setCornerRadius(105.0f); //dimension image 679x201
//
//            p.setShaderFactory(sf);
//            vTyreF.setBackground((Drawable)p);
//            vTyreF.getBackground().setAlpha(128);  //gestion de la transparence du gradient 0=100%trans, 128 = 50%,255 = opaque


            final View vTyreF = (View)findViewById(R.id.tyreFGradient);
            myDrawGradientTempToColorScale(tempTyreFrontMean);        //rescale les couleurs avant d'afficher
            myDrawGradient(vTyreF,tempTyreFrontMean);                //test Avec les methode pour re organaiser proprement



            //Log.i("Handlers", ""+compteur);

            //myDashboard.setRpmprogress(compteur);
            //myDashboard.setRpmprogress(16000);
            //compteur=(compteur+100)%15000;

            //wifiInfo = wifiManager.getConnectionInfo();
            //if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            //    ssid = wifiInfo.getSSID();
            //}
            //if(ssid="OlfoxDas") {
                //mViewPager.setCurrentItem(IhmTab, true);      //change tab with joystick X
                startClient();


            //}

            /** Do something **/
            mHandler.postDelayed(mRunnable, myRefreshViewPeriod);


        }
    };

    /************************************
     * Methode qui converti les 16 temperatures en couleurs
     * temp<30degC BLEU , 80deg VERT , temp>100degC ROUGE
     * @param tempTyreMean
     */

    private void myDrawGradientTempToColorScale(int [] tempTyreMean){
        float hueCOLD = 150.0f; //BLEU
        float hueHOT = 0.0f;   //ROUGE
        float tempCOLD = 30.0f;  //en degC correspondant au BLEU
        float tempHOT = 100.0f;  //en degC correspondant au BLEU

        float a = (hueCOLD / (tempCOLD - tempHOT));
        float b = hueHOT - (tempHOT*a);

        for(int x=0;x<16;x++) {
            tempTyreMean[x] = (int)((-a*tempTyreMean[x]) + b);  //le cast en int va aroundir a l'inferieur
            if(tempTyreMean[x]>hueCOLD){
                tempTyreMean[x]=(int)hueCOLD;
            }
            else if(tempTyreMean[x]<hueHOT){
                tempTyreMean[x]=(int)hueHOT;
            }
        }
    }

    /******************************
     * Methode qui affiche le gradient de temperature dans la textBox
     * @param tvTyre
     */
    private void myDrawGradient(final View tvTyre,final int tempTyreMean[]){

        ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
            @Override
            public Shader resize(int width, int height) {
                LinearGradient lg = new LinearGradient(0, tvTyre.getHeight(), tvTyre.getWidth(), tvTyre.getHeight(),
                        new int[]   {
                                Color.HSVToColor(new float[]{tempTyreMean[0],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[1],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[2],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[3],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[4],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[5],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[6],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[7],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[8],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[9],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[10],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[11],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[12],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[13],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[14],240.0f,127.0f}),
                                Color.HSVToColor(new float[]{tempTyreMean[15],240.0f,127.0f}),

                        }, //substitute the correct colors for these
                        new float[] {0,0.0625f,0.125f,0.1875f,0.25f,0.3125f,0.375f,0.4375f,0.5f,0.5625f,0.6875f,0.75f,0.8125f,0.875f,0.9375f,1},
                        //new float[] {0,0.5f,1},

                        Shader.TileMode.REPEAT);
                return lg;
            }
        };
        PaintDrawable p = new PaintDrawable();
        p.setShape(new RectShape());
        p.setCornerRadius(105.0f); //dimension image 679x201

        p.setShaderFactory(sf);
        tvTyre.setBackground((Drawable)p);
        tvTyre.getBackground().setAlpha(128);  //gestion de la transparence du gradient 9, 100%trans, 128 = 50%

    }


    /*********************************************
     *
     * fonction de bases gestion appli multi onglet
     */


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;

            switch(getArguments().getInt(ARG_SECTION_NUMBER)){

                case 1:

                    FragmentDashBinding binding = DataBindingUtil.inflate(inflater,R.layout.fragment_dash,container,false);
                    binding.setDash(myDashboard);
                    View view = binding.getRoot();

                    return view;

                case 2:

                    FragmentSensorBinding binding2 = DataBindingUtil.inflate(inflater,R.layout.fragment_sensor,container,false);
                    binding2.setCapteur(mySensors);
                    View view2 = binding2.getRoot();

                    return view2;


                default:    rootView = inflater.inflate(R.layout.fragment_main, container, false);
                    return rootView;

            }

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Dash";
                case 1:
                    return "Sensors";
                case 2:
                    return "Chrono";
            }
            return null;
        }
    }
}
