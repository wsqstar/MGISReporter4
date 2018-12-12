package com.wsqstar.mgisreporter4;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private EditText email_login_text;//1 定义两个变量
    private EditText pws_login_text;
//    private String pws_add = "ceshi";//相加一个混淆
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button button_login = (Button)findViewById(R.id.email_sign_in_button);
        ImageButton button_github = (ImageButton)findViewById(R.id.github_img);
        ImageButton button_collect = (ImageButton) findViewById(R.id.collect_img);
        email_login_text = (EditText) findViewById(R.id.email);//2 然后获取资源
        pws_login_text = (EditText) findViewById(R.id.password);

        //登录鉴权
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input_email = email_login_text.getText().toString();//3 将EditText转换为string类型，方便做判断
                String input_pws = pws_login_text.getText().toString();
                //TextView psw_login = (TextView) findViewById(R.id.password);
                if(input_email.equals(input_pws)){//4 判断，如果登陆名与密码相通，通过
                    Toast.makeText(LoginActivity.this,"鉴权成功",
                            Toast.LENGTH_SHORT).show();
                    //下面两行是显式Intent，用于跳转
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }else {
                    Toast.makeText(LoginActivity.this,"鉴权失败",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        button_github.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/wsqstar/MGISReporter4"));
                startActivity(intent);
            }
        });

        button_collect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://whois.aliyun.com/whois/domain/mgis.top?spm=5176.100251.111252.5.e76c4f15XDRAJi&file=mgis.top"));
                startActivity(intent);
            }
        });

    }
}
