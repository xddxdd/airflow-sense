package lantian.airflowsense.FileManager;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lantian.airflowsense.Common;

public class FileManager {

    private static ArrayList<Double> tempDataArray;
    private static final String fileType = "CSV";
    private static final String fileWrapper = "%s/%s.%s";

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

    public static boolean saveData(String user_name, String file_name){
        if (tempDataArray == null)
            return false;

        getDirectory(user_name); // Make sure the directory exists

        try {
            File file = new File(getFilePath(user_name, file_name));
            for (int i = 1; i < Integer.MAX_VALUE && file.exists(); i++){
                file = new File(getFilePath(user_name, file_name + "(" + i + ")"));
            }

            if (!file.createNewFile()){
                Log.w("saveData", "fail");
                return false;
            }

            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(tempDataArray.toString().getBytes());
            outputStream.close();

        }catch (Exception e){
            Log.w("saveData", "fail");
            return false;
        }
        tempDataArray = null;
        return true;
    }

    public static void dumpData(){
        tempDataArray = null;
    }

    public static File getFile(String user_name, String file_name){

        try {
            File file = new File(getFilePath(user_name, file_name));
            if (!file.exists())
                return null;
            return file;
        }catch (Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static boolean removeFile(String user_name, String file_name){
        try {
            File file = new File(getFilePath(user_name, file_name));
            if (!file.exists())
                return false;

            return file.delete();

        }catch (Exception e){
            Log.w("removeFile", "fail");
            return false;
        }
    }

    public static boolean renameFile(String user_name, String file_name, String new_name){
        if (file_name == null || new_name == null || user_name == null) return false;
        if (file_name.equals(new_name)) return true;
        try {
            File old_file = new File(getFilePath(user_name, file_name));
            File new_file = new File(getFilePath(user_name, new_name));
            if (!old_file.exists() || new_file.exists()) return false;
            return old_file.renameTo(new_file);
        }catch (Exception e){
            Log.w("renameFile", "fail");
            return false;
        }
    }

    public static String getDefaultFileName(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss:SS", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        return sdf.format(date);
    }

    public static File getDirectory(String user_name){
        try {
            File directory = new File(getDirectoryPath(user_name));
            if (!directory.exists()){
                if (!directory.mkdir()) {
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
        return (user_name.isEmpty()) ? Common.Norms.DEFAULT_USER_NAME : user_name;
    }

    private static String getFilePath(String user_name, String file_name){
        return String.format(fileWrapper, getDirectoryPath(user_name), file_name, fileType);
    }

}
