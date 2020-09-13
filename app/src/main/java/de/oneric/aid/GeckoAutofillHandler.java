/*
    This file is part of AiD.

    AiD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AiD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AiD.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020  Oneric
*/
package de.oneric.aid;

import android.util.Base64;
import android.util.Log;

import org.mozilla.geckoview.Autocomplete;
import org.mozilla.geckoview.Autocomplete.LoginEntry;
import org.mozilla.geckoview.GeckoResult;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

class GeckoAutofillHandler implements Autocomplete.LoginStorageDelegate {
    private static final String TAG = "core/autofill";

    private final Config config;
    private final MessageDigest hasher;

    public GeckoAutofillHandler(Config conf) {
        config = conf;
        MessageDigest h = null;
        try {
            h = MessageDigest.getInstance("SHA-512");
        } catch(NoSuchAlgorithmException e) {
            Log.e(TAG, "hash algorithm not available: "+ e.getMessage());
        }
        hasher = h;
    }

    /*
     * This babeling and saving of login data is only done on user request.
     * Users are recommended to use AoD's "Remember login" or if that doesn't
     * work any password manager, like eg KeypassDX, and only as a last resort
     * the app internal "Save Login".
     *
     * If users insist, save password in private SharedPreferences.
     * Moving App to SDCard and Backups are disallowed for this purpose.
     * Clouding the password and password length should be sufficient to fend
     * off untargeted malware with root-priveliges, but is ofc utterly useless
     * against targeted attacks.
     * For more details about loginstorage "safety" refer to Security.rst
     */

    /**
     * (#data(in char) concat data concat randomBits_{2^n-#data}) xor repeat(key)
     * With n bounded to >= 6; # is length operator
     *
     * This limits password length to 10^10-1, which seems more than reasonable
     * As key use a long hash or something
     *
     * @param data Data to babel
     * @param key Flipped Byte Buffer, limit must be real size.
     * @return null on failure, otherwise babelified data as String.
     */
    private String babelify( String data, final ByteBuffer key) {
        if(data.isEmpty() || key.limit() == 0) return null;

        //Get byte size of data
        byte bdata[] = data.getBytes(StandardCharsets.UTF_8);

        // Can't handle data longer than 65535 (2^16-1)
        if(bdata.length > Character.MAX_VALUE) return null;

        // Round size up to nearest power of 2
        int length = 2;
        for(int i = 1; length < bdata.length+Character.BYTES; length*=2) ;
        if(length < 32) length = 32;
        ByteBuffer ibuff = ByteBuffer.allocate(bdata.length+Character.BYTES);
        ibuff.putChar((char)bdata.length);
        ibuff.put(bdata);

        ibuff.rewind();

        ByteBuffer result = ByteBuffer.allocate(length);
        for(int i = 0; i < bdata.length+Character.BYTES; ++i) {
            result.put(
                    (byte)(ibuff.get() ^ key.get(i%key.limit()))
            );
        }

        //Fill remaining space up with random data
        if(bdata.length+Character.BYTES < length) {
            Random rand = new Random();
            int rl =  length - bdata.length - Character.BYTES;
            byte[] rest = new byte[rl];
            rand.nextBytes(rest);
            result.put(rest);
        }

        // XORed Data is likely no longer valid UTF-8; storing Base64 encoded
        return Base64.encodeToString(result.array(), Base64.DEFAULT);
    }

    /**
     * Same as babelify but reversed
     */
    private String unbabel (String data, final ByteBuffer key) {
        if(data.isEmpty() || key.limit() == 0) return null;

        byte[] bdata = Base64.decode(data, Base64.DEFAULT);
        ByteBuffer im = ByteBuffer.allocate(bdata.length);

        for(int i = 0; i < bdata.length; ++i) {
            im.put(
                    (byte)(bdata[i] ^ key.get(i%key.limit()))
            );
        }
        im.rewind();
        char dlength = im.getChar();
        byte[] rdata = new byte[dlength];
        im.get(rdata);

        return new String(rdata, StandardCharsets.UTF_8);
    }

    /**
     * @param inp String to be hashed
     * @return SHA-512 hash if available, null if SHA-512 is not possible
     */
    private byte[] sha512(String inp) {
        if(inp == null | inp.isEmpty()) return null;
        if(hasher == null) {
            Log.e("core/loginstore", "Attempting to hash with unavailable algorithm!");
            return null;
        }

        return hasher.digest(inp.getBytes(StandardCharsets.UTF_8));
    }



    @Override
    public GeckoResult<LoginEntry[]> onLoginFetch(String domain) {
        if(!domain.equals(Config.DOMAIN_AOD))
            return null;

        String[] creds = config.retrieveCredentials();
        if(creds[0].isEmpty() || creds[1].isEmpty()) {
            Log.d(TAG, "No valid creds stored.");
            return null;
        }
        creds[1] = unbabel(creds[1], ByteBuffer.wrap(sha512(domain)));
        if(creds[1] == null) {
            Log.e(TAG, "onLoginFetch: unbabel failed!");
            return null;
        }

        return GeckoResult.fromValue(new LoginEntry[] {
                //LoginFetch domain is domain only, but LoginEntries typically also have protocol
                new LoginEntry.Builder()
                        .guid("aod")
                        .origin("https://"+domain)
                        .username(creds[0])
                        .password(creds[1])
                        .formActionOrigin("https://"+domain)
                        .build()
        });
    }

    @Override
    public void onLoginSave(LoginEntry login) {
        if(!config.rememberLogin()) return;
        if(login.password   == null || login.password.isEmpty()
          || login.username == null || login.username.isEmpty()
          || login.origin   == null || login.origin.isEmpty()
        ) {
            Log.d(TAG, "Invalid entry passed to onLoginSave");
            return;
        }


        String domain = login.origin
                .replaceFirst("^https?://", "")
                .replaceAll("(/.*)+$", "");

        String pw = babelify(login.password, ByteBuffer.wrap(sha512(domain)));
        if(pw != null)
            config.storeCredentials(login.username, pw);
        else
            Log.e(TAG, "babeling creds failed!");
    }

    /*@Override
    public void onLoginUsed(LoginEntry login, int usedFields) {
        Log.d("autoComplete", "StorageDelegate.onLoginUsed");
    }*/

}
