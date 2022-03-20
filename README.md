# RxJava
![Rxjava](https://github.com/sunnnydaydev/NoteRxJava/blob/master/screenshot/Rxjava.png)

# 介绍

###### 异步事件流

Rxjava是基于异步事件的，到底啥是“异步事件”呢？我们刚接触这个概念时就理解成“多线程”，“线程切换”这些概念即可。

###### 观察者模式

观察者是一种设计模式，类似于事件的订阅、发布。订阅者订阅了感兴趣的事件，当事件发布时订阅者就能立刻接收到。

看看Github官方网站Rxjava在安卓上的扩展库RxAndroid已经更新到3.0版本了，这里就以最初的Rxjava1.X的版本来总结下~ 有了这些基础，后来版本的变化抽时间了解即可~

在Rxjava中也有观察者订阅者的概念，而且可能还会使人感到迷惑，这里就先介绍：

观察者：Observer/Subscriber，可见在Rxjava中观察者有两个概念Observer和Subscriber。Observer我们很好理解，Observe的名词形式。然而Subscriber就让人感到迷糊了，中文意思明明是“订阅者” 这里却被设计成了观察者。让人感到很奇怪。我猜测大概是Observer是一个接口，Subscriber实现了了这个接口的缘故吧~

被观察者：Observable ，中文意思原意是“可观察的”意思。既然是可观察的意思，翻译成被观察者还算合理。

订阅：subscribe 。翻译成汉语也是订阅。是一个动词。

###### 流程简介



总结就一句话，被观察者向观察者“订阅”事件，被观察者触发事件时，观察者即可监听到。

![Rxjava](https://github.com/sunnnydaydev/NoteRxJava/blob/master/screenshot/flow.png)

# 使用

###### 1、基本使用

有了流程简介这里就很好入手了，创建“观察者”、被观察者。然后让被观察者订阅事件即可。

```java

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        basicUsage()
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
     * log封装，方便使用。
     * */
    private fun logD(tag: String, msg: () -> String) {
        Log.d(tag, msg.invoke())
    }
}
```

其实也是很好理解的，

# 参考

[关于RxJava最友好的文章](https://mp.weixin.qq.com/s/6-0jFN_BwKOlwOK3kha90g)

[RxJava](https://github.com/ReactiveX/RxJava)

[RxAndroid](https://github.com/ReactiveX/RxAndroid)

[Carson带你学Android：这是一份全面 & 详细的RxJava学习指南](https://www.jianshu.com/p/d9b504f5b3bd)





