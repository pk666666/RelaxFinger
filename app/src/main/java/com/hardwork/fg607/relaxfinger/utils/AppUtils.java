package com.hardwork.fg607.relaxfinger.utils;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.hardwork.fg607.relaxfinger.MyApplication;
import com.hardwork.fg607.relaxfinger.model.AppInfo;
import com.hardwork.fg607.relaxfinger.model.ShortcutInfo;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by fg607 on 15-11-26.
 */
public class AppUtils {

    public static Context context = MyApplication.getApplication();
    public static PackageManager pm = context.getPackageManager();

    public static  ContentResolver cr = null;
    public static  Uri shortcutUri = null;
    public static List<ResolveInfo> resolveInfos = null;

    public static ArrayList<AppInfo> getAppInfos(){

        ArrayList<AppInfo> list = new ArrayList<>();

        Drawable icon;
        String name;
        String packageName;

        List<PackageInfo> packageInfos = pm.getInstalledPackages(0);

        for(PackageInfo info:packageInfos){

            //判断是否为用户应用
            if((ApplicationInfo.FLAG_SYSTEM & info.applicationInfo.flags) == 0){

                icon = info.applicationInfo.loadIcon(pm);

               if(icon != null){

                   name = info.applicationInfo.loadLabel(pm).toString();
                   packageName = info.packageName;

                   AppInfo appInfo = new AppInfo(icon,name,"",packageName);

                   list.add(appInfo);
               }

            }


        }

        return list;
    }

    public static ArrayList<AppInfo> getLauncherAppInfos(){

        ArrayList<AppInfo> list = new ArrayList<>();

        Drawable icon;
        String name;
        String packageName;
        String activityName;

        Intent intent = new Intent();
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);


