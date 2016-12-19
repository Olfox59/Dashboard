package com.example.dashboard;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.dashboard.databinding.FragmentDashBinding;
import com.example.dashboard.databinding.FragmentSensorBinding;

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


    //Attribut Architecture
    Handler mHandler;
    MyTask myAsynctask = new MyTask(MainActivity.this);

    int myRefreshViewPeriod = 500;
    int myTCPTimeOut = myRefreshViewPeriod - 100;

    final static Dashboard myDashboard = new Dashboard("1500");
    final static Sensors mySensors = new Sensors();

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


    public void useHandler() {
        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, myRefreshViewPeriod);
    }

    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {

            Log.i("debug", "Call Asynctask ");

            myAsynctask.execute();
            mySensors.setText(""+myAsynctask.frontTyre.GetGradient());
            //mViewPager.setCurrentItem(IhmTab, true);      //change tab with joystick X

            /** Do something **/





    /******************************
     * Methode qui affiche le gradient de temperature dans la textBox
     * @param tvTyre
     */



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
