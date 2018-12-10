package com.izho.progressbartest;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView[] textViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textViews = new TextView[10];
        PagerAdapter adapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return 5;
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
                return false;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                textViews[position] = new TextView(getApplicationContext());
                textViews[position].setText(position + "");
                container.addView(textViews[position]);
                return container;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(textViews[position]);
            }
        };

        //stub viewpager as driver
        final ViewPager x = (ViewPager) findViewById(R.id.viewPager);
        x.setAdapter(adapter);
        View view = findViewById(R.id.indicator);

        AnimatedDotLineViewPagerIndicator indicator = (AnimatedDotLineViewPagerIndicator) view;
        indicator.setStrokeWidth(7);
        indicator.setSeparationLineLength(getResources().getDimension(R.dimen.default_separation));
        indicator.setFillColor(Color.parseColor("#4286f4"));
        indicator.setStrokeColor(Color.parseColor("#9398a0"));
        indicator.setViewPager(x);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPage = x.getCurrentItem();
                if(x.getCurrentItem() < x.getChildCount() + 1) {
                    x.setCurrentItem(currentPage+1);
                } else {
                    x.setCurrentItem(0);
                }
            }
        });
    }
}
