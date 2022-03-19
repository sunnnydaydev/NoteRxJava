package com.sunnyday.constraintlayout.noterxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import rx.Subscriber


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 1、创建观察者
        val subscriber = object : Subscriber<String>() {
            override fun onStart() {
                //
            }

            override fun onCompleted() {

            }

            override fun onError(e: Throwable?) {

            }

            override fun onNext(t: String?) {

            }
        }
    }
}