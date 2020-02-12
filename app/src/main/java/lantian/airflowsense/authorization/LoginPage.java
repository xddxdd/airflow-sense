package lantian.airflowsense.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import lantian.airflowsense.Common;
import lantian.airflowsense.R;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {

    EditText name;
    EditText password;
    JSONObject send_msg = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Bind the login_page.xml layout

        (findViewById(R.id.login_page_login_button)).setOnClickListener(LoginPage.this);
        (findViewById(R.id.login_page_register_button)).setOnClickListener(LoginPage.this);
        name = findViewById(R.id.login_page_user_name);
        password = findViewById(R.id.login_page_password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_page_login_button:
                String name_txt = name.getText().toString();
                String password_txt = password.getText().toString();

                try {
                    send_msg.put(Common.PacketParams.USER_NAME, name_txt);
                    send_msg.put(Common.PacketParams.PASSWORD, password_txt);
                    send_msg.put(Common.PacketParams.OPERATION, Common.Operation.LOGIN);
                    send_msg.put(Common.PacketParams.INSTRUCTION, 0);
                    send_msg.put(Common.PacketParams.ERRORCODE, 0);
                }catch (JSONException e) {
                    Toast.makeText(LoginPage.this, "JSON exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent();
                try {
                    intent.putExtra(Common.PacketParams.USER_NAME, send_msg.getString(Common.PacketParams.USER_NAME));
                }catch (JSONException e){
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                setResult(RESULT_OK, intent);
                finish();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Socket socket = new Socket("47.97.98.49", 2862);
//
//                            try {
//                                OutputStream os = socket.getOutputStream();
//                                byte[] send_byte = send_msg.toString().getBytes();
//                                int msg_length = send_byte.length;
//                                os.write(send_byte);
//                                socket.shutdownOutput();
//
//                                InputStream is = socket.getInputStream();
//                                byte[] reply_byte = new byte[msg_length + 128];
//                                if (is.read(reply_byte) != -1) {
//                                    throw new Exception("Unexpectedly large server callback data");
//                                }
//                                socket.shutdownInput();
//
//                                try{
//                                    JSONObject reply_msg = new JSONObject(new String(reply_byte));
//                                    int instruction = reply_msg.getInt(Common.PacketParams.INSTRUCTION);
//                                    if (instruction == Common.Instruction.APPROVED) {
//                                        Intent intent = new Intent();
//                                        intent.putExtra(Common.PacketParams.USER_NAME, reply_msg.getString(Common.PacketParams.USER_NAME));
//                                        setResult(RESULT_OK, intent);
//                                        finish();
//                                    } else {
//                                        final int errorCode = reply_msg.getInt(Common.PacketParams.ERRORCODE);
//                                        runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                if (errorCode == Common.ErrorCode.WRONG_PASSWORD) {
//                                                    Toast.makeText(LoginPage.this, "密码错误", Toast.LENGTH_SHORT).show();
//                                                } else if (errorCode == Common.ErrorCode.NON_EXIST_USER) {
//                                                    Toast.makeText(LoginPage.this, "用户不存在", Toast.LENGTH_SHORT).show();
//                                                } else {
//                                                    Toast.makeText(LoginPage.this, "服务器端错误", Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        });
//                                    }
//                                }catch (JSONException e){
//                                    System.out.println(e.getMessage());
//                                }
//
//                            } catch (Exception e) {
//                                System.out.println(e.getMessage());
//                            }
//
//                            socket.close();
//                        }catch (IOException e){
//                            System.out.println(e.getMessage());
//                        }
//                    }
//                }).start();
                break;
            case R.id.login_page_register_button:
                startActivityForResult(new Intent(LoginPage.this, RegisterPage.class), Common.RequestCode.REQ_REGISTER);
                break;
            case R.id.login_page_user_name:
                break;
            default:
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.RequestCode.REQ_REGISTER){
            if (resultCode == RESULT_OK){
                Intent intent = new Intent();
                intent.putExtra(Common.PacketParams.USER_NAME, data.getStringExtra(Common.PacketParams.USER_NAME));
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}
