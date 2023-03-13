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

首先可以添加下依赖~

```java
//    implementation 'io.reactivex.rxjava3:rxandroid:3.0.0'
//    implementation 'io.reactivex.rxjava3:rxjava:3.0.0'
    implementation 'io.reactivex:rxjava:1.1.6'
    implementation 'io.reactivex:rxandroid:1.2.1'
```



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

###### 2、Observable的简洁创建法

create方法创建Observable对象时需要提供OnSubscribe实现类对象，并且需要手动调用onNext。其实Observable还提供了其他的方法可以帮互我们快速创建，触发事件。

- public static < T > Observable< T > just(final T value)：Observable提供了10个just重载方法，只是参数个数不同而已。
- public static < T > Observable< T > from(T[] array) ：Observable提供了好几个from重载方法，最常用的from方法就是传递一个泛型数组。

（1）just

```java
    /**
     * 使用just快速创建Observable对象，并触发事件。
     * */
    private fun createObservableQuicklybyJust() {
        Observable.just("hello", "RxJava")
            .subscribe(object : Subscriber<String>() {
                override fun onStart() {
                    logD(TAG) {
                        "createObservableQuickly#onStart"
                    }
                }
                override fun onCompleted() {
                    logD(TAG) {
                        "createObservableQuickly#onCompleted"
                    }
                }

                override fun onError(e: Throwable?) {
                    logD(TAG) {
                        "createObservableQuickly#onError"
                    }
                }

                override fun onNext(t: String?) {
                    logD(TAG) {
                        "createObservableQuickly#onNext:$t"
                    }
                }

            })
    }
log:
 D/MainActivity: createObservableQuickly#onStart
 D/MainActivity: createObservableQuickly#onNext:hello
 D/MainActivity: createObservableQuickly#onNext:RxJava
 D/MainActivity: createObservableQuickly#onCompleted
```

可见just比create更加简洁，内部自动完成了很多工作，我们把想要触发的“事件”当做参数传递过来即可。just自动完成发射工作。

在create中onCompleted需要的话还需要我们手动去调用，这里就不需要，真的方便多了。

（2）from

```java
    /**
     * 使用from快速创建Observable对象，并触发事件。
     * */
    private fun createObservableQuicklyByFrom() {
        //int 类型数组，onNext中发射int 类型数据
        val arr = arrayOf(1, 2)
        Observable.from(arr)
            .subscribe(object : Subscriber<Int>() {
                override fun onStart() {
                    logD(TAG) {
                        "createObservableQuicklyByFrom#onStart"
                    }
                }
                override fun onCompleted() {
                    logD(TAG) {
                        "createObservableQuicklyByFrom#onCompleted"
                    }
                }

                override fun onError(e: Throwable?) {
                    logD(TAG) {
                        "createObservableQuicklyByFrom#onError"
                    }
                }

                override fun onNext(t: Int) {
                    logD(TAG) {
                        "createObservableQuicklyByFrom#onNext:$t"
                    }
                }

            })
    }
log:
D/MainActivity: createObservableQuicklyByFrom#onStart
D/MainActivity: createObservableQuicklyByFrom#onNext:1
D/MainActivity: createObservableQuicklyByFrom#onNext:2
D/MainActivity: createObservableQuicklyByFrom#onCompleted
```

可见from功能与just功能差不多，接收一个数组类型参数，吧数组每一元素当做onNext的数据发射出去。只是功能比just更加强大，just只提供了10个方法重载，只是参数个数不同。

###### 3、Subscriber的不完全回调

Subscriber为抽象类，实现了Observer接口，当直接创建一个Subscriber实现类时一般我们需要实现Observer接口中定义的全部方法。而Rxjava提供了一个特殊类Action这个类中不需要实现所有的方法：

```java
    private fun action1Usage() {
        Observable.just("hello","Rxjava")
            .subscribe(object : Action1<String> {
                // 处理onNext事件
                override fun call(t: String?) {
                    logD(TAG) {
                        "action1Usage#call:$t"
                    }
                }
            })
    }
log:
D/MainActivity: action1Usage#call:hello
D/MainActivity: action1Usage#call:Rxjava
```

