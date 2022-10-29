package AutoTrading.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    @Value("${security.access-key}")
    private String accessKey;
    @Value("${security.secret-key}")
    private String secretKey;

    HashMap<String, String> params = new HashMap<>();

    public String createTokenForAccount() {
        final Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", UUID.randomUUID().toString())
                .sign(algorithm);
    }
//    public String createTokenForOrder() throws NoSuchAlgorithmException, UnsupportedEncodingException {
//
//        ArrayList<String> queryElements = new ArrayList<>();
//        for(Map.Entry<String, String> entity : params.entrySet()) {
//            queryElements.add(entity.getKey() + "=" + entity.getValue());
//        }
//        String queryString = String.join("&", queryElements.toArray(new String[0]));
//
//        MessageDigest md = MessageDigest.getInstance("SHA-512");
//        md.update(queryString.getBytes("UTF-8"));
//
//        String queryHash = String.format("%0128x", new BigInteger(1, md.digest()));
//        final Algorithm algorithm = Algorithm.HMAC256(secretKey);
//        String jwtToken = JWT.create()
//                .withClaim("access_key", accessKey)
//                .withClaim("nonce", UUID.randomUUID().toString())
//                .withClaim("query_hash", queryHash)
//                .withClaim("query_hash_alg", "SHA512")
//                .sign(algorithm);
//        String authenticationToken = "Bearer " + jwtToken;
//
//        return authenticationToken;
//    }
}
