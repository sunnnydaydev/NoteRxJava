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

有了流程简介这里就很好入手了，创建“观察者”、被观察者。然后让被观察者订阅观察者即可。这样被观察者触发事件时，观察者就能立即收到响应。

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

流程很简单，主要设计到几个类，Subscriber、Observer、OnSubscribe、Observable。

Subscriber：观察者，实现了Observer接口。

```java
/**
1、抽象类，实现了Observer接口、Subscription接口。
2、Observer是一个接口。
*/
public abstract class Subscriber<T> implements Observer<T>, Subscription {
        ...
        //在Observer基础上添加了onStart方法，这个方法在onNext方法被调用之前被“自动”被调用。
        public void onStart() {}
        ...
}
```

Observer：观察者，是一个接口，接口很简单定义了三个方法。

```java
public interface Observer<T> {
    /**
    1、事件完成时回调。需要手动调用.调用这个方法时代表事件结束。后续的事件不会执行如：
    
     // Subscriber中只会回调onStart、onCompleted
        Observable.create(Observable.OnSubscribe<String> {
            it.onCompleted()// 后续代码不会执行。
            it.onNext("hello")
            it.onNext("RxJava")
            it.onNext((1 / 0).toString())
        })
   （2） 当触发onError时这个方法不会被调用。
     //Subscriber中会回调onStart、onNext、onError
           Observable.create(Observable.OnSubscribe<String> {
            it.onNext("hello")
            it.onNext("RxJava")
            it.onNext((1 / 0).toString())//代码触发onError
            it.onCompleted()
        })
    */
    void onCompleted();
    /**
    事件执行出现异常时回调。
    */
    void onError(Throwable e);
    /**
    执行事件。
    */
    void onNext(T t);
}   
```

Observable，被观察者，内部提主要供了create方法来创建Observable对象。核心是有一系列“操作符”，可以进行对象创建、数据转换、线程调度。

```java
public class Observable<T> {
       ...
        /**
         Action1也是一个接口内部只定义个call方法： void call(T t);
        */
        public interface OnSubscribe<T> extends Action1<Subscriber<? super T>> {}
    
        public static <T> Observable<T> create(OnSubscribe<T> f) {
        return new Observable<T>(hook.onCreate(f));
    }
        //订阅
        public final Subscription subscribe(Subscriber<? super T> subscriber) {
        return Observable.subscribe(subscriber, this);
    }
        //很多操作符，其一。作用数据转换。
        public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return lift(new OperatorMap<T, R>(func));
    }
       ...
}
```



熟悉了上面的知识后我们就可以整理下代码了，简介写法：

```java
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
```



# 参考

[关于RxJava最友好的文章](https://mp.weixin.qq.com/s/6-0jFN_BwKOlwOK3kha90g)

[RxJava](https://github.com/ReactiveX/RxJava)

[RxAndroid](https://github.com/ReactiveX/RxAndroid)

[Carson带你学Android：这是一份全面 & 详细的RxJava学习指南](https://www.jianshu.com/p/d9b504f5b3bd)





