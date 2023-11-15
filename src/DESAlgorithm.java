import java.nio.charset.StandardCharsets;

public class DESAlgorithm {

    Data data = new Data();

    int[] PC1 = data.getPC1();
    int[] PC2 = data.getPC2();
    int[] IP = data.getIP();

    int[] IP_1 = data.getIP1();
    int[] move = data.getLeftMove();

    int[][][] sBox = data.getSBox();

    int[] eBox = data.getE();
    int[] pBox = data.getP();
    public DESAlgorithm() {
    }


    public int[] PC_1(int[] key){
        // 利用交换规则表PC_1将64位的K转换成56位的K+
        int[] K0 = new int[56];
        int i = 0;
        while (i < 56){
            K0[i] = key[PC1[i] - 1];
            i = i + 1;
        }
        return K0;
    }
    public int[] PC_2(int[] key){
        // 利用交换规则表PC_2将左移后的两个子序列拼出的完整序列转换成子密钥
        int[] K0 = new int[48];
        int i = 0;
        while (i < 48){
            K0[i] = key[PC2[i] - 1];
            i = i + 1;
        }
        return K0;
    }

    public int[] IP(int[] key){
        // 利用交换规则表IP将明文加密
        int[] K0 = new int[64];
        int i = 0;
        while (i < 64){
            K0[i] = key[IP[i] - 1];
            i = i + 1;
        }
        return K0;
    }

    public int[] IP_1(int[] key){
        // 利用交换规则表IP将明文加密
        int[] K0 = new int[64];
        int i = 0;
        while (i < 64){
            K0[i] = key[IP_1[i] - 1];
            i = i + 1;
        }
        return K0;
    }

    public int[] E(int[] key){
        // 利用扩展置换表E将明文加密后的右半部分扩展到48位
        int[] K0 = new int[48];
        int i = 0;
        while (i < 48){
            K0[i] = key[eBox[i] - 1];
            i = i + 1;
        }
        return K0;
    }

    public int[] P(int[] key){
        // 利用扩展置换表E将明文加密后的右半部分扩展到48位
        int[] K0 = new int[32];
        int i = 0;
        while (i < 32){
            K0[i] = key[pBox[i] - 1];
            i = i + 1;
        }
        return K0;
    }

    public int[][] getSubKey(int[] K0){
        // 获得子密钥

        int [][] sub_key = new int[16][48];
        // 子密钥集合

        int[] C0 = new int[28];
        int[] D0 = new int[28];
        // 经过PC_1处理后的K+平分出的两个子序列

        int[][] C = new int[16][28];
        int[][] D = new int[16][28];
        // 左移后的两个子序列合集

        for (int i = 0; i < 28; i++) {
        // 分离K+得到两个子序列
            C0[i] = K0[i];
            D0[i] = K0[i + 28];
        }

        int i = 0;
        while (i < 16){
            // 对所有子序列进行左移操作
            if (i == 0){
                // 第一个序列由原始序列左移而成
                C[i] = moveLeft(C0, move[i]);
                D[i] = moveLeft(D0, move[i]);
            }else {
                // 之后每一个序列都由前一个序列左移而成
                C[i] = moveLeft(C[i - 1], move[i]);
                D[i] = moveLeft(D[i - 1], move[i]);
            }
            i = i + 1;
        }

        int j = 0;
        while (j < sub_key.length){
            //合并子序列后用PC_2交换规则处理获得子密钥
            sub_key[j] = PC_2(merge(C[j], D[j]));
            j = j + 1;
        }
        return sub_key;

    }

    public int[] moveLeft(int[] k, int p){
        // 数组左移操作
        // k: 操作数组    p: 左移的位数

        int[] k1 = new int[k.length];
        // 左移后的序列

        if (p < k.length) {
            // 假设左移的位数不超过序列长度
            int i = 0;
            while (i < k.length - p) {
                // 将原序列左移位数之后的所有数字直接复制到新序列开头
                k1[i] = k[i + p];
                i = i + 1;
            }
            int j = 0;
            while (j < p) {
                // 将原序列左移位数之前的所有数字直接复制到新序列结尾
                k1[i] = k[j];
                j = j + 1;
                i = i + 1;
            }
            return k1;
        }else {
            // 若超过序列长度则返回原数组
            return k;
        }
    }

    public int[] merge(int[] a, int[] b){
        // 合并两个数组
        int[] result = new int[a.length + b.length];
        int i = 0;
        while (i < a.length){
            result[i] = a[i];
            i = i + 1;
        }
        int j = 0;
        while (j < b.length){
            result[i] = b[i - a.length];
            j = j + 1;
            i = i + 1;
        }
        return result;
    }

