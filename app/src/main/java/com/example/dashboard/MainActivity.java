package com.example.dashboard;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

import org.json.JSONArray;
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
    int myRefreshViewPeriod = 1000;
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
        //startClient();







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

                clientSocket.setSoTimeout(900);
                Log.i(debugString,"wait data from serveur...");
                String dataMoto=""+br.readLine();

                Log.i(debugString,"RECEIVE: "+dataMoto);

                mySensors.setText(""+dataMoto+" compteur="+compteur+""+"compteurErreur="+compteurErreur);
                try {
                    JSONObject json_obj = new JSONObject(dataMoto);

                    //RPM
                    //myDashboard.setRpm(""+json_obj.getString("RPM")+"/"+compteur );
                    myDashboard.setRpm(""+json_obj.getString("RPM") );

                    //rempli la table de temperature du sensor tyre front
                    for(int y=0;y<4;y++){
                        for(int x=0;x<16;x++) {
                            String cell = Character.toString((char)(int)(97+y))+x;
                            tempTyreFront[y][x]= json_obj.getString(cell);
                            tempTyreFrontInt[y][x] = Integer.parseInt(tempTyreFront[y][x]);
                        }
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
                                if( (joyX<200)&&(IhmTab >0) ){
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

            final TextView tvTyreF = (TextView)findViewById(R.id.tyreF);
            ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
                @Override
                public Shader resize(int width, int height) {
                    LinearGradient lg = new LinearGradient(0, 0, tvTyreF.getWidth(), tvTyreF.getHeight(),
                            new int[]   {
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][0],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][1],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][2],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][3],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][4],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][5],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][6],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][7],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][8],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][9],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][10],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][11],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][12],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][13],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][14],240.0f,127.0f}),
                                    Color.HSVToColor(new float[]{tempTyreFrontInt[0][15],240.0f,127.0f}),

                            }, //substitute the correct colors for these
                            new float[] {0,0.0625f,0.125f,0.1875f,0.25f,0.3125f,0.375f,0.4375f,0.5f,0.5625f,0.6875f,0.75f,0.8125f,0.875f,0.9375f,1},
                            //new float[] {0,0.5f,1},

                            Shader.TileMode.REPEAT);
                    return lg;
                }
            };
            PaintDrawable p = new PaintDrawable();
            p.setShape(new RectShape());
            p.setShaderFactory(sf);
            tvTyreF.setBackground((Drawable)p);
            tvTyreF.getBackground().setAlpha(128);


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
