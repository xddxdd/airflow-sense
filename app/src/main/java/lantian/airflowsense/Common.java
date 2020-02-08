package lantian.airflowsense;

import lantian.airflowsense.BuildConfig;

public class Common {
    public static String BROADCAST_DATA_UPDATE = BuildConfig.APPLICATION_ID + ".broadcast.data_update";
    public static String BROADCAST_CONNECTION_STATUS_UPDATE = BuildConfig.APPLICATION_ID + ".broadcast.connection_status_update";

    public class PacketParams {
        public static final String USER_NAME = "user_name";
        public static final String PASSWORD = "password";
        public static final String ERRORCODE = "error";
        public static final String INSTRUCTION = "instruction";
        public static final String OPERATION = "operation";
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
    }
}
