package com.example.mychat.util.fs;

import com.alibaba.fastjson2.JSONObject;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FsMsgcrypt {
    public static void main(String[] args) throws Exception {

        FsMsgcrypt d = new FsMsgcrypt("epjKVQbSMyN5vzvGHAcAE6vEEPZsUjaU");
        String decrypt = d.decrypt("38RBc3c2HhgQPoveA1Gchd4F3XS4mWT6LehnWSXHB0+8IZNp4xZX3/5eVn67jcV28U3grSQbzHmX/quUItim1tVWVumGzEyLL9jYTCxQPKCDm/baHjiWH/saLJaS29kAZr+/ZhQkNv2wHJb3UeYo0IeyFUuzKzWg2zW/ize7ULmJ0v85WyZKvJG3LXtsE1ftqC6zp6Ww4I0XhCR97+dGRKHMJVwmMwAymqhzFx4JUM0+g417kcvE+1sF4JougaR54Fu1KoHN3gD4NUo7fvBBJWaG4u3Bdo1VuyvUEpKKgVTb8LSOHbBOTRc2XLwPwgrFGYQXyCE3gYh9KnbSnqbpiz/hcnjjOZeFdg5iJY7gPelZ4ymSmmR897oFMyu0LJUfPrqW4cjynRpGlFY42WSsB41sqC4oW+vHceUv2lxzdVLrwWBVfnh3hQl1Lq5z807hO0vvEs/FZS8rxO5VwEwvJjbmNQzv0nrHHjObwR9jUTJ1QucMUYkDU3GHrWyRyzqoIi5Y+MBSnPXscFVzGbkMRjPYq766sZ8bEUTFuQzTOBuPwQbdTJcm84ic/M9yCfmjgXFYYrDUtISTo54pTvXVoxeIae9XLbztkBoDtrFjP4VWMjoYBXszM/I9XYxTuwCgLSB4tRQ9Yyhc045uH+8rMQgt6C99uyM8ZfCQwu8fJqOvM/CtcijC4U6LEtuDB6s+X7+Y7iCL5TtMVvISlPYDWZ37Bb6k3LSaTDmYzOhOpbYd6t84tELJxsoDPOAVILM4qVtZO91efKS4nL4Gc8YdnS4w1h7wDiTC7Z33uGKOnNPpA8G2YGWe5PKd80ikstEpUkrF9Yx0BBnfHNjqWTdt8ri1vhafQ9XRR8CkHpIk7ISIamqFaQslR14VTbjZKEFLBqROEjoBB1PjLsgnLDqhtQ==");
        JSONObject jsonObject = JSONObject.parseObject(decrypt);

        System.out.println(decrypt); //hello world

    }

    private byte[] keyBs;


    public FsMsgcrypt(String key) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // won't happen
        }
        keyBs = digest.digest(key.getBytes(StandardCharsets.UTF_8));
    }

    public String decrypt(String base64) throws Exception {
        byte[] decode = Base64.getDecoder().decode(base64);
        Cipher cipher = Cipher.getInstance("AES/CBC/NOPADDING");
        byte[] iv = new byte[16];
        System.arraycopy(decode, 0, iv, 0, 16);
        byte[] data = new byte[decode.length - 16];
        System.arraycopy(decode, 16, data, 0, data.length);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBs, "AES"), new IvParameterSpec(iv));
        byte[] r = cipher.doFinal(data);
        if (r.length > 0) {
            int p = r.length - 1;
            for (; p >= 0 && r[p] <= 16; p--) {
            }
            if (p != r.length - 1) {
                byte[] rr = new byte[p + 1];
                System.arraycopy(r, 0, rr, 0, p + 1);
                r = rr;
            }
        }
        return new String(r, StandardCharsets.UTF_8);
    }


}