    public int[][] split(int[] a){
        // 将数组等分成两半
        int[] result1 = new int[a.length/2];
        int[] result2 = new int[a.length/2];
        int i = 0;
        while (i < a.length){
            if (i < a.length/2){
                result1[i] = a[i];
            }else {
                result2[i - a.length/2] = a[i];
            }
            i = i + 1;
        }
        int [][] ret = new int[2][a.length/2];
        ret[0] = result1;
        ret[1] = result2;
        // 返回的结果中第一个元素为前半个数组，第二个为后半个数组
        return ret;

    }

    public int[][] split_8(int[] a){
        // 将48位结果分成8个6位的块
        int[][] result = new int[8][6];
        int i = 0;
        int j = -1;
        int k = 0;

        while (i < a.length){
            if (i % 6 == 0){
                j = j + 1;
                k = 0;
            }
            result[j][k] = a[i];
            k = k + 1;
            i = i + 1;
        }

        return result;

    }

    public int[] Xor (int[] a, int[] b){
        // 异或操作
        int [] result = new int[a.length];
        int i = 0;
        while (i < a.length){
            if (a[i] == b[i]){
                result[i] = 0;
            }else {
                result[i] = 1;
            }
            i = i + 1;
        }
        return result;
    }

    public int[] Xor_P(int[] a, int[] b){
        // a: S盒结果 b: L(n-1)
        int[] p = P(a);
        return Xor(b, p);

    }

    public int BinaryToInt(String a){
        // 将二进制01序列形式的字符串转换为十进制数
        int result = 0;

        int s = 1;// 2的n次幂
        int j = 0;
        while (j < a.length() - 1){
            s = s * 2;
            j = j + 1;
        }
        int i = 0;
        while (i < a.length()){
            if (a.charAt(i) == '1') {
                result = result + s;
            }
            s = s / 2;
            i = i + 1;
        }
        return result;
    }

    public int[] getSBoxPosition(int[] a){
        // 找到6位块对应S盒中的坐标
        int[] result = new int[2];
        String PositionX = "";
        StringBuilder PositionY = new StringBuilder();
        PositionX = String.valueOf(a[0]) + String.valueOf(a[a.length - 1]);
        // 以数组中第一个和最后一个数字组合的二进制转化成十进制作为X坐标
        int i = 1;
        while (i < a.length - 1){
            // 以数组中中间四位数组合的二进制转换为十进制作为Y坐标
            PositionY.append(String.valueOf(a[i]));
            i = i + 1;
        }
        result[0] = BinaryToInt(PositionX);
        result[1] = BinaryToInt(String.valueOf(PositionY));
        // 返回的数组中第一个元素为x坐标，第二个为y坐标
        return result;
    }

    public String intToBinary(int a){
        // 将整数转化为二进制序列

        String binary = "";

        while (a > 0) {
            int remainder = a % 2;
            binary = remainder + binary;
            a = a / 2;
        }

        return binary;
    }

    public int[] getSBoxOutput(int[] a){
        // 获得异或结果经过S盒替换之后的结果
        // 参数： 异或结果
        int[][] split = split_8(a);
        String binaryStr = "";
        int[] result = new int[32];
        int i = 0;
        int j = 0;
        while (i < split.length){
            int[] position = getSBoxPosition(split[i]);
            // 获取S盒坐标
            int sBoxResult = sBox[i][position[0]][position[1]];
            String binary = intToBinary( sBoxResult );

            //补足4位
            if (binary.length() == 3){
                binary = "0" + binary;
            }else if (binary.length() == 2){
                binary = "00" + binary;
            }else if (binary.length() == 1){
                binary = "000" + binary;
            }else if (binary.length() == 0){
                binary = "0000";
            }

            i = i + 1;
            while (j < binary.length()){
                // 拼接
                binaryStr = binaryStr + String.valueOf(binary.charAt(j));
                j = j + 1;
            }
            j = 0;
        }

        int k = 0;
        while (k < binaryStr.length()){
            // 将字符串放入数组
            result[k] = Integer.parseInt(String.valueOf(binaryStr.charAt(k)));
            k = k + 1;
        }
        return result;
    }

