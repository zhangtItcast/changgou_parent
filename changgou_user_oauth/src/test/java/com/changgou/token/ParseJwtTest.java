package com.changgou.token;

import org.junit.Test;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

/*****
 * @Author: Steven
 * @Date: 2019/7/7 13:48
 * @Description: com.changgou.token
 *  使用公钥解密令牌数据
 ****/
public class ParseJwtTest {

    /***
     * 校验令牌
     */
    @Test
    public void testParseToken(){
        //令牌
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJyb2xlcyI6IlJPTEVfVklQLFJPTEVfVVNFUiIsIm5hbWUiOiJpdGhlaW1hIiwiaWQiOiIxIn0.LmYPfcsJdjK_02aEn76iT4ymtacofHGPI0u3Xy7G9Cm4EKjL7cXaFFwuxb192eX7uMIR-b89p-nSEas33OZC24rRnl43bdw_BsUe7fWt2qGDF-vD11DfAx7W0i8pzqt3oFO8SKVTqPn-j7Wov8jpknFuriOGiU6V6aggeW2BJdLcM-gQNX3siBr7x40G4_EVu0rF8HN2PhO9XyC_4v_w_o_ReqouaMoqOfG7L1ilFHarNMRmhlDhktYR6QaU9D8-GQl0zZmMqAVtlVzCbW-TGYwyhUGvmhiMh8JxSWZ65bpYQo_by9Dy9nkT1nxxXh4sZsRbMo10Wb3sAq9yPBy1kA";

        //公钥
        String publickey = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAgdM2//UC1J7HR9N0cDKpq28KT0SaZLgR2xL1tfgkHi8FICB4mML2Zlmabjg74I/Y2vo9JfyEpBP4WLpgknYBD6vdJQ4CjSaDz4u6DNXi/MHPp39c0tcL3BDN06SdlUt3RnM+xugpHcLZK2MPYN8w+rsfF34c0LDxShvNx2NFohgWF/RJRP1xU904TVyV4IMb+eNdAdLmaqgpaRXTSD+xSn3g2mPELCYm1cQZ9YDRwv1k6I4gorZHLDY/4M2XRb3lPw9IyV4nEIQe3tn4lJP7C7IYRjRRw5I8qKhQ4rhVHrY/NTGYchVlREu9wRguNVVE/9UrHKBraoLibsHM+WyOqQIDAQAB-----END PUBLIC KEY-----";

        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(publickey));

        //获取Jwt原始内容
        String claims = jwt.getClaims();
        System.out.println(claims);
        //jwt令牌
        String encoded = jwt.getEncoded();
        System.out.println(encoded);
    }
}
