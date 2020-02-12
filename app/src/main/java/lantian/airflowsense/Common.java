package lantian.airflowsense;

import android.Manifest;

import lantian.airflowsense.BuildConfig;

public class Common {

    public class Norms {
        public static final String DEFAULT_USER_NAME = "default";
    }

    public class Action {
        public static final String BROADCAST_DATA_UPDATE = BuildConfig.APPLICATION_ID + ".broadcast.data_update";
        public static final String BROADCAST_CONNECTION_STATUS_UPDATE = BuildConfig.APPLICATION_ID + ".broadcast.connection_status_update";
        public static final String FLOAT_WINDOW_STATUS_UPDATE = "float_window_status_update";
    }

    public class PacketParams {
        public static final String USER_NAME = "user_name";
        public static final String PASSWORD = "password";
        public static final String ERRORCODE = "error";
        public static final String INSTRUCTION = "instruction";
        public static final String OPERATION = "operation";

        public static final String CONNECTIVITY = "connectivity";
        public static final String NEW_VALUE = "new_value";
        public static final String FLOAT_WINDOW_SHOW = "float_window_show";
    }

    public class ErrorCode {
        public static final int NON_EXIST_USER = 0;
        public static final int NAME_OCCUPIED = 1;
        public static final int WRONG_PASSWORD = 2;
    }

    public class Instruction {
        public static final int APPROVED = 0;
        public static final int REJECTED = 1;
    }

    public class Operation {
        public static final int LOGIN = 0;
        public static final int REGISTER = 1;
    }

    public class RequestCode {
        public static final int REQ_LOGIN = 0;
        public static final int REQ_REGISTER = 1;
        public static final int REQ_OVERLAY_PERMISSION = 2;
        public static final int REQUEST_READ_WRITE_PERMISSION_CODE = 1; // R&W Request State Code
    }

    // Read & Write permission
    public static final String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
}
