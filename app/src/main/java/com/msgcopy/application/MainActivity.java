package com.msgcopy.application;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.alipay.sdk.app.PayTask;

import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button:
                new AliPayTask().execute("0.01","0db30de2ff2611e6aa4200163e04390d","测试商品0.01","测试商品0.01","http://cloudapp.kaoke.me/wapi/pay_service/alipay/");
                break;
        }
    }

    // 支付宝task
    private class AliPayTask extends AsyncTask<String, Void, String> {
        private String tradeId;
        private String price;
        private String title;
        private String body;
        private String notifyUrl;

        private Handler mHandler;

        public AliPayTask(){
            this.mHandler = new Handler();
        }

        @Override
        protected String doInBackground(String... params) {
            this.price = params[0];
            this.tradeId = params[1];
            this.title = params[2];
            this.body = params[3];
            this.notifyUrl = params[4];

            StringBuilder sb = new StringBuilder();
            sb.append("partner=\"");
            sb.append(ThirdConstants.ALIPAY_PARTNER);
            sb.append("\"&out_trade_no=\"");
            sb.append(this.tradeId);
            sb.append("\"&subject=\"");
            sb.append(this.title);
            sb.append("\"&body=\"");
            sb.append(this.body);
            sb.append("\"&total_fee=\"");
            sb.append(this.price);
            sb.append("\"&notify_url=\"");

            // 网址需要做URL编码
            sb.append(URLEncoder.encode(this.notifyUrl));
            sb.append("\"&service=\"mobile.securitypay.pay");
            sb.append("\"&_input_charset=\"UTF-8");
            sb.append("\"&return_url=\"");
            sb.append(URLEncoder.encode("http://m.alipay.com"));
            sb.append("\"&payment_type=\"1");
            sb.append("\"&seller_id=\"");
            sb.append(ThirdConstants.ALIPAY_SELLER);

            // 如果show_url值为空，可不传
            // sb.append("\"&show_url=\"");
            sb.append("\"&it_b_pay=\"15m");
            sb.append("\"");

            String info = new String(sb);
            String sign = Rsa.sign(info, ThirdConstants.ALIPAY_PRIVATE);
            sign = URLEncoder.encode(sign);
            info += "&sign=\"" + sign + "\"&" + "sign_type=\"RSA\"";

            PayTask payTask = new PayTask(MainActivity.this);

            String result = payTask.pay(info);
            LogUtil.i("resultStatus:", result);

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            PayResult rst = new PayResult(result);
            LogUtil.i("resultStatus:", rst.getResultStatus());
            if (rst.getResultStatus().contains("9000")) {
                ToastUtils.showLong(getApplication(),"支付成功");
            } else if (rst.getResultStatus().contains("8000")){
                ToastUtils.showLong(getApplication(),"支付结果确认中");
            }else {
                ToastUtils.showLong(getApplication(),"支付失败");
            }
        }
    }

}
