package com.sohu.mrd.sonlp.core;

import java.io.*;
import java.util.*;

/**
 * Created by huangyu on 15/12/20.
 */
public class Library {
    public static Map<String, Boolean> stopmap = new HashMap<String, Boolean>();

    public static Map<String, Boolean> whitemap = new HashMap<String, Boolean>();

    public static Map<String, String> sameword = new HashMap<String, String>();

    public static Map<String, Double> idfmap = new HashMap<String, Double>();
    private static Properties conf = new Properties();

    static {
        try {
            conf.load(Library.class.getClassLoader().getResourceAsStream("library.properties"));
            inputStopword();
            System.err.println("input stop word done!");
            inputWhiteword();
            System.err.println("input white word done!");
            inputSameword();
            System.err.println("input same word done!");
            inputIdfdict();
            System.err.println("input idf dict done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFile(String file) {
        String root = conf.getProperty("sonlpLibrary");
        root = root.endsWith("/") ? root : root + "/";
        return root + file;
    }

    private static void inputStopword() throws IOException {
        File f = new File(getFile("stop_word"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            stopmap.put(line, true);
        }
        br.close();
    }

    private static void inputIdfdict() throws IOException {
        File f = new File(getFile("idf_dict"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] outline = line.split(",");
            idfmap.put(outline[0], Double.valueOf(outline[1]));
        }
        br.close();
    }

    private static void inputWhiteword() throws IOException {
        File f = new File(getFile("white_word"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            whitemap.put(line, true);
        }
        br.close();
    }

    private static void inputSameword() throws IOException {
        File f = new File(getFile("same_word"));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
        String line = "";
        while ((line = br.readLine()) != null) {
            String[] outline = line.split("\t");
            sameword.put(outline[0], outline[1]);
        }
        br.close();
    }

    public static void main(String[] args) {
        Library.idfmap.get("");
    }

}
