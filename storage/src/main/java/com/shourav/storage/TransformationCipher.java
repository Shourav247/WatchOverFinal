package com.shourav.storage;


public class TransformationCipher {
	private static final String _ = "/";

	public static final String AES_CBC_NoPadding = AlgorithmCipher.AES + _ + ModeCipher.CBC + _ + PaddingCipher.NoPadding;
	public static final String AES_CBC_PKCS5Padding = AlgorithmCipher.AES + _ + ModeCipher.CBC + _ + PaddingCipher.PKCS5Padding;
	public static final String AES_ECB_NoPadding = AlgorithmCipher.AES + _ + ModeCipher.ECB + _ + PaddingCipher.NoPadding;
	public static final String AES_ECB_PKCS5Padding = AlgorithmCipher.AES + _ + ModeCipher.ECB + _ + PaddingCipher.PKCS5Padding;

	public static final String DES_CBC_NoPadding = AlgorithmCipher.DES + _ + ModeCipher.CBC + _ + PaddingCipher.NoPadding;
	public static final String DES_CBC_PKCS5Padding = AlgorithmCipher.DES + _ + ModeCipher.CBC + _ + PaddingCipher.PKCS5Padding;
	public static final String DES_ECB_NoPadding = AlgorithmCipher.DES + _ + ModeCipher.ECB + _ + PaddingCipher.NoPadding;
	public static final String DES_ECB_PKCS5Padding = AlgorithmCipher.DES + _ + ModeCipher.ECB + _ + PaddingCipher.PKCS5Padding;

	public static final String DESede_CBC_NoPadding = AlgorithmCipher.DESede + _ + ModeCipher.CBC + _ + PaddingCipher.NoPadding;
	public static final String DESede_CBC_PKCS5Padding = AlgorithmCipher.DESede + _ + ModeCipher.CBC + _ + PaddingCipher.PKCS5Padding;
	public static final String DESede_ECB_NoPadding = AlgorithmCipher.DESede + _ + ModeCipher.ECB + _ + PaddingCipher.NoPadding;
	public static final String DESede_ECB_PKCS5Padding = AlgorithmCipher.DESede + _ + ModeCipher.ECB + _ + PaddingCipher.PKCS5Padding;

	public static final String RSA_ECB_PKCS1Padding = AlgorithmCipher.RSA + _ + ModeCipher.ECB + _ + PaddingCipher.PKCS1Padding;
	public static final String RSA_ECB_OAEPWithSHA_1AndMGF1Padding = AlgorithmCipher.RSA + _ + ModeCipher.ECB + _ + PaddingCipher.OAEPWithSHA_1AndMGF1Padding;
	public static final String RSA_ECB_OAEPWithSHA_256AndMGF1Padding = AlgorithmCipher.RSA + _ + ModeCipher.ECB + _ + PaddingCipher.OAEPWithSHA_256AndMGF1Padding;

}
