package lantian.airflowsense.FileManager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;

import lantian.airflowsense.Common;

public class FileManager {

    private static ArrayList<Double> tempDataArray;
    public static final String fileType = ".CSV";
    private static final String fileWrapper = "%s" + File.separator + "%s%s";
    private static String rootDir;

    public static void init(Activity activity){
        File root = activity.getExternalFilesDir(null);
        if (root != null){
            rootDir = root.getPath();
        }
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity, Common.PERMISSIONS_STORAGE, Common.RequestCode.REQUEST_READ_WRITE_PERMISSION_CODE);
        }
    }

    public static synchronized void addTempData(double new_value){
        if (tempDataArray == null)
            return;
        tempDataArray.add(new_value);
    }

    public static void createTempFile(){
        /* Clear the array */
        tempDataArray = null;
        /* Give it a new Object */
        tempDataArray = new ArrayList<>();
    }

    public static String saveData(String user_name, String file_name){
        if (tempDataArray == null)
            return null;

        /* Make sure the directory exists */
        if (getDirectory(user_name) == null)
            return null;

        String postfix = generatePostfix(0);

        try {
            File file = new File(getFilePath(user_name, file_name + postfix));
            for (int i = 1; i < Integer.MAX_VALUE && file.exists(); i++){
                postfix = generatePostfix(i);
                file = new File(getFilePath(user_name, file_name + postfix));
            }

            if (!file.createNewFile()){
                Log.w("saveData", "fail");
                return null;
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(tempDataArray.toString().getBytes());
            outputStream.close();

        }catch (Exception e){
            Log.w("saveData", "fail");
            return null;
        }
        tempDataArray = null;
        return postfix;
    }

    public static void dumpData(){
        tempDataArray = null;
    }

    public static File getFile(String user_name, String file_name, String postfix){
        if (user_name == null || file_name == null || postfix == null)
            return null;

        String full_file_name = file_name + postfix;
        try {
            File file = new File(getFilePath(user_name, full_file_name));
            if (!file.exists())
                return null;
            return file;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static boolean removeFile(String user_name, String file_name, String postfix){
        if (user_name == null || file_name == null || postfix == null)
            return false;

        String full_file_name = file_name + postfix;
        try {
            File file = new File(getFilePath(user_name, full_file_name));
            if (!file.exists())
                return false;

            return file.delete();

        }catch (Exception e){
            Log.w("removeFile", "fail");
            return false;
        }
    }

    public static boolean renameFile(String user_name, String file_name, String postfix, String new_name){
        if (file_name == null || new_name == null || postfix == null || user_name == null)
            return false;

        if (file_name.equals(new_name))
            return true;

        try {
            File old_file = new File(getFilePath(user_name, file_name + postfix));
            File new_file = new File(getFilePath(user_name, new_name + postfix));
            if (!old_file.exists() || new_file.exists()) return false;
            return old_file.renameTo(new_file);
        }catch (Exception e){
            Log.w("renameFile", "fail");
            return false;
        }
    }

    private static String generatePostfix(int offset){
        Date date = new Date(System.currentTimeMillis());
        long time = date.getTime() + offset;
        return "_" + time;
    }

    public static File getDirectory(String user_name){
        if (user_name == null) return null;

        try {
            File directory = new File(getDirectoryPath(user_name));
            if (!directory.exists()){
                if (!directory.mkdir()){
                    return null;
                }
            }
            return directory;
        }catch (Exception ex){
            Log.w("getDirectory", "fail to create directory");
            return null;
        }
    }

    private static String getDirectoryPath(String user_name){
        return rootDir + File.separator + ((user_name.isEmpty()) ? Common.Norms.DEFAULT_USER_NAME : user_name);
    }

    private static String getFilePath(String user_name, String file_name){
        return String.format(fileWrapper, getDirectoryPath(user_name), file_name, fileType);
    }

}
