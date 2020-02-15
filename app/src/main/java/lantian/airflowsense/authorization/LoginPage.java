package lantian.airflowsense.authorization;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import lantian.airflowsense.Common;
import lantian.airflowsense.R;

public class LoginPage extends AppCompatActivity implements View.OnClickListener {

    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page); // Bind the login_page.xml layout

        (findViewById(R.id.login_page_login_button)).setOnClickListener(LoginPage.this);
        (findViewById(R.id.login_page_register_button)).setOnClickListener(LoginPage.this);
        name = findViewById(R.id.login_page_user_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.login_page_login_button:
                handleLogin();
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

    private void handleLogin(){
        final String name_txt = name.getText().toString();
        if (Common.Norms.DEFAULT_USER_NAME.equals(name_txt)){
            Toast.makeText(LoginPage.this, "用户名不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                String user_name = (name_txt.isEmpty()) ? Common.Norms.DEFAULT_USER_NAME : name_txt;
                try {
                    JSONObject send_msg = new JSONObject();
                    send_msg.put(Common.PacketParams.USER_NAME, user_name);
                    send_msg.put(Common.PacketParams.OPERATION, Common.Operation.LOGIN);

                    Socket socket = new Socket(Common.Norms.SERVER_IP, Common.Norms.SERVER_PORT);

                    try {
                        OutputStream os = socket.getOutputStream();
                        os.write(send_msg.toString().getBytes());
                        socket.shutdownOutput();

                        InputStream is = socket.getInputStream();
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        byte[] input_byte = new byte[128];
                        int len;
                        while ((len = is.read(input_byte)) != -1){
                            buffer.write(input_byte, 0, len);
                        }

                        JSONObject reply_msg = new JSONObject(buffer.toString());
                        if (Common.Instruction.APPROVED == reply_msg.getInt(Common.PacketParams.INSTRUCTION)) {
                            Intent intent = new Intent();
                            intent.putExtra(Common.PacketParams.USER_NAME, name_txt);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            final int errorCode = reply_msg.getInt(Common.PacketParams.ERRORCODE);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (errorCode == Common.ErrorCode.NON_EXIST_USER) {
                                        Toast.makeText(LoginPage.this, "用户不存在", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginPage.this, "服务器端错误", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
