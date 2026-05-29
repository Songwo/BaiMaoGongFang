package da0;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import Moudle.TokenManager;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

// 定义一个类，用于创建 Retrofit 实例并提供 API 服务
public class RetrofitClient_python {
    // 定义服务器的基础 URL
    private static final String BASE_URL = "http://wacyg.fun:9000/";
    // 定义一个静态的 Retrofit 实例，用于单例模式
    private static Retrofit retrofit = null;

    // 静态方法，用于获取 ApiService 实例
    public static ApiService getApiService(Context context) {
        // 如果 Retrofit 实例为空，则创建一个新的实例
        if (retrofit == null) {
            // 创建一个日志拦截器，用于记录 HTTP 请求和响应的详细信息
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            // 设置日志拦截器的级别为 BODY，表示记录请求和响应的完整内容
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // 创建一个 OkHttpClient 实例，用于处理 HTTP 请求
            OkHttpClient client = new OkHttpClient.Builder()
                    // 添加一个拦截器，用于在请求头中添加 Authorization 字段
                    .addInterceptor(chain -> {
                        // 获取原始请求
                        Request original = chain.request();

                        // 从 TokenManager 中获取 token
                        String token = new TokenManager(context).getToken();
                        if (token != null) {
                            // 如果 token 不为空，则在请求头中添加 Authorization 字段
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                            return chain.proceed(request);
                        }
                        // 如果 token 为空，则直接发送原始请求
                        return chain.proceed(original);
                    })
                    // 添加日志拦截器
                    .addInterceptor(loggingInterceptor)
                    // 设置连接超时时间
                    .connectTimeout(6000, TimeUnit.SECONDS)
                    // 设置读写超时时间
                    .readTimeout(6000, TimeUnit.SECONDS)
                    .writeTimeout(6000, TimeUnit.SECONDS)
                    .build();

            // 创建一个 Retrofit 实例
            retrofit = new Retrofit.Builder()
                    // 设置服务器的基础 URL
                    .baseUrl(BASE_URL)
                    // 设置 OkHttpClient 实例
                    .client(client)
                    // 添加 Gson 转换器，用于将 JSON 数据转换为 Java 对象
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        // 使用 Retrofit 实例创建 ApiService 实例
        return retrofit.create(ApiService.class);
    }
}



