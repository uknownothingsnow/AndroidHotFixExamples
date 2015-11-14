package com.github.lzyzsd.androidhotfixexamples;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("------------start");
                DexLoaderUtil.copyDex(MainActivity.this, DexLoaderUtil.SECONDARY_DEX_NAME);
                DexLoaderUtil.copyDex(MainActivity.this, DexLoaderUtil.THIRD_DEX_NAME);
//                DexLoaderUtil.loadAndCall(MainActivity.this, DexLoaderUtil.SECONDARY_DEX_NAME);
                String secondDexPath = new File(getDir("dex", Context.MODE_PRIVATE), DexLoaderUtil.SECONDARY_DEX_NAME).getAbsolutePath();
                String thirdDexPath = new File(getDir("dex", Context.MODE_PRIVATE), DexLoaderUtil.THIRD_DEX_NAME).getAbsolutePath();
                final String optimizedDexOutputPath = getDir("outdex", Context.MODE_PRIVATE).getAbsolutePath();
                DexLoaderUtil.injectAboveEqualApiLevel14(thirdDexPath, optimizedDexOutputPath, null, "com.example.MyClass");
                DexLoaderUtil.injectAboveEqualApiLevel14(secondDexPath, optimizedDexOutputPath, null, "com.example.MyClass");
                DexLoaderUtil.call(getClassLoader());
                System.out.println("------------end");
            }
        }).start();
    }
}
