package sloev.ripple.users;

import com.quickblox.users.model.QBUser;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

/**
 * Created by johannes on 12/03/15.
 */
public class UserStructure {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    private boolean enabled;

    public UserStructure(String exponent, String modulus, boolean enabled) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory fact = KeyFactory.getInstance("RSA");
        RSAPublicKeySpec pub = new RSAPublicKeySpec(new BigInteger(exponent), new BigInteger(modulus));
        publicKey = (RSAPublicKey) fact.generatePublic(pub);
        this.enabled = enabled;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        this.enabled = true;
    }
    public void disable() {
        this.enabled = false;
    }
}
