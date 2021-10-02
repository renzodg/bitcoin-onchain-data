package rpc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class RPCClient {
    private final Gson gson;
    private final HttpClient httpclient;
    private final String rpcServer;

    public <T> RPCResponse<T> invokeRPC(RPCRequest request, Class<T> responseType) throws IOException {
        HttpEntity entity = httpclient.execute(createHttpPost(request)).getEntity();
        return gson.fromJson(EntityUtils.toString(entity), TypeToken.getParameterized(RPCResponse.class, responseType).getType());
    }

    public static List<Object> params(Object... params) {
        return Arrays.asList(params);
    }

    private HttpPost createHttpPost(RPCRequest request) throws UnsupportedEncodingException {
        HttpPost httppost = new HttpPost(rpcServer);
        httppost.setEntity(new StringEntity(gson.toJson(request)));
        return httppost;
    }
}