    public int[] encryption(int[] m, int[] k){
        // m: 明文 k: 密钥

        int[] M = IP(m);
        // 经过IP处理后的明文
        int[][] K = getSubKey(PC_1(k));
        // 子密钥合集
//        for (int[] i : K){
//            System.out.println(Arrays.toString(i));
//        }
//        System.out.println(Arrays.deepToString(K));
        int[][] L = new int[17][32];
        int[][] R = new int[17][32];
        // 储存每一轮的 L和R

        L[0] = split(M)[0];
        // IP处理的明文的左半部分L
        R[0] = split(M)[1];
        // 右半部分R

        int i = 0;
        while (i < 16){
//            System.out.println("loop no."+i);
            int[] E = E(R[i]);
            // 对当前循环到的R做E盒拓展
//            System.out.println("E BOX:");
//            System.out.println(Arrays.toString(E));
            int[] Xor = Xor(E, K[i]);
            // 将拓展结果与对应次序的子密钥做异或处理
//            System.out.println("Xor");
//            System.out.println(Arrays.toString(Xor));
            int[] S = getSBoxOutput(Xor);
            // 将异或的结果通过S盒转换成32位
//            System.out.println("S BOX");
//            System.out.println(Arrays.toString(S));
            int[] P = P(S);
            // 将S盒输出通过P盒置换再转换成一个32位结果
//            System.out.println("P BOX");
//            System.out.println(Arrays.toString(P));
            R[i + 1] = Xor(L[i], P);
            // 下一轮的R就是本轮的L与P盒输出的异或
//            System.out.println("R n+1:");
//            System.out.println(Arrays.toString(R[i + 1]));
            L[i + 1] = R[i];
            // 下一轮的L就是本轮的R

            i = i + 1;
        }

        int[] newK = merge(R[16],L[16]);

        // 将最后一轮的R和L拼起来并进行IP_1变换就是最终的二进制密文
        return  IP_1(newK);
    }

    public int[] dencryption(int[] m, int[] k){
        // m: 密文 k: 密钥
//        System.out.println(Arrays.toString(m));
        int[] M = IP(m);
        // 经过IP处理后的密文
        int[][] K = getSubKey(PC_1(k));
        // 逆置子密钥合集
        int a = 0;
        int b = 15;
        int[][] Kr = new int[16][48];// 倒置的子密钥合集
        while (a < K.length){
            Kr[a] = K[b];
            a = a + 1;
            b = b - 1;
        }


        int[][] L = new int[17][32];
        int[][] R = new int[17][32];
        // 储存每一轮的 L和R


        L[0] = split(M)[0];
        // IP处理的密文的左半部分L
        R[0] = split(M)[1];
        // 右半部分R

        int i = 0;
        while (i < 16){
//            System.out.println("Loop no." + i);
//            System.out.println(Arrays.toString(R[i]));
            int[] E = E(R[i]);
            // 对当前循环到的R做E盒拓展

            int[] Xor = Xor(E, Kr[i]);
            // 将拓展结果与对应次序的子密钥做异或处理

            int[] S = getSBoxOutput(Xor);
            // 将异或的结果通过S盒转换成32位

            int[] P = P(S);
            // 将S盒输出通过P盒置换再转换成一个32位结果

            R[i + 1] = Xor(L[i], P);
            // 下一轮的R就是本轮的L与P盒输出的异或

            L[i + 1] = R[i];
            // 下一轮的L就是本轮的R

            i = i + 1;
        }

        int[] newK = merge(R[16],L[16]);

        // 将最后一轮的R和L拼起来并进行IP_1变换就是最终的二进制明文
        return  IP_1(newK);
    }