        List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);

        for(ResolveInfo info:resolveInfoList){

            icon = info.loadIcon(pm);
            name = info.loadLabel(pm).toString();
            packageName  = info.activityInfo.packageName;
            activityName = info.activityInfo.name;


            AppInfo appInfo = new AppInfo(icon,name,activityName,packageName);

            list.add(appInfo);
        }

        return list;

    }

    public static String getAppName(String packageName){

        if(TextUtils.isEmpty(packageName)){

            return  null;
        }

        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(packageInfo!= null){

            return packageInfo.applicationInfo.loadLabel(pm).toString();
        }else {

            return null;
        }

    }

    public static Drawable getAppIcon(String packageName){

        if(TextUtils.isEmpty(packageName)){

            return  null;
        }

        PackageInfo packageInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(packageInfo!= null){

            return packageInfo.applicationInfo.loadIcon(pm);
        }else {

            return null;
        }

    }

    public static boolean startApplication(String packageName) throws ActivityNotFoundException{

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {

            String className = intent.getComponent().getClassName();

            //解决直接用pm.getLaunchIntentForPackage(packageName)会重新打开APP的问题
            Intent intent1 = new Intent(Intent.ACTION_MAIN);
            intent1.addCategory(Intent.CATEGORY_LAUNCHER);
            intent1.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent1.setComponent(new ComponentName(packageName, className));

            context.startActivity(intent1);

            return true;

        }else {

            throw new  ActivityNotFoundException();
        }

    }

    public static boolean startApplication(String packageName,String className) throws ActivityNotFoundException{


        if (className != null) {

            //解决直接用pm.getLaunchIntentForPackage(packageName)会重新打开APP的问题
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(packageName, className));

            context.startActivity(intent);

            return true;

        }else {

            throw new  ActivityNotFoundException();
        }

    }

    public static boolean startApplication(Context activity,String packageName) throws ActivityNotFoundException{

        PackageManager pm = activity.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {

            activity.startActivity(intent);

            return true;

        }else {

            throw new  ActivityNotFoundException();
        }

    }

    public static void startActivity(String intentUri) throws URISyntaxException,ActivityNotFoundException {

        Intent intent = Intent.parseUri(intentUri,Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if(intent!= null){

            context.startActivity(intent);
        }else {

            throw new  ActivityNotFoundException();
        }

    }

    public static void startActivity(Context activity,String intentUri) throws URISyntaxException,ActivityNotFoundException {

        Intent intent = Intent.parseUri(intentUri,Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        if(intent!= null){

            activity.startActivity(intent);
        }else {

            throw new  ActivityNotFoundException();
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static  void startActivity(Class<?> cls) {

        Intent intent = new Intent(context, cls);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long now = Calendar.getInstance().getTimeInMillis();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, now, pendingIntent);
        else
            alarmManager.set(AlarmManager.RTC_WAKEUP, now, pendingIntent);
    }

    public static void uninstallApplication(String packageName){

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);

    }

    public static void showAppDetailActivity(String packageName){

        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + packageName));
        context.startActivity(intent);


    }
    public static String getFilePath(String packageName){

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageInfo = pm.getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }

        if(packageInfo != null){
            applicationInfo =  packageInfo.applicationInfo;
            return applicationInfo.sourceDir;
        }
        return null;
    }


    /**
     * 根据进程名获取应用信息
     * @param processNames
     * @return
     */
    public static ApplicationInfo getApplicationInfoByProcessName(String processNames)
    {

        //获取所有包信息
        //List<ApplicationInfo> applicationInfoList
        // = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

        List<ApplicationInfo> applicationInfoList = pm.getInstalledApplications(0);

        for(ApplicationInfo applicationInfo : applicationInfoList)
        {
            if(applicationInfo.processName.equals(processNames)
                    && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
                //只显示第三方的应用进程,不显示系统应用
                //要显示所有应用进程,删去(applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0 即可
                return applicationInfo;
        }
        return null;
    }

    public static String getLauncherPackageName() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = context.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo == null) {
            // should not happen. A home is always installed, isn't it?
            return null;
        }
        if (res.activityInfo.packageName.equals("android")) {
            // 有多个桌面程序存在，且未指定默认项时；
            return null;
        } else {
            return res.activityInfo.packageName;
        }
    }

    /**
     * 获取程序包名(本程序包名5.0版本上下都可获取)
     *
     * @return
     */
    public static ActivityManager.RunningAppProcessInfo getCurrentAppInfo() {
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        int START_TASK_TO_FRONT = 2;
        ApplicationInfo currentApp = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (Exception e) {
                e.printStackTrace();
            }
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo app : appList) {
                if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    Integer state = null;
                    try {
                        state = field.getInt(app);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (state != null && state == START_TASK_TO_FRONT) {
                        currentInfo = app;
                        break;
                    }
                }
            }
            if (currentInfo != null) {
                currentApp = getApplicationInfoByProcessName(currentInfo.processName);
            }
        } else {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = am.getRunningAppProcesses();
            currentApp = getApplicationInfoByProcessName(tasks.get(0).processName);
            currentInfo=tasks.get(0);
        }
        Log.i("TAG", "Current App in foreground is: " + currentApp);
        return currentInfo;
    }

    public static int getVersionCode(Context context)//获取版本号(内部识别号)
    {
        try {

            PackageInfo pi= null;

            pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            return pi.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return 0;
        }
    }

    public static ArrayList<ShortcutInfo> getShortcutsA() throws SecurityException,SQLiteException {


        ArrayList<ShortcutInfo> list = new ArrayList<>();


        if(cr == null){

            cr = context.getContentResolver();
        }

        if(shortcutUri == null){

            shortcutUri = ShortcutSuperUtils.getUriFromLauncher(context);
        }


        try{

            Cursor c = cr.query(shortcutUri, new String[] {"icon", "title", "intent"},
                    null, null, null);

            if (c != null && c.getCount() > 0) {

                String intent = null;
                String title;
                byte[] icon;

                while(c.moveToNext()){
                    icon = c.getBlob(0);
                    title = c.getString(1);
                    intent = c.getString(2);

                    if(icon !=null && intent!=null){

                        ShortcutInfo shortcutInfo = new ShortcutInfo(ImageUtils.Bytes2Drawable(icon),
                                title,intent);

                        list.add(shortcutInfo);

                    }

                }

            }

        }catch (Exception e){

            e.printStackTrace();
        }



        return list;

    }

    public static List<ResolveInfo> getResolveInfos(){

        Intent shortcutsIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(
                shortcutsIntent, 0);

        return resolveInfos;
    }

    public static ArrayList<ShortcutInfo> getShortcutsB(){

        ArrayList<ShortcutInfo> list = new ArrayList<>();

        resolveInfos = getResolveInfos();

        if(resolveInfos != null){

            for(ResolveInfo resolveInfo:resolveInfos){

                ShortcutInfo shortcutInfo = new ShortcutInfo(resolveInfo.loadIcon(pm),
                        (String)resolveInfo.loadLabel(pm),getIntentUri(resolveInfo));

                list.add(shortcutInfo);
            }
        }

        return list;
    }


    public static ArrayList<ShortcutInfo> getShortcuts(){

        ArrayList<ShortcutInfo> list = getShortcutsA();

        list.addAll(getShortcutsB());

        return list;
    }

    public static String getIntentUri(ResolveInfo resolveInfo){


        Intent intent = new Intent();

        String pkgName = resolveInfo.activityInfo.packageName;
        String clsName = resolveInfo.activityInfo.name;

        intent.setComponent(new ComponentName(pkgName,clsName));

        return intent.toUri(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static String getIntentUri(AppWidgetProviderInfo widgetInfo){


        Intent intent = new Intent();

        intent.setComponent(widgetInfo.configure);

        return intent.toUri(Intent.FLAG_ACTIVITY_NEW_TASK);
    }


    public static Drawable getShortcutIcon(String title){

        Drawable drawable = null;

        if(cr == null){

            cr = context.getContentResolver();
        }

        if(shortcutUri == null){

            shortcutUri = ShortcutSuperUtils.getUriFromLauncher(context);
        }

        try {
            Cursor c = cr.query(shortcutUri, new String[] {"icon"},
                    "title=?", new String[]{title}, null);

            if (c != null && c.getCount() > 0) {

                c.moveToFirst();

                byte[] icon = c.getBlob(0);

                if(icon != null && icon.length>0){

                    drawable = ImageUtils.Bytes2Drawable(icon);

                }


            }
        }catch (Exception e){

            e.printStackTrace();
        }



        if(drawable == null){

            return getShortcutIconA(title);
        }

        return  drawable;
    }

    public static Drawable getShortcutIconA(String title){

        Drawable drawable = null;

        if(resolveInfos == null){

            resolveInfos = getResolveInfos();
        }

        if(resolveInfos != null){

            for (ResolveInfo info:resolveInfos){

                if(title.equals((String)info.loadLabel(pm))){

                    drawable = info.loadIcon(pm);
                }
            }
        }

        return drawable;
    }

}