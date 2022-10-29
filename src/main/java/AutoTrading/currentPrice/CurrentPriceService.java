package AutoTrading.currentPrice;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class CurrentPriceService {

    public String viewCurrentPrice(String markets) throws IOException {
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet("https://api.upbit.com/v1/ticker?markets=" + markets);
            request.addHeader("accept", "application/json");

            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity, "UTF-8");

            log.info(result);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }
}
