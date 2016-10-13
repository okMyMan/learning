package com.infinity.codec;


import com.google.common.io.BaseEncoding;


public class Base64_16 {
    
    public static byte[] b64decode(String str){
        BaseEncoding b64 = BaseEncoding.base64();
        return b64.decode(str);
    }
    
    public static String b64encode(byte[] bytes){
        BaseEncoding b64 = BaseEncoding.base64();
        return b64.encode(bytes).replaceAll("\\+", "(").replaceAll("\\/", ")");
    }
    
    public static byte[] b16decode(String str){
        BaseEncoding b16 = BaseEncoding.base16();
        return b16.decode(str.toUpperCase());
    }
    
    public static String b16encode(byte[] bytes){
        BaseEncoding b16 = BaseEncoding.base16();
        return b16.encode(bytes).toLowerCase();
    }
    
    public static String bin_to_b64(byte[] bs){
        BaseEncoding b64=BaseEncoding.base64();
        String result = b64.encode(bs).replaceAll("=", "");
        result=result.replaceAll("\\+", "(").replaceAll("\\/", ")");
        return result;
    }

    
    public static String b64_to_b16(String str){
        BaseEncoding b16 = BaseEncoding.base16();
        if(str.startsWith("@")){
            String start = str.substring(1, 2);
            String result = b16.encode(b64_to_bin(str.substring(2))).toLowerCase();
            result = start + result;
            return result;
        }else{
            String result = b16.encode(b64_to_bin(str)).toLowerCase();
            return result;
        }
    }
    
    public static byte[] b64_to_bin(String str){
        BaseEncoding b64=BaseEncoding.base64();
        int slen = str.length();
        int tail = slen % 4;
        for(int i=0;i<tail;i++)
            str+="=";
        str = str.replaceAll("\\(", "+").replaceAll("\\)", "/");
        return b64.decode(str);
    }
    
    
    public static String b16_to_b64(String str){
        BaseEncoding b64=BaseEncoding.base64();
        BaseEncoding b16=BaseEncoding.base16();
        if(str.length()%2==0){
            byte[]  bs = b16.decode(str.toUpperCase());
            String result= b64.encode(bs).replaceAll("=", "");
            result = result.replaceAll("\\+", "(").replaceAll("\\/", ")");
            return result;
        }else{
            String start="@"+str.substring(0,1);
            byte[]  bs = b16.decode(str.substring(1).toUpperCase());
            String result= b64.encode(bs).replaceAll("=", "");
            result=start+result.replaceAll("\\+", "(").replaceAll("\\/", ")");
            return result;
        }
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
       String s ="1001:59";
       String a= Base64_16.b16encode(ARC4Util.encode("group_name".getBytes(), s.getBytes()));
       System.out.println(a);
       System.out.println(new String(ARC4Util.decode("group_name".getBytes(), Base64_16.b16decode(a))));
       
//     byte[] bytes=Base64_16.b64decode("004006055A001E9B91CC917288B9E7F69AA253B898BC6600");
//     System.out.println(Base64_16.bin_to_b64(bytes));
    
//     byte[] bs= DigestUtils.md5("aaa" + "pP2SMbwjK6V+LHqdZiKD");
//     BaseEncoding b16 = BaseEncoding.base16();
//     String s = b16.encode(bs).toLowerCase();
//     System.out.println(s);
//     
//     BaseEncoding b32 = BaseEncoding.base32();
//     s=b32.encode(bs).toLowerCase();
//     System.out.println(s);
//     //ls_8q6xuoiotw_ok-92edw== 
//     BaseEncoding b64 = BaseEncoding.base64();
//     s=b64.encode(bs).toLowerCase();
//     System.out.println(s);
//     
//     
//       String str1=Base64_16.b16_to_b64("b96333f961ad41d0a67c4e85977d2a94");
//       System.out.println(str1);
//       String str2=Base64_16.b64_to_b16(str1);
//       System.out.println(str2);
//       String str3=Base64_16.bin_to_b64("http://127.0.0.1/wss".getBytes());
//       System.out.println(str3);
//       String str4=new String(Base64_16.b64_to_bin(str3));
//       System.out.println(str4);
//       System.out.println(System.currentTimeMillis()/1000);
//str1 = util.Base64_16.b16_to_b64("b96333f961ad41d0a67c4e85977d2a941")
//str2 = util.Base64_16.b64_to_b16(str1)
//str3 = util.Base64_16.bin_to_b64("http://127.0.0.1/wss")
//str4 = util.Base64_16.b64_to_bin(str3)
    }

}
