package com.zscat.mallplus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2019/7/3.
 */
public class T {
    public static void main(String[] args) {
       for (int q=0;q<10;q++){
           for (int w=0;w<5;w++){
               System.out.println("qq");
               break;
           }
       }
    }
    public static int numJewelsInStones(String J, String S) {
        return S.replaceAll("[^" + J + "]", "").length();
    }
    public static int numJewelsInStones1(String J, String S) {
        Map<Character, Integer> map = new HashMap<>();
        int count = 0;
        for(char s : S.toCharArray())
            map.put(s, map.getOrDefault(s, 0) + 1);

        for(int i = 0; i < J.length(); i++)
            count += map.getOrDefault(J.charAt(i), 0);

        return count;
    }


}
