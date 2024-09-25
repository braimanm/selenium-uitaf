/*
Copyright 2010-2024 Michael Braiman braimanm@gmail.com
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.braimanm.uitaf.utils;

import com.braimanm.datainstiller.generators.WordGenerator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.nio.ByteBuffer;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Base64;

@SuppressWarnings("unused")
public class Crypt {
    private transient final byte[] salt;
    private final int iterations = 100;
    private final SecretKey key;
    private final Cipher cipher;

    public Crypt(String pswd) {
        this(pswd, null);
    }

    public Crypt(String pswd, String salt) {
        String osalto = System.getenv("OSALTO");
        if (salt == null || salt.isEmpty()) {
            if (osalto != null && !osalto.isEmpty()) {
                this.salt = hash(osalto);
            } else {
                this.salt = "secret01".getBytes();
            }
        } else {
            this.salt = hash(salt);
        }

        KeySpec keySpec = new PBEKeySpec(pswd.toCharArray(), this.salt, iterations);
        try {
            key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
            cipher = Cipher.getInstance(key.getAlgorithm());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] hash(String string) {
        StringBuilder sb = new StringBuilder(string);
        ByteBuffer b = ByteBuffer.allocate(8);
        b.putInt(sb.toString().hashCode());
        b.putInt(sb.reverse().toString().hashCode());
        return b.array();
    }

    public String encrypt(String input) {
        try {
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
            cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(input.getBytes()));
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    public String decrypt(String base64Input) {
        try {
            AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterations);
            cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
            byte[] input = Base64.getDecoder().decode(base64Input.getBytes());
            return new String(cipher.doFinal(input));
        } catch (Exception e) {
            return new WordGenerator().generate("[a][b][c]");
        }
    }

}
