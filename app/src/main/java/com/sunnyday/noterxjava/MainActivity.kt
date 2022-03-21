package com.sunnyday.noterxjava

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sunnyday.noterxjava.beans.Course
import com.sunnyday.noterxjava.beans.Student
import kotlinx.android.synthetic.main.activity_main.*
import rx.Observable
import rx.Subscriber
import rx.functions.Action0
import rx.functions.Action1
import rx.functions.Func1


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // basicUsage()
        // knownUsage()
        // createObservableQuicklyByJust()
        // createObservableQuicklyByFrom()
        // action1Usage()
        // actionUsage()
        // mapDemo()
       // printStudentName()
        printStudentCourseByFlatMap()
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
     * 使用just快速创建Observable对象，并触发事件。
     * */
    private fun createObservableQuicklyByJust() {
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

    private fun action1Usage() {
        Observable.just("hello", "Rxjava")
            .subscribe(object : Action1<String> {
                // 处理onNext事件
                override fun call(t: String?) {
                    logD(TAG) {
                        "action1Usage#call:$t"
                    }
                }
            })
    }

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

    /**
     * Rxjava 操作符Map.
     * */
    private fun mapDemo() {
        val path = cacheDir.absolutePath + "/1.png"
        Observable.just(path)
            .map(object : Func1<String, Bitmap> {
                override fun call(t: String?): Bitmap {
                    return createBitmap(t)
                }

            }).subscribe(object : Action1<Bitmap> {
                override fun call(t: Bitmap) {
                    img.setImageBitmap(t)
                }
            })
    }

    private fun createBitmap(path: String?): Bitmap {
        return BitmapFactory.decodeFile(path)
    }

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

    private fun printStudentName() {
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


    /**
     * log封装，方便使用。
     * */
    private fun logD(tag: String, msg: () -> String) {
        Log.d(tag, msg.invoke())
    }
}