    public int[] ByteToBinary(byte[] bytes) {
        //  ASCII ---> 二进制
        int i;
        int j;
        int[] binary = new int[8];
        for (i = 0; i < 8; i++) {
            binary[i] = bytes[i];
            if (binary[i] < 0) {
                binary[i] += 256;
                binary[i] %= 256;
            }
        }
        int[] result = new int[64];
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                result[((i * 8) + 7) - j] = binary[i] % 2;
                binary[i] = binary[i] / 2;
            }
        }
        return result;
    }
    public byte[] BinaryToByte(int[] binary) {
        //  二进制 ---> ASCII
        byte[] result = new byte[8];
        int i, j;
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {

                result[i] += (binary[(i << 3) + j] << (7 - j));
            }
        }
        for (i = 0; i < 8; i++) {
            result[i] %= 256;
            if (result[i] > 128) {
                result[i] -= 255;
            }
        }
        return result;
    }
    public static String convertToHex(String binary) {
        // 二进制 --> 十六进制
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
        // 十六进制 --> 二进制
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
    public static void main(String[] args) {
//      测试代码
        int [] key = { // 密钥
                0,0,0,1,0,0,1,1,
                0,0,1,1,0,1,0,0,
                0,1,0,1,0,1,1,1,
                0,1,1,1,1,0,0,1,
                1,0,0,1,1,0,1,1,
                1,0,1,1,1,1,0,0,
                1,1,0,1,1,1,1,1,
                1,1,1,1,0,0,0,1
        };

        int[] m = { //明文0123456789ABCDEF
                0,0,0,0,
                0,0,0,1,
                0,0,1,0,
                0,0,1,1,
                0,1,0,0,
                0,1,0,1,
                0,1,1,0,
                0,1,1,1,
                1,0,0,0,
                1,0,0,1,
                1,0,1,0,
                1,0,1,1,
                1,1,0,0,
                1,1,0,1,
                1,1,1,0,
                1,1,1,1
        };

        int[] secretText = {1, 0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 1, 0, 1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 0, 0, 1, 0, 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1

        };

        DESAlgorithm d = new DESAlgorithm();

//        System.out.println(Arrays.toString(d.PC_1(key)));

        int[] a = {1,2,3,4,5,6,7,8,9};
//        System.out.println(Arrays.toString(d.moveLeft(a, 2)));
//
//        System.out.println(Arrays.deepToString(d.getSubKey(d.PC_1(key))));
//
//        System.out.println(Arrays.toString(d.IP(m)));
//
//         int[] b = {0, 1, 1, 0, 0, 0};
//        System.out.println(Arrays.toString(d.split(d.IP(m))[1]));
//
//        System.out.println(Arrays.toString(d.E(d.split(d.IP(m))[1])));
//
//        int[] yihuojieguo = d.Xor(d.E(d.split(d.IP(m))[1]), d.getSubKey(d.PC_1(key))[0]);
//        System.out.println(Arrays.toString(yihuojieguo));
//
//        System.out.println(Arrays.deepToString(d.split_8(yihuojieguo)));
//
//        System.out.println(d.BinaryToInt("1011"));
//
////        System.out.println(Arrays.toString(d.getSBoxPosition(b)));
//
//        System.out.println(Arrays.toString(d.getSBoxOutput(yihuojieguo)));

//        java.lang.System.out.println(Arrays.toString(d.encryption(m, key)));
//        java.lang.System.out.println(Arrays.toString(d.dencryption(secretText, key)));
        String a1 = "012345671";
        String k1 = "133457799BBCDFF1";
//        String s = "10000000";
//        System.out.println(Arrays.toString(d.ByteToBinary(s.getBytes(StandardCharsets.UTF_8))));
//        System.out.println("密钥二进制");
//        System.out.println((Arrays.toString(d.ByteToBinary( k1.getBytes(StandardCharsets.UTF_8) ))) );
//        System.out.println(Arrays.toString(d.encryption(d.ByteToBinary( a1.getBytes(StandardCharsets.UTF_8) ), key)));
//        int[] k2 = {0,0,0,0,0,0,0,0};
        int[] e = d.encryption(d.ByteToBinary(a1.getBytes(StandardCharsets.UTF_8)), d.ByteToBinary(k1.getBytes(StandardCharsets.UTF_8)));
//        System.out.println(Arrays.toString(e));
//        System.out.println("ddddd:");
//        System.out.println(Arrays.toString(d.dencryption(e, d.ByteToBinary(k1.getBytes(StandardCharsets.UTF_8)))));
//        String str = new String(d.BinaryToByte(e));
//        System.out.println(str);
        System.out.println("plain text: " + a1);
        System.out.println("secret key: " + k1);
        System.out.println("encrypt:");
        System.out.println(new String(d.BinaryToByte(e)));
        System.out.println("decrypt:");
        System.out.println(new String(d.BinaryToByte(d.dencryption(e, d.ByteToBinary(k1.getBytes(StandardCharsets.UTF_8))))));
//        System.out.println(Arrays.toString(d.ByteToBinary(k1.getBytes(StandardCharsets.UTF_8))));
//        System.out.println(a1.length() % 8);
//        String[] s = new String[(a1.length() % 8) == 0 ? (a1.length()/8) : (a1.length()/8) + 1];
//        s[0] = "";
//        int i = 0;
//        int j = 0;
//        while (i < a1.length()){
//            s[j] = s[j] + a1.charAt(i);
//            i ++;
//            if (i % 8 == 0){
//                j ++;
//                if (j < s.length){
//                    s[j] = "";
//                }
//            }
//        }
//        if ( s[s.length - 1].length() != 8 ){
//            int k = s[s.length - 1].length();
//            while (k < 8) {
//                s[s.length - 1] += 0;
//                k ++;
//            }
//        }
//        System.out.println(Arrays.toString(s));
//        System.out.println(Arrays.toString(d.ByteToBinary("helloooo".getBytes(StandardCharsets.UTF_8))));
//        String binaryString = "00010010";
//
//        String str = "";
//
//        for(int i : e){
//            str += i;
//        }
//
//        System.out.println(convertToHex(str));
//
//        System.out.println(convertToBinary16(convertToHex(str)));

        DesSystem ds = new DesSystem();

        String kkk = "012137654";

        System.out.println("---processKey---");
        System.out.println(ds.processKey(kkk));
        System.out.println("------");
    }
}
