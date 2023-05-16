/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.trans.steps.symmetriccrypto.symmetriccryptotrans;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.loadsave.LoadSaveTester;
import org.pentaho.di.trans.steps.loadsave.validator.FieldLoadSaveValidator;
import org.pentaho.di.trans.steps.loadsave.validator.IntLoadSaveValidator;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SymmetricCryptoTransMetaTest {
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  @Test
  public void testRoundTrip() throws KettleException {
    KettleEnvironment.init();

    List<String> attributes = Arrays.asList( "operation_type", "algorithm", "schema", "secretKeyField", "messageField",
      "resultfieldname", "secretKey", "secretKeyInField", "readKeyAsBinary", "outputResultAsBinary" );

    Map<String, String> getterMap = new HashMap<String, String>();
    getterMap.put( "operation_type", "getOperationType" );
    getterMap.put( "algorithm", "getAlgorithm" );
    getterMap.put( "schema", "getSchema" );
    getterMap.put( "secretKeyField", "getSecretKeyField" );
    getterMap.put( "messageField", "getMessageField" );
    getterMap.put( "resultfieldname", "getResultfieldname" );
    getterMap.put( "secretKey", "getSecretKey" );
    getterMap.put( "secretKeyInField", "isSecretKeyInField" );
    getterMap.put( "readKeyAsBinary", "isReadKeyAsBinary" );
    getterMap.put( "outputResultAsBinary", "isOutputResultAsBinary" );

    Map<String, String> setterMap = new HashMap<String, String>();
    setterMap.put( "operation_type", "setOperationType" );
    setterMap.put( "algorithm", "setAlgorithm" );
    setterMap.put( "schema", "setSchema" );
    setterMap.put( "secretKeyField", "setsecretKeyField" );
    setterMap.put( "messageField", "setMessageField" );
    setterMap.put( "resultfieldname", "setResultfieldname" );
    setterMap.put( "secretKey", "setSecretKey" );
    setterMap.put( "secretKeyInField", "setSecretKeyInField" );
    setterMap.put( "readKeyAsBinary", "setReadKeyAsBinary" );
    setterMap.put( "outputResultAsBinary", "setOutputResultAsBinary" );

    Map<String, FieldLoadSaveValidator<?>> fieldLoadSaveValidator = new HashMap<String, FieldLoadSaveValidator<?>>();
    fieldLoadSaveValidator.put( "operation_type",
      new IntLoadSaveValidator( SymmetricCryptoTransMeta.operationTypeCode.length ) );

    LoadSaveTester loadSaveTester = new LoadSaveTester( SymmetricCryptoTransMeta.class, attributes,
      getterMap, setterMap, fieldLoadSaveValidator, new HashMap<String, FieldLoadSaveValidator<?>>() );

    loadSaveTester.testSerialization();
    //testAES();
  }

  public void testAES() throws KettleException {
    //String content ="BF3E22FF9B93D66BE2B90C257BE635E8ABA83C7CD3D864541B03D1562E3B24AF2D97A8599FF4E95E17BE7B25B6DC9EBD1DCB571D0BA5D499FB800F9624DDA25051312B3C28EE6511E72B960D7ECE2A73CF7A42DBF964A7BE8E77CB46CCA773212D2586C0E2862DB8D5C8DCB778E13FD7";
    String content ="25EE9C9A3CC0F449FE153A7768C20F3BC8C1E5514D8DB84145EF542CC5A8C0404C2D9B5856072437CA73BD76C33EA1A5250EE401F64427BF0DBBC8EAE816BCECF4EC7A76977D96E2A573CFEF7FD739F420510C502B7DCA708F47D2FA40F75BD7C47F4DB1B2807EB92453FEE9F4542D9F";
    final String ivSpec = "v7AxZs3hNRQUz44a";//763741785a7333684e5251557a343461
    final String pass = "Yjz7BRR4Rp0WJfV8";//596a7a3742525234527030574a665638

    byte[] decode = parseHexStr2Byte(content);
    byte[] ivSpecB = parseHexStr2Byte(ivSpec);
    byte[] passB = parseHexStr2Byte(pass);

System.out.println();
    byte[] decryptResult= new byte[0];

    try {
      decryptResult = decrypt(decode,pass,ivSpec);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    } catch (BadPaddingException e) {
      throw new RuntimeException(e);
    } catch (IllegalBlockSizeException e) {
      throw new RuntimeException(e);
    } catch (InvalidKeyException e) {
      throw new RuntimeException(e);
    } catch (NoSuchPaddingException e) {
      throw new RuntimeException(e);
    }
    String data = new String(decryptResult != null ? decryptResult : new byte[0], StandardCharsets.UTF_8);
    System.out.println(data);
  }

  public static String parseByte2HexStr(byte buf[]) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < buf.length; i++) {
      String hex = Integer.toHexString(buf[i] & 0xFF);
      if (hex.length() == 1) {
        hex = '0' + hex;
      }
      sb.append(hex.toUpperCase());
    }
    return sb.toString();
  }

  /**
   * 将16进制转换为二进制
   *
   * @param hexStr
   * @return
   */
  public static byte[] parseHexStr2Byte(String hexStr) {
    if (hexStr.length() < 1)
      return null;
    byte[] result = new byte[hexStr.length() / 2];
    for (int i = 0; i < hexStr.length() / 2; i++) {
      int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
      int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
      result[i] = (byte) (high * 16 + low);
    }
    return result;
  }

  public static byte[] decrypt(byte[] content, String key,String ivSpec) throws NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException {
    try {
//            KeyGenerator kgen = KeyGenerator.getInstance("AES");
//            kgen.init(128, new SecureRandom(message.getBytes()));
//            SecretKey secretKey = kgen.generateKey();
//            byte[] enCodeFormat = secretKey.getEncoded();
//            SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
      IvParameterSpec iv = new IvParameterSpec(ivSpec.getBytes(StandardCharsets.UTF_8));
      SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");// 创建密码器
      cipher.init(Cipher.DECRYPT_MODE, keySpec, iv);// 初始化
      return cipher.doFinal(content); // 加密
    } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException |
             NoSuchPaddingException e) {
      e.printStackTrace();
      throw e;
    } catch (InvalidAlgorithmParameterException e) {
      throw new RuntimeException(e);
    }
  }
}