上述是Action1类的用法，其实Action1就可以看做onNext(t:T) 类型的数据。此外Rxjava提供了Action0，Action1,,,,Action10等一些列类，区别就是相当于onNext(t:T) 参数个数不同。如：

```java
/**
 * A zero-argument action.
 */
public interface Action0 extends Action {
    void call();
}
/**
 * A two-argument action.
 * @param <T1> the first argument type
 * @param <T2> the second argument type
 */
public interface Action2<T1, T2> extends Action {
    void call(T1 t1, T2 t2);
}
```

Action1其实就是处理onNext事件的，Action1其实就是处理onNext事件的，Action1其实就是处理onNext事件的，那么onCompleted、onError对应的有方法吗？其实是有的：

```java
                // 对应onError 功能，被观察者中触发时这里就回调。
                // 可见就是泛型限定参数为Throwable时就可对应onError 功能
                object : Action1<Throwable> {
                    override fun call(t: Throwable?) {

                    }
                }
               //对应onCompleted的功能。
                object : Action0 {
                    override fun call() {
                    }
                }

```

其实Action1、Action0是不能单独使用的，受Observable#subscribe方法影响~

```java
//1、只需要一个Action1<? super T>类型的参数，代表只有onNext的功能
public final Subscription subscribe(final Action1<? super T> onNext)
    
//2、在上面的基础上多了 Action1<Throwable>类型的参数，代表增加了onError的功能。   
public final Subscription subscribe(final Action1<? super T> onNext, 
                                    final Action1<Throwable> onError)
//3、在上面的基础上又多了 final Action0 ，代表增加了onCompleted的功能  
public final Subscription subscribe(final Action1<? super T> onNext, 
                                    final Action1<Throwable> onError, 
                                    final Action0 onCompleted)
    
public final Subscription subscribe(final Observer<? super T> observer)
```

接下来就可以看下Action0，final Action1< Throwable >的用法了：

```java
    /**
     * Action用法：使用受Observable#subscribe限制。
     * action0->onCompleted
     * action1 ->onNext
     * action1 ->onError
     * */
    private fun actionUsage() {
        Observable.just("hello", "Rxjava")
            .subscribe(
                // 处理onNext事件
                object : Action1<String> {
                    override fun call(t: String?) {
                        logD(TAG) {
                            "actionUsage#Action1:$t"
                        }
                    }
                },
                // 对应onError 功能，just中触发时这里回调。
                object : Action1<Throwable> {
                    override fun call(t: Throwable?) {
                        logD(TAG) {
                            "actionUsage#Action1:$t"
                        }
                    }
                },
                object : Action0 {
                    override fun call() {
                        logD(TAG) {
                            "actionUsage#Action0"
                        }
                    }
                }
            )
    }
```



# 常见操作符

###### 1、map

```java
      public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return lift(new OperatorMap<T, R>(func));
    }
```

这个方法用来做数据转换，参数是Func1类型的参数，Func1与Action1 用法类似唯一区别就是 Func1 带返回值的。

Func1 的泛型需要两个参数T、R。分别代表输入数据、输出数据。接下来看个栗子，根据提供的png文件path来获取bitmap：

```java
    /**
     * Rxjava 操作符Map.
     * */
    private fun mapDemo() {
        //png 文件在本地path
       val path = cacheDir.absolutePath+"/1.png"
       Observable.just(path)
           //数据流经map时，会最为map的“输入参数” 进行转换。
           .map(object : Func1<String,Bitmap> {
               /**
               call方法的含义：输入String类型参数，返回Bitmap类型对象。
               方法的具体转换过程由开发者自己实现。
               */ 
               override fun call(t: String?): Bitmap {
                  return createBitmap(t)
               }

           }).subscribe(object : Action1<Bitmap> {
               override fun call(t: Bitmap) {
                 img.setImageBitmap(t)
               }
           })
    }
    
    // 创建Bitmap对象
    private fun createBitmap(path:String?): Bitmap {
        return BitmapFactory.decodeFile(path)
    }
```

