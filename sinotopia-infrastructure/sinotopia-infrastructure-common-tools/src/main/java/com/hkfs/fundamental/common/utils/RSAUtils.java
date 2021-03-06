package com.hkfs.fundamental.common.utils;

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;



/** *//**
 * <p>
 * RSA公钥/私钥/签名工具包
 * </p>
 * <p>
 * 罗纳德·李维斯特（Ron [R]ivest）、阿迪·萨莫尔（Adi [S]hamir）和伦纳德·阿德曼（Leonard [A]dleman）
 * </p>
 * <p>
 * 字符串格式的密钥在未在特殊说明情况下都为BASE64编码格式<br/>
 * 由于非对称加密速度极其缓慢，一般文件不使用它来加密而是使用对称加密，<br/>
 * 非对称加密算法可以用来对对称加密的密钥加密，这样保证密钥的安全也就保证了数据的安全
 * </p>
 * 
 * @author IceWee
 * @date 2012-4-26
 * @version 1.0
 */
public class RSAUtils {
	/**
     * 加密算法RSA
     */
    public static final String KEY_ALGORITHM = "RSA";
    
    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /**
     * 获取公钥的key
     */
    private static final String KEY_PUBLIC = "RSAPublicKey";
    
    /**
     * 获取私钥的key
     */
    private static final String KEY_PRIVATE = "RSAPrivateKey";
    
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;
    
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;

    public static final String TRANSFORMATION_NAME = "RSA/ECB/PKCS1PADDING";
    
	  /** *//**
	  * <p>
	  * BASE64字符串解码为二进制数据
	  * </p>
	  * 
	  * @param base64
	  * @return
	  * @throws Exception
	  */
	 public static byte[] decode(String base64) throws Exception {
		return Base64_2.decode(base64);
//	     return new BASE64Decoder().decodeBuffer(base64);
	 }
	 
	 /** *//**
	  * <p>
	  * 二进制数据编码为BASE64字符串
	  * </p>
	  * 
	  * @param bytes
	  * @return
	  * @throws Exception
	  */
	 public static String encode(byte[] bytes) throws Exception {
		return Base64_2.encodeBytes(bytes);
//	     return new String(new BASE64Encoder().encode(bytes));
	 }
    
