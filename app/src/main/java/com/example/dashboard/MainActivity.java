package com.example.dashboard;

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

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

import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.dashboard.databinding.FragmentDashBinding;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

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
    public int compteur=0;

    //Attribut Architecture
    Handler mHandler;
    int myRefreshViewPeriod = 100;

    //Attribut Dashboard
    final static Dashboard myDashboard = new Dashboard("1500");

    //connection tcp
    public static final String SERVER_IP = "192.168.4.1";
    public int SERVER_PORT = 2525;
    Socket clientSocket= null;
    Thread m_ObjThreadClient;


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
                    Log.i(debugString,"Input Thread");
                    //Log.i(debugString,"Try to connect ....");
                    clientSocket = new Socket(SERVER_IP,SERVER_PORT);
                    Log.i(debugString,"connect to socket");
                    if(clientSocket.isConnected() ){
                        Log.i(debugString,"Client connected");
                    }
                    else{
                        Log.i(debugString,"Client NOT connected");
                    }
                    //ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                   // Log.i(debugString,"Sending to server...");
                    bw.write(""+compteur );

                    bw.newLine();
                    bw.flush();
                    Log.i(debugString,"Send !!!");

                    clientSocket.setSoTimeout(90);

                    //ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                    BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    //Log.i(debugString,"Wait for read...");
                    //System.out.println("message from server:"+br.readLine());

                    Log.i(debugString,"message from server:"+br.readLine());


                    /*String fileName = "temp.txt";


                        // Assume default encoding.
                        FileWriter fileWriter =     new FileWriter(fileName);

                        // Always wrap FileWriter in BufferedWriter.
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                        // Note that write() does not automatically
                        // append a newline character.
                        bufferedWriter.write(br.readLine());
                        bufferedWriter.newLine();

                        // Always close files.
                        bufferedWriter.close();

*/




                    clientSocket.close();
                    Log.i(debugString,"Socket closed");

                } catch (IOException e) {
                    Log.i(debugString,"error");
                    System.out.println(e);
                    e.printStackTrace();
                    try {
                        clientSocket.close();
                        Log.i(debugString,"TIMEOUT: Socket closed/r/n---------------/r/n ");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    //Log.e(debugString,e.getMessage());
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

            //Log.i("Handlers", ""+compteur);
            myDashboard.setRpm(""+compteur );
            myDashboard.setRpmprogress(compteur);
            //myDashboard.setRpmprogress(16000);
            //compteur=(compteur+100)%15000;
            compteur=compteur+1;
            startClient();
            Map<String,Object> toto = new HashMap<String,Object>();
            toto.put("tkt",7);
            toto.put("tkt2","toto");
            toto.get("tkt");
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
