package dev.ssdd.rtdb;

import dev.ssdd.zot.JSONObject;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Console;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.Scanner;

import static dev.ssdd.rtdb.GenFile.readFileAndReturnCred;

public class PewPew {
    private final String pw;

    public PewPew(String pw) {
        this.pw = pw;
    }

    private final SecureRandom RAND = new SecureRandom();

    public Optional<String> generateSalt(final int length) {

        if (length < 1) {
            System.err.println("error in generateSalt: length must be > 0");
            return Optional.empty();
        }

        byte[] salt = new byte[length];
        RAND.nextBytes(salt);

        return Optional.of(Base64.getEncoder().encodeToString(salt));
    }

    public Optional<String> hashPassword(String password, String salt) {

        char[] chars = password.toCharArray();
        byte[] bytes = salt.getBytes();

        int ITERATIONS = 65536;
        int KEY_LENGTH = 512;
        PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

        Arrays.fill(chars, Character.MIN_VALUE);

        try {
            String ALGORITHM = "PBKDF2WithHmacSHA512";
            SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] securePassword = fac.generateSecret(spec).getEncoded();
            return Optional.of(Base64.getEncoder().encodeToString(securePassword));

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            System.err.println("Exception encountered in hashPassword()");
            return Optional.empty();

        } finally {
            spec.clearPassword();
        }
    }

    public boolean verifyPassword(String key, String salt) {
        Optional<String> optEncrypted = hashPassword(pw, salt);
        return optEncrypted.map(s -> s.equals(key)).orElse(false);
    }

    static void createpw(Scanner sc) {
        System.out.println("Please create password for db: ");
        String pw;
        if (!(pw = sc.nextLine()).equals("") && !(pw.length() < 8)) {
            System.out.println("Want to change above password? (y/N)");
            if (sc.nextLine().equalsIgnoreCase("y")) {
                createpw(sc);
            } else {
                PewPew pew = new PewPew(pw);
                String key, salt;
                salt = pw;
                key = pew.hashPassword(pw, salt).get();
//                String cred = GenFile.readFileAndReturnCred();
                JSONObject j = new JSONObject();
                j.put("key", key);
                GenFile.updateCred(j);
            }
        } else {
            System.out.println("Please enter password grater then 8 chars");
            createpw(sc);
        }
    }

     static void verifypw(Scanner sc) {
        String x = readFileAndReturnCred();
        if (x!=null) {
            String key = new JSONObject(x).getString("key");
            System.out.println("Please enter password: ");
            String pw = sc.nextLine();
            PewPew pew = new PewPew(pw);
            if (!pew.verifyPassword(key, pw)) {
                System.err.println("Incorrect Password.");
                verifypw(sc);
            } //clearConsole();
        } else {
            System.out.println("Something went wrong, please delete the folder:- " + System.getProperty("user.home") + File.separator + ".ssddrtdb");
        }
    }
}
