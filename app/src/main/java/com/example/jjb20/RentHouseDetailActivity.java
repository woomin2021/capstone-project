package com.example.jjb20;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class RentHouseDetailActivity extends AppCompatActivity {

    private ViewPager2 viewPaperImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent_house_detail_page);

        viewPaperImage = findViewById(R.id.viewPagerImages);

        viewPaperImage.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        List<Integer> imageList = Arrays.asList(
                R.drawable.sample1,
                R.drawable.sample2

        );

        ImageSliderAdapter adapter = new ImageSliderAdapter(imageList);
        viewPaperImage.setAdapter(adapter);

    }
}