    /**
     * <p>
     * 生成密钥对(公钥和私钥)
     * </p>
     * 
     * @return
     * @throws Exception
     */
    public static Map<String, Object> genKeyPair() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(1024);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        Map<String, Object> keyMap = new HashMap<String, Object>(2);
        keyMap.put(KEY_PUBLIC, publicKey);
        keyMap.put(KEY_PRIVATE, privateKey);
        return keyMap;
    }
    
    /**
     * <p>
     * 用私钥对信息生成数字签名
     * </p>
     * 
     * @param data 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * 
     * @return
     * @throws Exception
     */
    public static String sign(byte[] data, String privateKey) throws Exception {
        byte[] keyBytes = decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateK);
        signature.update(data);
        return encode(signature.sign());
    }

    /**
     * <p>
     * 校验数字签名
     * </p>
     * 
     * @param data 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param sign 数字签名
     * 
     * @return
     * @throws Exception
     * 
     */
    public static boolean verify(byte[] data, String publicKey, String sign)
            throws Exception {
        byte[] keyBytes = decode(publicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicK = keyFactory.generatePublic(keySpec);
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicK);
        signature.update(data);
        return signature.verify(decode(sign));
    }

    /**
     * <P>
     * 私钥解密
     * </p>
     * 
     * @param encryptedData 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey)
            throws Exception {
        byte[] keyBytes = decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
        cipher.init(Cipher.DECRYPT_MODE, privateK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }

    /**
     * <p>
     * 公钥解密
     * </p>
     * 
     * @param encryptedData 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey)
            throws Exception {
        byte[] keyBytes = decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
        cipher.init(Cipher.DECRYPT_MODE, publicK);
        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return decryptedData;
    }
    
    /** *//**
     * <P>
     * 私钥解密
     * </p>
     * 
     * @param encryptedData 已加密数据
     * @param privateKey 私钥(BASE64编码)
     * @param charset 数据最终转换成字符串的字符集
     * @return
     * @throws Exception
     */
    public static String decryptByPrivateKey(byte[] encryptedData, String privateKey, String charset)
            throws Exception {
    	byte[] decryptedData = decryptByPrivateKey(encryptedData, privateKey);
		return new String(decryptedData, charset);
    }


    /**
     * <p>
     * 公钥加密
     * </p>
     * 
     * @param data 源数据
     * @param publicKey 公钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPublicKey(byte[] data, String publicKey)
            throws Exception {
        byte[] keyBytes = decode(publicKey);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicK = keyFactory.generatePublic(x509KeySpec);
        // 对数据加密
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, publicK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /** *//**
     * <p>
     * 公钥解密
     * </p>
     * 
     * @param encryptedData 已加密数据
     * @param publicKey 公钥(BASE64编码)
     * @param charset 数据最终转换成字符串的字符集
     * @return
     * @throws Exception
     */
    public static String decryptByPublicKey(byte[] encryptedData, String publicKey, String charset)
            throws Exception {
    	byte[] decryptedData = decryptByPublicKey(encryptedData, publicKey);
		return new String(decryptedData, charset);
    }
    
    /**
     * <p>
     * 私钥加密
     * </p>
     * 
     * @param data 源数据
     * @param privateKey 私钥(BASE64编码)
     * @return
     * @throws Exception
     */
    public static byte[] encryptByPrivateKey(byte[] data, String privateKey)
            throws Exception {
        byte[] keyBytes = decode(privateKey);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateK = keyFactory.generatePrivate(pkcs8KeySpec);
        Cipher cipher = Cipher.getInstance(TRANSFORMATION_NAME);
        cipher.init(Cipher.ENCRYPT_MODE, privateK);
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * <p>
     * 获取私钥
     * </p>
     * 
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPrivateKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(KEY_PRIVATE);
        return encode(key.getEncoded());
    }

    /**
     * <p>
     * 获取公钥
     * </p>
     * 
     * @param keyMap 密钥对
     * @return
     * @throws Exception
     */
    public static String getPublicKey(Map<String, Object> keyMap)
            throws Exception {
        Key key = (Key) keyMap.get(KEY_PUBLIC);
        return encode(key.getEncoded());
    }

    
    public static final String PUBLIC_KEY = 
    		"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCDzxV2iObvCDIrxrqzpR50HaCl3tAMhBgfyc+/"+
			"XTs7UYHejcsgEe7wKWwPPCihMRIyopGD4YCOD+LXN32eS2vcTIQV7qiCAnKX7LxVY/QRz0biQb5A"+
			"/kJgUsngeVo/JFHk1pHVDLCPpHWSRETRhLnHbRc2clIv0P5HTJTxLbjfDQIDAQAB";
    
    public static final String PRIVATE_KEY = 
    		"MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAIPPFXaI5u8IMivGurOlHnQdoKXe"+
			"0AyEGB/Jz79dOztRgd6NyyAR7vApbA88KKExEjKikYPhgI4P4tc3fZ5La9xMhBXuqIICcpfsvFVj"+
			"9BHPRuJBvkD+QmBSyeB5Wj8kUeTWkdUMsI+kdZJERNGEucdtFzZyUi/Q/kdMlPEtuN8NAgMBAAEC"+
			"gYAEHOkhisISAWJ3ZTscMfHSU75YjqxVR6XtEAIQiZs4jaGMzUXiWUzoZ5J8ozbtWLelptb9k4LM"+
			"bAh7CLs9vkK+UE9z5sO3Mw2ArCqE/+L8++drKQBfSsOo5J/B4S2MM2l50DQyO5Rd4BZHmjcV9o7l"+
			"noGkWV9TvdHlozyj2L2koQJBAOYSRpl8bX0a7yxyNgOMkk736u/dbwLHGCzB8o6vS62Du6EEbSDz"+
			"sEDc8m4DQ4wn2WHw9ezf4flXsz+Ab+7jWGUCQQCSqd2Tj7sLM2PxFkaiIIkHVLdsecHxGO47OxD5"+
			"4vxbbj4Hm99ftS3yiUQD/K5wIz7cjGt1FofIrIekJER73L2JAkAbmZmqlAi2d2K13EWqi1SJ8KfY"+
			"eqH0nVnDFMk6YMEdYa5ClLtatqEwRtE2bWHPEIC9hSCbeAgt112Dgq7q448ZAkAN5a6rRtlQbGQx"+
			"+gxjXHXfjfV7f+YStGwOjMBFDW8gMsgJ7Ik0BnT+IGejgRP+aDiSqXOdOq9PIpoPwagDnJ0RAkEA"+
			"hEM0EpMbT6moPNplMsZP6ZROBfT5lHOlriKL82GqSHPvnQnlEQD52cSTjXRm3TUQDZjYQ1+T0h1l"+
			"gklDMRPtRw==";

	// 生成密钥的工具类
	// public class RSATester {
//	static String publicKey;
//	static String privateKey;
//	static {
//		try {
//			Map<String, Object> keyMap = RSAUtils.genKeyPair();
//			publicKey = RSAUtils.getPublicKey(keyMap);
//			privateKey = RSAUtils.getPrivateKey(keyMap);
//			System.err.println("公钥: \n\r" + publicKey);
//			System.err.println("私钥： \n\r" + privateKey);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void main(String[] args) throws Exception {
//		test();
//		testSign();
//	}
//
//	static void test() throws Exception {
//		System.err.println("公钥加密——私钥解密");
//		String source = "这是一行测试RSA数字签名的无意义文字";
//		System.out.println("\r加密前文字：\r\n" + source);
//		byte[] data = source.getBytes();
//		byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
//		System.out.println("加密后文字：\r\n" + new String(encodedData));
//		byte[] decodedData = RSAUtils.decryptByPrivateKey(encodedData,
//				privateKey);
//		String target = new String(decodedData);
//		System.out.println("解密后文字: \r\n" + target);
//	}
//
//	static void testSign() throws Exception {
//		System.err.println("私钥加密——公钥解密");
//		String source = "这是一行测试RSA数字签名的无意义文字";
//		System.out.println("原文字：\r\n" + source);
//		byte[] data = source.getBytes();
//		byte[] encodedData = RSAUtils.encryptByPrivateKey(data, privateKey);
//		System.out.println("加密后：\r\n" + new String(encodedData));
//
//		byte[] decodedData = RSAUtils
//				.decryptByPublicKey(encodedData, publicKey);
//		String target = new String(decodedData);
//		System.out.println("解密后: \r\n" + target);
//		System.err.println("私钥签名——公钥验证签名");
//		String sign = RSAUtils.sign(encodedData, privateKey);
//		System.err.println("签名:\r" + sign);
//		boolean status = RSAUtils.verify(encodedData, publicKey, sign);
//		System.err.println("验证结果:\r" + status);
//	}
	// }
}

