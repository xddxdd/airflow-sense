package lantian.airflowsense.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import lantian.airflowsense.Common;
import lantian.airflowsense.R;

public class RegisterPage extends AppCompatActivity {

    EditText name;
    EditText password;
    EditText re_password;
    JSONObject send_msg = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page); // Bind the login_page.xml layout

        name = findViewById(R.id.register_page_user_name);
        password = findViewById(R.id.register_page_password);
        re_password = findViewById(R.id.register_page_re_password);

        (findViewById(R.id.register_page_register_button)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String name_txt = name.getText().toString();
                String password_txt = password.getText().toString();
                if (name_txt.isEmpty()){
                    Toast.makeText(RegisterPage.this, "姓名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password_txt.isEmpty()) {
                    Toast.makeText(RegisterPage.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password_txt.equals(re_password.getText().toString())) {
                    Toast.makeText(RegisterPage.this, "两次密码不同", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    send_msg.put("name", name_txt);
                    send_msg.put("password", password_txt);
                    send_msg.put("operation", Common.Operation.REGISTER);
                    send_msg.put("instruction", 0);
                    send_msg.put("error", 0);
                }catch (JSONException e){
                    Toast.makeText(RegisterPage.this, "JSON exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("47.97.98.49",2862);

                            try {
                                OutputStream os = socket.getOutputStream();
                                byte[] send_byte = send_msg.toString().getBytes();
                                int msg_length = send_byte.length;
                                os.write(send_byte);
                                socket.shutdownOutput();

                                InputStream is = socket.getInputStream();
                                byte[] reply_byte = new byte[msg_length + 128];
                                if (is.read(reply_byte) != -1) {
                                    throw new Exception("Unexpectedly large server callback data");
                                }
                                socket.shutdownInput();

                                try {
                                    JSONObject reply_msg = new JSONObject(is.toString());
                                    int instruction = reply_msg.getInt("instruction");
                                    if (instruction == Common.Instruction.APPROVED){
                                        Intent intent = new Intent();
                                        intent.putExtra(Common.PacketParams.USER_NAME, reply_msg.getString(Common.PacketParams.USER_NAME));
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }else {
                                        final int errorCode = reply_msg.getInt("error");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (errorCode == Common.ErrorCode.NAME_OCCUPIED){
                                                    Toast.makeText(RegisterPage.this, "用户名已被占用", Toast.LENGTH_SHORT).show();
                                                }else {
                                                    Toast.makeText(RegisterPage.this, "服务器端错误", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }catch (JSONException e){
                                    Toast.makeText(RegisterPage.this, "JSON exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            }catch (Exception e){
                                Toast.makeText(RegisterPage.this, "Socket exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            socket.close();
                        }catch (IOException e){
                            Toast.makeText(RegisterPage.this, "Socket exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }).start();
            }
        });
    }
}
