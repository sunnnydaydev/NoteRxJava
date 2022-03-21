package com.sunnyday.constraintlayout.noterxjava

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import rx.Observable
import rx.Subscriber


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
       // basicUsage()
        knownUsage()
    }

    /**
     * 基本使用
     * */
    private fun basicUsage() {
        // 1、创建观察者
        val subscriber = object : Subscriber<String>() {
            override fun onStart() {
                logD(TAG) {
                    "onStart"
                }
            }

            override fun onCompleted() {
                logD(TAG) {
                    "onCompleted"
                }
            }

            override fun onError(e: Throwable?) {
                logD(TAG) {
                    "onError"
                }
                e?.printStackTrace()
            }

            override fun onNext(t: String?) {
                logD(TAG) {
                    "onNext:$t"
                }
            }
        }
        // 2、创建被观察者
        val observable = Observable.create(object : Observable.OnSubscribe<String> {
            override fun call(t: Subscriber<in String>?) {
                // 被观察者做事情
                t?.let {
                    //通过onNext触发事件
                    it.onNext("hello")
                    it.onNext("RxJava")
                }
            }
        })

        // 3、被观察者注册观察者
        observable.subscribe(subscriber)

        /**
        log:
        D/MainActivity: onStart
        D/MainActivity: onNext:hello
        D/MainActivity: onNext:RxJava
         * */
    }

    /**
     * 熟悉写法：链式调用+Lambda
     * */
    private fun knownUsage() {
        Observable.create(Observable.OnSubscribe<String> {
            it.onNext("hello")
            it.onNext("RxJava")
            it.onNext((1 / 0).toString())
            it.onCompleted()
        }).subscribe(object : Subscriber<String>() {
            override fun onStart() {
                logD(TAG) {
                    "knownUsage#onStart"
                }
            }

            override fun onCompleted() {
                logD(TAG) {
                    "knownUsage#onCompleted"
                }
            }

            override fun onError(e: Throwable?) {
                logD(TAG) {
                    e?.printStackTrace()
                    "knownUsage#onError"
                }
            }

            override fun onNext(t: String?) {
                logD(TAG) {
                    "knownUsage#onNext:$t"
                }
            }

        })
    }

    /**
     * log封装，方便使用。
     * */
    private fun logD(tag: String, msg: () -> String) {
        Log.d(tag, msg.invoke())
    }
}