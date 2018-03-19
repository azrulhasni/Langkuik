/*
 * Copyright 2017 Azrul.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.azrul.langkuik.system.security;

import java.lang.reflect.Field;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Sha256Hash;

/**
 *
 * @author Azrul
 */
public class SecretUtils {
    public static HashAndSalt getHashAndSalt(String clearPassword){
        RandomNumberGenerator rng = new SecureRandomNumberGenerator();
        Object salt = rng.nextBytes();
                   
                    // Now hash the plain-text password with the random salt and multiple
                    // iterations and then Base64-encode the value (requires less space than Hex):
        String hashedPasswordBase64 = new Sha256Hash(clearPassword, salt, 1024).toBase64();
        HashAndSalt has = new HashAndSalt();
        has.setHashedPassword(hashedPasswordBase64);
        has.setSalt(salt);
        return has;
                   

                    
    }
}
