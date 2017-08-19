package com.study.android.jct.ImageLoader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import com.study.android.jct.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by 10764 on 2017/8/18.
 */

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    public static final int MESSAGE_POST_RESULT = 1;

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final long KEEP_ALIVE = 10L;

    private static final int TAG_KEY_URI = R.id.imageloader_uri;
    private static final long DISK_CACHE_SIZE = 1024*1024*50;
    private static final int IO_BUFFER_SIZE = 8*1024;
    private static final int DISK_CACHE_INDEX = 0;
    private boolean mIsDiskLruCacheCreated = false;


    //线程工厂
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        //AtomicInteger是一种线程安全的加减操作接口。
        private final AtomicInteger mCount = new AtomicInteger(1);
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r,"ImageLoader#" + mCount.getAndIncrement());
        }
    };

    //线程池参数：
    //corePoolSize 核心线程池大小
    //maximumPoolSize 线程池最大容量大小
    //keepAliveTime 线程池空闲时，线程存活的时间
    //TimeUnit 时间单位
    //ThreadFactory 线程工厂
    //BlockingQueue任务队列
    public static final Executor THREAD_POOL_EXECUTOR = new ThreadPoolExecutor(CORE_POOL_SIZE,MAXIMUM_POOL_SIZE,KEEP_ALIVE, TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>(),sThreadFactory);

    private Handler mMainHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult result = (LoaderResult) msg.obj;
            ImageView imageView = result.imageView;
            imageView.setImageBitmap(result.bitmap);
            String uri = (String) imageView.getTag(TAG_KEY_URI);
            if (uri.equals(result.uri)){
                imageView.setImageBitmap(result.bitmap);
            }else {
                Log.w(TAG, "set image bitmap,but url has changed, ignored!");
            }
        }
    };

    private Context mContext;
    private ImageResizer mImageResizer = new ImageResizer();
    private LruCache<String,Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;

    private ImageLoader(Context context){
        mContext = context.getApplicationContext();
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
            }
        };
        File diskCacheDir = getDiskCacheDir(mContext,"bitmap");
        if (!diskCacheDir.exists()){
            diskCacheDir.mkdirs();
        }
        if (getUsableSpace(diskCacheDir) > DISK_CACHE_SIZE){
            try {
                mDiskLruCache = DiskLruCache.open(diskCacheDir,1,1,DISK_CACHE_SIZE);
                mIsDiskLruCacheCreated = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * build a new instance of ImageLoader
     * @param context
     * @return a new instance of ImageLoader
     */
    public static ImageLoader builder(Context context){
        return new ImageLoader(context);
    }

    private void addBitmapToMemoryCache(String key,Bitmap bitmap){
        if (getBitmapFromMemCache(key) == null){
            mMemoryCache.put(key,bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * load bitmap from memory cache or disk cache or network async,then bind imageView and bitmap
     * @param url
     * @param imageView bitmap's bind object
     */
    public void bindBitmap(final String url,final ImageView imageView){
        bindBitmap(url,imageView,0,0);
    }

    public void bindBitmap(final String url, final ImageView imageView, final int reqWidth, final int reqHeight) {
        imageView.setTag(TAG_KEY_URI,url);
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
            return;
        }
        Runnable loadBitmapTask = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = loadBitmap(url,reqWidth,reqHeight);
                if (bitmap != null){
                    LoaderResult result = new LoaderResult(imageView,url,bitmap);
                    mMainHandler.obtainMessage(MESSAGE_POST_RESULT,result).sendToTarget();
                }
            }
        };
        THREAD_POOL_EXECUTOR.execute(loadBitmapTask);
    }

    /**
     * load bitmap from memory cache or disk cache or network
     * @param url  http url
     * @param reqWidth  the width imageView desired
     * @param reqHeight  the height imageView desired
     * @return bitmap , maybe null
     */
    public Bitmap loadBitmap(String url, int reqWidth, int reqHeight) {
        Bitmap bitmap = loadBitmapFromMemCache(url);
        if (bitmap != null){
            Log.d(TAG, "loadBitmapFromMemCache,url:" + url);
            return bitmap;
        }
        try {
            bitmap = loadBitmapFromDiskCache(url,reqWidth,reqHeight);
            if (bitmap != null){
                Log.d(TAG, "loadBitmapFromDisk,url:" + url);
                return bitmap;
            }
            bitmap = loadBitmapFromHttp(url,reqWidth,reqHeight);
            Log.d(TAG, "loadBitmapFromHttp,url:"+url);
        }catch (IOException e){
            e.printStackTrace();
        }

        if (bitmap == null && !mIsDiskLruCacheCreated){
            Log.w(TAG, "encounter error, DiskLruCache is not created.");
            bitmap = downloadBitmapFromUrl(url);
        }
        return bitmap;
    }

    private Bitmap downloadBitmapFromUrl(String urlString) {
        Bitmap bitmap = null;
        HttpURLConnection urlConnection = null;
        BufferedInputStream in = null;

        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            bitmap = BitmapFactory.decodeStream(in);
        }  catch (IOException e) {
            Log.e(TAG, "Error in downloadBitmap: " + e);
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            try {
                if (in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    private Bitmap loadBitmapFromHttp(String url, int reqWidth, int reqHeight) throws IOException{
        if (Looper.myLooper() == Looper.getMainLooper()){
            throw new RuntimeException("can not visit network from UI Thread.");
        }
        if (mDiskLruCache == null){
            return null;
        }

        String key = hashKeyFromUrl(url);
        DiskLruCache.Editor editor = mDiskLruCache.edit(key);
        if (editor != null){
            OutputStream outputStream = editor.newOutputStream(DISK_CACHE_INDEX);
            if (downloadUrlToStram(url,outputStream)){
                editor.commit();
            }else {
                editor.abort();
            }
            mDiskLruCache.flush();
        }
        return loadBitmapFromDiskCache(url,reqWidth,reqHeight);
    }

    private Bitmap loadBitmapFromDiskCache(String url, int reqWidth, int reqHeight) throws IOException{
        if (Looper.myLooper() == Looper.getMainLooper()){
            Log.w(TAG, "load bitmap from UI Thread, it's not recommended!" );
        }
        if (mDiskLruCache == null){
            return null;
        }
        Bitmap bitmap = null;
        String key = hashKeyFromUrl(url);
        DiskLruCache.Snapshot snapshot = mDiskLruCache.get(key);
        if (snapshot != null){
            FileInputStream fileInputStream = (FileInputStream) snapshot.getInputStream(DISK_CACHE_INDEX);
            FileDescriptor fileDescriptor = fileInputStream.getFD();
            bitmap = mImageResizer.decodeSampledBitmapFromFileDescriptor(fileDescriptor,reqWidth,reqHeight);
            if (bitmap != null){
                addBitmapToMemoryCache(key,bitmap);
            }
        }
        return bitmap;
    }



    private boolean downloadUrlToStram(String urlString, OutputStream outputStream) {
        HttpURLConnection urlConnection = null;
        BufferedOutputStream out = null;
        BufferedInputStream in = null;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream(),IO_BUFFER_SIZE);
            out = new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);

            int b;
            while ((b = in.read()) != -1){
                out.write(b);
            }
            return true;
        }  catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (urlConnection != null){
                urlConnection.disconnect();
            }
            try {
                if (out != null){
                    out.close();
                }
                if (in != null){
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    private Bitmap loadBitmapFromMemCache(String url) {
        final String key = hashKeyFromUrl(url);
        Bitmap bitmap = getBitmapFromMemCache(key);
        return bitmap;
    }

    private String hashKeyFromUrl(String url) {
        String cacheKey;
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            cacheKey = bytesToHexString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            cacheKey = String.valueOf(url.hashCode());
        }
        return cacheKey;
    }

    private String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0;i < bytes.length ; i++){
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1){
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public File getDiskCacheDir(Context mContext, String uniqueName) {
        boolean externalStorageAvaliable = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorageAvaliable){
            cachePath = mContext.getExternalCacheDir().getPath();
        }else {
            cachePath = mContext.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    private long getUsableSpace(File path){
        final StatFs statFs = new StatFs(path.getPath());
        return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
    }


    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView,String uri,Bitmap bitmap){
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }
}
