# GifView
Android安卓gif动图播放,支持8种缩放模式,速度调节,播放停止等

## 使用
```
<com.song.GifView
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:id="@+id/gif_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:gif="@drawable/panda"
        app:scaleType="fitCenter" />
```
## API
```
public void setGifResource(int resourceId);//来自资源
public void setGifPath(String resourcePath);//来自文件
public void setScaleType(int scaleType);//缩放模式
public void setSpeed(float speed);//播放速度
public void setFPS(int fps);//刷新率 影响cpu占用
public void play();//
public void rePlay();//
public void pause();//
public void stop();//
public boolean isPlaying();//
```

[ **DEMO APK** ](https://raw.githubusercontent.com/tohodog/GifView/master/gifdemo.apk)
<br>
![](http://raw.githubusercontent.com/tohodog/GifView/master/demo.jpg)