###### 2、flatMap

以一个栗子来引入吧~

```java
/**
 * Create by SunnyDay on 20:48 2022/03/21
 */
data class Student(val name: String,val mList:List<Course>)

data class Course(val name: String, val score: Int)
```



假如有个需求输入一组“学生”，获取学生姓名。map轻轻松松实现~

```java
    private fun printStudentName(){
        val tomCourse = listOf(
            Course("线性代数",80),
            Course("C语言",90)
        )
        val kateCourse = listOf(
            Course("线性代数",70),
            Course("C语言",100)
        )
      val students = arrayOf(Student("Tom",tomCourse),Student("kate",kateCourse))
        Observable.from(students).map{
             it.name
        }.subscribe(object : Action1<String> {
            override fun call(t: String?) {
             logD(TAG){"studentName:$t"}
            }
        })
    }
```

这时需求变了需要查看每个学生的每一门的课程,这时我们或许会考虑下，map不能使用了，或许我还要循环打印数据，然后：

```java
   private fun printStudentCourseByLoop() {
        val tomCourse = listOf(
            Course("线性代数", 80),
            Course("C语言", 90)
        )
        val kateCourse = listOf(
            Course("线性代数", 70),
            Course("C语言", 100)
        )
        val students = arrayOf(Student("Tom", tomCourse), Student("kate", kateCourse))
        Observable.from(students).subscribe(
            object : Action1<Student> {
                override fun call(t: Student?) {
                    val courseList = t?.mList
                    courseList?.forEach {
                        logD(TAG) {
                            val info = "学生:${t.name} $it"
                            info
                        }
                    }
                }
            })
    }
log:
 D/MainActivity: 学生:Tom Course(name=线性代数, score=80)
 D/MainActivity: 学生:Tom Course(name=C语言, score=90)
 D/MainActivity: 学生:kate Course(name=线性代数, score=70)
 D/MainActivity: 学生:kate Course(name=C语言, score=100)
```

如果我们不想在观察者中（Action1）使用循环，那该怎没办呢？

这时我们或许希望向map那样直接传入单个的 Student对象就好了，然后输出学生的每个Course。

可是map做不到这个功能因为学生1个，一个学生有很多课程，这是个1对多的关系，map只能处理1对1的关系~

这时使用flatMap可以解决这个问题：

```java
    /**
     * flapMap:
     * 1对多装换
     * */
    private fun printStudentCourseByFlatMap() {
        val tomCourse = listOf(
            Course("线性代数", 80),
            Course("C语言", 90)
        )
        val kateCourse = listOf(
            Course("线性代数", 70),
            Course("C语言", 100)
        )
        val students = arrayOf(Student("Tom", tomCourse), Student("kate", kateCourse))
        //使用flapMap，参数还是Func1，但是Func1的第二个参数是Observable<T> 类型
        Observable.from(students).flatMap(object : Func1<Student, Observable<Course>> {
            override fun call(t: Student?): Observable<Course> {
                // 包装成Observable对象返回。
                return Observable.from(t?.mList)
            }

        }).subscribe(object : Action1<Course> {
            override fun call(t: Course?) {
                // 直接拿到Course对象
                logD(TAG) { "Course:$t" }
            }
        })
    }
```

###### 3、总结

```java
public final <R> Observable<R> map(Func1<? super T, ? extends R> func) {
        return lift(new OperatorMap<T, R>(func));
    }
    
public final <R> Observable<R> flatMap(Func1<? super T, ? extends Observable<? extends R>> func) {
        if (getClass() == ScalarSynchronousObservable.class) {
        return ((ScalarSynchronousObservable<T>)this).scalarFlatMap(func);
        }
        return merge(map(func));
        }
```

map与flatMap有啥区别呢？

（1）map

map这个方法用来做数据转换，参数是Func1类型的参数，Func1与Action1 用法类似唯一区别就是 Func1 带返回值的。

Func1 的泛型需要两个参数T、R。分别代表输入数据、输出数据。

T就是Observable的数据类型，R就是用户想要转换为的数据类型：

