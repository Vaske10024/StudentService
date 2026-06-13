package org.raflab.studsluzba.utils;

public class ParseUtils {


    public static String[] parseIndeks(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() < 3) return null;


        s = s.replace("-", "").toUpperCase();


        int i = 0;
        while (i < s.length() && Character.isLetter(s.charAt(i))) {
            i++;
        }
        if (i == 0) return null; // nema programa
        if (i >= s.length()) return null;

        String program = s.substring(0, i).toLowerCase();
        String digits = s.substring(i);
        if (!digits.chars().allMatch(Character::isDigit)) return null;
        if (digits.length() < 3) return null;

        String godina2 = digits.substring(0, 2);
        String broj = digits.substring(2);

        return new String[]{ program, godina2, broj };
    }


    public static String[] parseEmail(String email) {
        if (email == null) return null;

        String s = email.trim().toLowerCase();
        if (!s.endsWith("@raf.rs")) return null;

        int at = s.indexOf('@');
        if (at <= 0) return null;

        String local = s.substring(0, at);
        if (local.isEmpty()) return null;


        int i = local.length() - 1;
        while (i >= 0 && Character.isLetter(local.charAt(i))) {
            i--;
        }
        if (i == local.length() - 1) return null;

        String program = local.substring(i + 1).toLowerCase();


        String digits = local.substring(0, i + 1);
        if (!digits.chars().allMatch(Character::isDigit)) return null;
        if (digits.length() < 3) return null;

        String godina2 = digits.substring(0, 2);
        String broj = digits.substring(2);

        return new String[]{ program, godina2, broj };
    }
}
