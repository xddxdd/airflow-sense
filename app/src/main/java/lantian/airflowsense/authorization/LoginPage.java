package lantian.airflowsense.authorization;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import lantian.airflowsense.MainActivity;
import lantian.airflowsense.norm.ErrorCode;
import lantian.airflowsense.norm.Instruction;
import lantian.airflowsense.norm.Operation;
import lantian.airflowsense.R;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {

    EditText name;
    EditText password;
    JSONObject send_msg = new JSONObject();
    int msg_length;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Bind the login_page.xml layout

        (findViewById(R.id.login_button)).setOnClickListener(LoginPage.this);
        (findViewById(R.id.register_button)).setOnClickListener(LoginPage.this);
        name = findViewById(R.id.user_name);
        password = findViewById(R.id.password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_button:
                String name_txt = name.getText().toString();
                String password_txt = password.getText().toString();
                if (name_txt.isEmpty()){
                    Toast.makeText(LoginPage.this, "姓名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password_txt.isEmpty()) {
                    Toast.makeText(LoginPage.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    send_msg.put("name", name_txt);
                    send_msg.put("password", password_txt);
                    send_msg.put("operation", Operation.LOGIN);
                    send_msg.put("instruction", 0);
                    send_msg.put("error", 0);
                }catch (JSONException e) {
                    Toast.makeText(LoginPage.this, "JSON exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket("47.97.98.49", 2862);

                            try {
                                OutputStream os = socket.getOutputStream();
                                byte[] send_byte = send_msg.toString().getBytes();
                                msg_length = send_byte.length;
                                os.write(send_byte);
                                socket.shutdownOutput();

                                InputStream is = socket.getInputStream();
                                byte[] reply_byte = new byte[msg_length + 128];
                                if (is.read(reply_byte) != -1) {
                                    Toast.makeText(LoginPage.this, "服务器回传数据故障", Toast.LENGTH_SHORT).show();
                                    throw new Exception("Unexpectedly large server callback data");
                                }
                                socket.shutdownInput();

                                try{
                                    JSONObject reply_msg = new JSONObject(new String(reply_byte));
                                    int instruction = reply_msg.getInt("instruction");
                                    if (instruction == Instruction.APPROVED) {
                                        SharedPreferences user_info = getSharedPreferences("user_info", Context.MODE_PRIVATE);
                                        SharedPreferences.Editor editor = user_info.edit();
                                        editor.putString("name", reply_msg.getString("name"));
                                        editor.putString("password", reply_msg.getString("password"));
                                        editor.apply();
                                        Intent intent = new Intent(LoginPage.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        int errorCode = reply_msg.getInt("error");
                                        if (errorCode == ErrorCode.WRONG_PASSWORD) {
                                            Toast.makeText(LoginPage.this, "密码错误", Toast.LENGTH_SHORT).show();
                                        } else if (errorCode == ErrorCode.NON_EXIST_USER) {
                                            Toast.makeText(LoginPage.this, "用户不存在", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(LoginPage.this, "服务器端错误", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }catch (JSONException e){
                                    Toast.makeText(LoginPage.this, "JSON exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                                }

                            } catch (Exception e) {
                                Toast.makeText(LoginPage.this, "Socket exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            socket.close();
                        }catch (IOException e){
                            Toast.makeText(LoginPage.this, "Socket exception" + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }).start();
                break;
            case R.id.register_button:
                Intent intent = new Intent(LoginPage.this, RegisterPage.class);
                startActivity(intent);
                break;
            case R.id.user_name:
                break;
            default:
        }
    }
}