```kotlin
 //这里Observable.from为Observable<Student>类型，经过Map转换后就是Observable<String>类型
 Observable.from(students).map{
             it.name
        }
```

（2）flatMap

flatMap同样是用来做转化的，接受的参数同样是Func1类型的参数，但是Func1的泛型参数不同flatMap吧类型T转化为

Observable<R>,而不是R：

```java
        Observable.from(students).flatMap(object : Func1<Student, Observable<Course>> {
            override fun call(t: Student?): Observable<Course> {
                // 包装成Observable<R>对象返回。
                return Observable.from(t?.mList)
            }

        })
```

表面上看二者的都是把Observable<T> 转化为Observable<R>但内部Fun1函数需要泛型参数细节不同，实现细节也不同。

flatmap把一个Observable变成多个Observable，然后把得到的多个Obervable的元素一个个的发射出去。

因此flatMap常常用来做有依赖关系的事情，如上面拿到每个学生，然后再根据学生获取学生的每门课具体信息。工作中最常见的就是
接口的依赖如接口b需要接口a的结果：

```kotlin
      api.getUserInfo()
            .flatMap {
                // 1、首先请求Token
                getToken().map { _ ->
                    it
                }
            }.subscribe{
                //2、拿到Token接口返回的token信息
                
                // 通过token再请求user接口
            }
```


Rxjava 操作符有很多，引入两个吧，后续的再查阅学习~

# 线程调度

先来回顾下学习map操作符的栗子：

```java

    /**
     * Rxjava 操作符Map.
     * */
    private fun mapDemo() {
        val path = cacheDir.absolutePath + "/1.png"
        Observable.just(path)
            .map(object : Func1<String, Bitmap> {
                override fun call(t: String?): Bitmap {
                    //应当运行在子线程
                    return createBitmap(t)
                }

            }).subscribe(object : Action1<Bitmap> {
                override fun call(t: Bitmap) {
                  //应当运行在主线程
                    img.setImageBitmap(t)
                }
            })
    }

    private fun createBitmap(path: String?): Bitmap {
        return BitmapFactory.decodeFile(path)
    }
```

这段程序其实是有问题的，按照开发中的实际场景，bitmap的创建应该运行在子线程中，UI的更新应该运行在主线程中这时Rxjava的线程调度就可以发挥作用了~

在Rxjava中切换线程有两个方法

- public final Observable< T > subscribeOn(Scheduler scheduler):Observable在一个指定的调度器上创建。注意这个方法很特别，只作用于”被观察者创建阶段“，”只能指定一次“，”如果指定多次则以第一次为准“
- public final Observable<T> observeOn(Scheduler scheduler) ：指定在事件传递（加工变换）和最终被处理（观察者）的发生在哪一个调度器。可指定多次，每次指定完都在下一步生效。

上述栗子修改

```java
   /**
     * Rxjava 操作符Map.
     * */
    private fun mapDemoWithThreadSchedule() {
        val path = cacheDir.absolutePath + "/1.png"
        Observable.just(path)
            .subscribeOn(Schedulers.newThread())//Observable 在子线程中被创建
            .subscribeOn(Schedulers.io())//接下来代码运行在io线程中。
            .map(object : Func1<String, Bitmap> {
                override fun call(t: String?): Bitmap {
                logD(TAG){
                    "currentThread:${Thread.currentThread()}"
                }
                    return createBitmap(t)
                }
            })
            .observeOn(AndroidSchedulers.mainThread())//接下来代码运行在安卓主线程
            .subscribeOn(Schedulers.io())//指定无效，只能指定一次
            .subscribe(object : Action1<Bitmap> {
                override fun call(t: Bitmap) {
                    img.setImageBitmap(t)
                    logD(TAG){
                        "currentThread:${Thread.currentThread()}"
                    }
                }
            })
    }

    private fun createBitmap(path: String?): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

log:
D/MainActivity: currentThread:Thread[RxNewThreadScheduler-1,5,main]
D/MainActivity: currentThread:Thread[main,5,main]
```

可见subscribeOn就一个作用，指定”被观察者“创建的线程。

