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

public class RegisterPage extends AppCompatActivity {

    EditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        /* Automatically generated code */
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page); // Bind the login_page.xml layout

        name = findViewById(R.id.register_page_user_name);

        (findViewById(R.id.register_page_register_button)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final String name_txt = name.getText().toString();
                if (name_txt.isEmpty()){
                    Toast.makeText(RegisterPage.this, "姓名不能为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Common.Norms.DEFAULT_USER_NAME.equals(name_txt)){
                    Toast.makeText(RegisterPage.this, "用户名不可用", Toast.LENGTH_SHORT).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket socket = new Socket(Common.Norms.SERVER_IP,Common.Norms.SERVER_PORT);
                            JSONObject send_msg = new JSONObject();
                            send_msg.put(Common.PacketParams.USER_NAME, name_txt);
                            send_msg.put(Common.PacketParams.OPERATION, Common.Operation.REGISTER);
                            try {
                                OutputStream os = socket.getOutputStream();
                                os.write(send_msg.toString().getBytes());
                                socket.shutdownOutput();

                                InputStream is = socket.getInputStream();
                                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                byte[] reply_byte = new byte[128];
                                int len;
                                while ((len = is.read(reply_byte)) != -1){
                                    buffer.write(reply_byte, 0, len);
                                }

                                JSONObject reply_msg = new JSONObject(buffer.toString());
                                if (Common.Instruction.APPROVED == reply_msg.getInt(Common.PacketParams.INSTRUCTION)){
                                    Intent intent = new Intent();
                                    intent.putExtra(Common.PacketParams.USER_NAME, name_txt);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }else {
                                    final int errorCode = reply_msg.getInt(Common.PacketParams.ERRORCODE);
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
        });
    }
}
