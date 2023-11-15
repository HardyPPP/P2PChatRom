

import java.nio.charset.StandardCharsets;


public class DesSystem {


    public DesSystem() {
    }


    public String index(){
        // 主页
        return "index";
    }


    public String encrypt(
             String key,
             String text

    ) {
        // 开始加密
        // 参数 key: 密钥   text: 明文
        DESAlgorithm d = new DESAlgorithm();
        String[] texts = splitAndFill(text);
        // 将明文分成每块8个字节的集合
        String res = "";
        // 最终加密结果
        for (String s : texts){
            // 对于每一个明文块
            int[] encryption = d.encryption(d.ByteToBinary(s.getBytes(StandardCharsets.UTF_8)), d.ByteToBinary(processKey(key).getBytes(StandardCharsets.UTF_8)));
            // 加密明文块，获得加密后的二进制序列
            String str = "";
            for(int i : encryption){
                str += i;
            }
            res += convertToHex(str);
            // 将二进制序列转化成ASCII码序列再转化成字符串并入最终结果中
        }
        Result result = new Result(res, text, key);

        // 向前端传值
        return res;
    }


    public String decrypt(
            String key,
            String text

    ) {
        // 开始解密
        // 参数 key: 密钥   text: 密文
        DESAlgorithm d = new DESAlgorithm();
        String binaryText = convertToBinary16(text);
        // 将16进制密文转换成2进制序列
        String[] texts = splitAndFill64(binaryText);
        // 按每64个字符一组将2进制序列等分
        String res = "";// 最终加密结果
        for (String s : texts){
            // 对于每一个2进制密文块
            int[] binary = new int[s.length()] ;
            // 2进制序列数组
            int i = 0;
            String[] ss = s.split("");
            while (i < ss.length){
                // 将2进制字符串中每个字符放入2进制数组
                binary[i] = Integer.parseInt(ss[i]);
                i ++;
            }
            int[] decryption = d.dencryption(binary, d.ByteToBinary(processKey(key).getBytes(StandardCharsets.UTF_8)));
            // 解密2进制密文块，获得解密后的二进制序列
            res += new String(d.BinaryToByte(decryption));
            // 将2进制序列转化成ASCII码序列再转化成字符串并入最终结果中
        }
//        int j = res.length();
//        while (res.charAt(j - 1) == 48){
//            // 清除补位
//            j --;
//        }
//        res = res.substring(0, j);
        Result result = new Result(res, text, key);

        // 向前端传值
        return res;
    }

    public static String convertToHex(String binary) {
        StringBuilder hex = new StringBuilder();

        // 将二进制字符串按照每4位进行分割
        for (int i = 0; i < binary.length(); i += 4) {
            String binaryNibble = binary.substring(i, Math.min(i + 4, binary.length()));

            // 将每个4位二进制字符串转换为十六进制字符
            int decimal = Integer.parseInt(binaryNibble, 2);
            char hexDigit = Character.forDigit(decimal, 16);

            // 添加到结果字符串中
            hex.append(hexDigit);
        }

        return hex.toString().toUpperCase();
    }

    public static String convertToBinary16(String hex) {
        StringBuilder binary = new StringBuilder();

        // 遍历十六进制字符串中的每个字符
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);

            // 将字符转换为对应的整数值
            int decimal = Character.digit(c, 16);

            // 将整数值转换为四位二进制字符串
            String binaryByte = String.format("%4s", Integer.toBinaryString(decimal))
                    .replace(' ', '0');

            // 添加到结果字符串中
            binary.append(binaryByte);
        }

        return binary.toString();
    }

    public String[] splitAndFill(String a1){
        // 用于按照八个字符一组分割明文文本并用零补齐
        String[] s = new String[(a1.length() % 8) == 0 ? (a1.length()/8) : (a1.length()/8) + 1];
        // 初始化字符串数组，长度为初始字符对8整除或者对8整除加1
        s[0] = "";
        int i = 0;
        int j = 0;
        while (i < a1.length()){
            // 分割
            s[j] = s[j] + a1.charAt(i);
            i ++;
            if (i % 8 == 0){
                // 每8个字一组
                j ++;
                if (j < s.length){
                    s[j] = "";
                    // 初始化字符串
                }
            }
        }
        if ( s[s.length - 1].length() != 8 ){
            // 补位
            int k = s[s.length - 1].length();
            while (k < 8) {
                s[s.length - 1] += " ";
                k ++;
            }
        }
        return s;
    }
    public String[] splitAndFill64(String a1){
        // 用于按照64个字符一组分割二进制序列文本
        String[] s = new String[(a1.length() % 64) == 0 ? (a1.length()/64) : (a1.length()/64) + 1];
        // 初始化字符串数组，长度为初始字符对64整除或者对64整除加1
        s[0] = "";
        int i = 0;
        int j = 0;
        while (i < a1.length()){
            // 分割
            s[j] = s[j] + a1.charAt(i);
            i ++;
            if (i % 64 == 0){
                // 每64个字一组
                j ++;
                if (j < s.length){
                    s[j] = "";
                    // 初始化字符串
                }
            }
        }
        if ( s[s.length - 1].length() != 8 ){
            // 补位
            int k = s[s.length - 1].length();
            while (k < 8) {
                s[s.length - 1] += 0;
                k ++;
            }
        }
        return s;
    }

    public String processKey(String key){
        // 处理密钥使其长度等于8
        String newKey = key;
        if (key.length() < 8){
            int i = key.length() ;
            while (i < 8){
                newKey += 0;
                i ++;
            }
        }else if (key.length() > 8){
            newKey = newKey.substring(0, 8);
        }
        return newKey;
    }
}