当事件”被发射“到”观察者观察到事件“这一阶段就需要使用observeOn来进行任务调度了。而且observeOn之后的代码运行在observeOn所指定的线程。

再来个栗子实战下

```java
    private fun threadSchedulers() {
        Observable.create(object : Observable.OnSubscribe<String> {
            override fun call(t: Subscriber<in String>?) {
                t?.let {
                   it.onNext("")
                }
                logD(TAG) {
                    "call#currentThread:${Thread.currentThread()}"
                }
            }

        }).subscribeOn(Schedulers.newThread())
            .subscribe(object : Subscriber<String>() {

            override fun onCompleted() {
                logD(TAG) {
                    "onCompleted#currentThread:${Thread.currentThread()}"
                }
            }

            override fun onError(e: Throwable?) {
                logD(TAG) {
                    "onError#currentThread:${Thread.currentThread()}"
                }
            }

            override fun onNext(t: String?) {
                logD(TAG) {
                    "onNext#currentThread:${Thread.currentThread()}"
                }
            }
        })
    }
//log:
D/MainActivity: onNext#currentThread:Thread[RxNewThreadScheduler-1,5,main]
D/MainActivity: call#currentThread:Thread[RxNewThreadScheduler-1,5,main]
```

可见只指定subscribeOn时，观察者、被观察者运行在相同的线程。

在进行测试

```java
    private fun threadSchedulers() {
        Observable.create(object : Observable.OnSubscribe<String> {
            override fun call(t: Subscriber<in String>?) {
                t?.let {
                    it.onNext("")
                }
                logD(TAG) {
                    "call#currentThread:${Thread.currentThread()}"
                }
            }

        }).subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Subscriber<String>() {

                override fun onCompleted() {
                    logD(TAG) {
                        "onCompleted#currentThread:${Thread.currentThread()}"
                    }
                }

                override fun onError(e: Throwable?) {
                    logD(TAG) {
                        "onError#currentThread:${Thread.currentThread()}"
                    }
                }

                override fun onNext(t: String?) {
                    logD(TAG) {
                        "onNext#currentThread:${Thread.currentThread()}"
                    }
                }
            })
    }
//log
D/MainActivity: call#currentThread:Thread[RxNewThreadScheduler-1,5,main]
D/MainActivity: onNext#currentThread:Thread[main,5,main]
```



好了已经差不多了，前面看到有不同的调度类型，这里就再总结下这个~



| RxJava调度器类型                      | 效果                                       |
| -------------------------------- | ---------------------------------------- |
| Scheduler.computation            | 用于计算任务，如事件循环或和回调处理，不要用于IO操作默认线程数等于cpu个数。 |
| Scheduler.from(Executor executor | 指定一个executer作为调度器。                       |
| Scheduler.immediate()            | 在当前线程立即开始执行任务。                           |
| Scheduler.io()                   | 适用于IO密集型任务，这个调度器的线程池会根据需要增长，默认是CacheThreadScheduler。 |
| Scheduler.newThread()            | 为每个任务创建一个新线程。                            |
| Scheduler.trampoline()           | 当其排队的任务完成后，在当前线程排队开始执行。                  |



| RxAndroid调度器类型                        | 效果                  |
| ------------------------------------- | ------------------- |
| AndroidSchedulers.mainThread()        | 当前任务运行在安卓主线程。       |
| AndroidSchedulers.from(Looper looper) | 当前任务运行在指定的looper线程。 |

# 总结

总体来说过了一遍，不过操作符和Rxjava2+等还需要后续再了解了~

# 参考

[RxJava 操作符flatmap](https://blog.csdn.net/jdsjlzx/article/details/51493552)

[关于RxJava最友好的文章](https://mp.weixin.qq.com/s/6-0jFN_BwKOlwOK3kha90g)

[RxJava](https://github.com/ReactiveX/RxJava)

[RxAndroid](https://github.com/ReactiveX/RxAndroid)

[Carson带你学Android：这是一份全面 & 详细的RxJava学习指南](https://www.jianshu.com/p/d9b504f5b3bd)





