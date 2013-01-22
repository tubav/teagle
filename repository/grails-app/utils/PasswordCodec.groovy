/**
 * </copyright>
 *
 * 2008-2010 © Waterford Institute of Technology (WIT),
 *              TSSG, EU FP7 ICT Panlab.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0
 * which accompanies this distribution, and is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * author: Shane Fox
 *
 * </copyright>
 *
 */

import java.security.MessageDigest
import sun.misc.BASE64Encoder
import sun.misc.CharacterEncoder

class PasswordCodec {

    static encode = { str ->
		MessageDigest md = MessageDigest.getInstance("MD5")
                md.reset()
		md.update(str.getBytes('UTF-8'))

                byte[] digest= md.digest()
                StringBuffer hexString = new StringBuffer()
                for (int i=0;i<digest.length;i++) {
                    //Convert to hex and keep the leading zeros
                    hexString.append(Integer.toString((digest[i] & 0xFF) + 0x100, 16).substring(1))
                }
		return hexString
            }
}

