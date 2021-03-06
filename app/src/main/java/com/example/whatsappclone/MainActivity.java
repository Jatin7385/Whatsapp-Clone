package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button search;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = (Button)findViewById(R.id.search);


        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpager);

        tabLayout.addTab(tabLayout.newTab().setText("CHATS"));
        tabLayout.addTab(tabLayout.newTab().setText("PROFILE"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final MainAdapter adapter = new MainAdapter(this,getSupportFragmentManager(),tabLayout.getTabCount());

        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });
    }

    private class MainAdapter extends FragmentPagerAdapter {

        private Context myContext;
        private int totalTabs;

        public MainAdapter(Context context, FragmentManager fm , int totalTabs) {
            super(fm);
            myContext = context;
            this.totalTabs = totalTabs;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position){
                case 0:
                    ChatsFragment chatsFragment = new ChatsFragment();
                    return chatsFragment;
                case 1:
                    ProfileFragment profileFragment = new ProfileFragment();
                    return profileFragment;
                default:
            }
            return null;
        }

        @Override
        public int getCount() {
            return totalTabs;
        }
    }
}