package top.sacz.timtool.net.httpconfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import top.sacz.timtool.net.HttpClient;
import top.sacz.timtool.net.entity.TokenInfo;


public class TokenHeader implements Interceptor {

    @Override
    public @NotNull Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        Request.Builder builder = request.newBuilder();
        TokenInfo tokenInfo = HttpClient.getTokenInfo();
        if (tokenInfo != null) {
            String tokenName = tokenInfo.getTokenName();
            String tokenValue = tokenInfo.getTokenValue();
            builder.header(tokenName, tokenValue);
        }
        request = builder.build();
        return chain.proceed(request);
    }

}
