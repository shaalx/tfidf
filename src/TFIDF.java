//package tfidf;

import java.io.*;
import java.util.*;

import org.wltea.analyzer.lucene.IKAnalyzer;

public class TFIDF {

    /**
     * @param args
     */    
    private ArrayList<String> FileList = new ArrayList<String>(); // the list of file
    public TFIDF TFIDF_V; 
    public TFIDF(){
    	System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }
    public TFIDF getTFIDF(){
    	if (this.TFIDF_V == null) {
        	this.TFIDF_V = this;
		}
    	return this.TFIDF_V;
    }
    //get list of file for the directory, including sub-directory of it
    public  List<String> readDirs(String filepath) throws FileNotFoundException, IOException
    {
        try
        {
            File file = new File(filepath);
            if(!file.isDirectory())
            {
                System.out.println("输入的[]");
                System.out.println("filepath:" + file.getAbsolutePath());
            }
            else
            {
                String[] flist = file.list();
                for(int i = 0; i < flist.length; i++)
                {
                    File newfile = new File(filepath + "\\" + flist[i]);
                    if(!newfile.isDirectory())
                    {
                        FileList.add(newfile.getAbsolutePath());
                    }
                    else if(newfile.isDirectory()) //if file is a directory, call ReadDirs
                    {
                        readDirs(filepath + "\\" + flist[i]);
                    }                    
                }
            }
        }catch(FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        return FileList;
    }
    
    //read file
    public  String readFile(String file) throws FileNotFoundException, IOException
    {
        StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(file), "utf-8"); //byte streams to character streams
        BufferedReader br = new BufferedReader(inStrR); 
        String line = br.readLine();
        while(line != null){
            strSb.append(line).append("\r\n");
            line = br.readLine();    
        }
        
        return strSb.toString();
    }
    
    //write file
    public  void writeFile(String dir, String file,ArrayList<String> content) throws FileNotFoundException, IOException
    {
       StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
       File f = new File(file);
       File path =new File(dir);
       File dirs=new File(path, f.getName());
       if(!dirs.exists()){
    	   dirs.createNewFile(); 
	   }
        FileOutputStream FILE = new FileOutputStream(dir + "\\" + f.getName());        
        OutputStreamWriter outStrW = new OutputStreamWriter(FILE, "utf-8"); //byte streams to character streams
        for(String it : content){
            outStrW.write(it);
            outStrW.flush();
        }
        outStrW.close();
    }
    
    //word segmentation
    public  ArrayList<String> cutWords(String file) throws IOException{
        
        ArrayList<String> words = new ArrayList<String>();
//        String text = TFIDF_V.readFile(file);
////        String[] result = text.split("\t");
////        for(int i =0;i<result.length;i++){
////            words.add(result[i]);
////        }
//        IKAnalyzer analyzer = new IKAnalyzer();
//        words = analyzer.split(text);
        words = (ArrayList<String>) getTerms(file);
        
        return words;
    }
    //    normal tf of file
    public  HashMap<String, Integer> normalTFFile(String file) throws IOException{
        ArrayList<String> words = cutWords(file);
        HashMap<String, Integer> resTF = normalTF(words);
        return resTF;
    }
    //term frequency in a file, times for each word
    public  HashMap<String, Integer> normalTF(ArrayList<String> cutwords){
        HashMap<String, Integer> resTF = new HashMap<String, Integer>();
        
        for(String word : cutwords){
            if(resTF.get(word) == null){
                resTF.put(word, 1);
//                System.out.println(word);
            }
            else{
                resTF.put(word, resTF.get(word) + 1);
//                System.out.println(word.toString());
            }
        }
        return resTF;
    }
    
    //    tf of file
    public  HashMap<String, Float> tfFile(String file) throws IOException{
        ArrayList<String> words = cutWords(file);
        HashMap<String, Float> resTF = tf(words);
        return resTF;
    }
    //term frequency in a file, frequency of each word
    public  HashMap<String, Float> tf(ArrayList<String> cutwords){
        HashMap<String, Float> resTF = new HashMap<String, Float>();
        
        int wordLen = cutwords.size();
        HashMap<String, Integer> intTF = TFIDF_V.normalTF(cutwords); 
        
        Iterator iter = intTF.entrySet().iterator(); //iterator for that get from TF
        while(iter.hasNext()){
            Map.Entry entry = (Map.Entry)iter.next();
            resTF.put(entry.getKey().toString(), Float.parseFloat(entry.getValue().toString()) / wordLen);
//            System.out.println(entry.getKey().toString() + " = "+  Float.parseFloat(entry.getValue().toString()) / wordLen);
        }
        return resTF;
    } 
    
    //tf times for file
    public  HashMap<String, HashMap<String, Integer>> normalTFAllFiles(String dirc) throws IOException{
        HashMap<String, HashMap<String, Integer>> allNormalTF = new HashMap<String, HashMap<String,Integer>>();
        
        List<String> filelist = TFIDF_V.readDirs(dirc);
        for(String file : filelist){
            HashMap<String, Integer> dict = new HashMap<String, Integer>();
            ArrayList<String> cutwords = TFIDF_V.cutWords(file); //get cut word for one file
            
            dict = TFIDF_V.normalTF(cutwords);
            allNormalTF.put(file, dict);
        }    
        return allNormalTF;
    }
    
    //tf for all file
    public  HashMap<String,HashMap<String, Float>> tfAllFiles(String dirc) throws IOException{
        HashMap<String, HashMap<String, Float>> allTF = new HashMap<String, HashMap<String, Float>>();
        List<String> filelist = TFIDF_V.readDirs(dirc);
        
        for(String file : filelist){
            HashMap<String, Float> dict = new HashMap<String, Float>();
            ArrayList<String> cutwords = TFIDF_V.cutWords(file); //get cut words for one file
            
            dict = TFIDF_V.tf(cutwords);
            allTF.put(file, dict);
        }
        return allTF;
    }
    public  HashMap<String, Float> idf(HashMap<String,HashMap<String, Float>> all_tf){
        HashMap<String, Float> resIdf = new HashMap<String, Float>();
        HashMap<String, Integer> dict = new HashMap<String, Integer>();
        int docNum = FileList.size();
        
        for(int i = 0; i < docNum; i++){
            HashMap<String, Float> temp = all_tf.get(FileList.get(i));
            Iterator iter = temp.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry)iter.next();
                String word = entry.getKey().toString();
                if(dict.get(word) == null){
                    dict.put(word, 1);
                }else {
                    dict.put(word, dict.get(word) + 1);
                }
            }
        }
//        System.out.println("IDF for every word is:");
        Iterator iter_dict = dict.entrySet().iterator();
        while(iter_dict.hasNext()){
            Map.Entry entry = (Map.Entry)iter_dict.next();
            float value = (float)Math.log(docNum / Float.parseFloat(entry.getValue().toString()));
            resIdf.put(entry.getKey().toString(), value);
//            System.out.println(entry.getKey().toString() + " = " + value);
        }
        return resIdf;
    }
    public HashMap<String, List<Map.Entry<String, Float>>> tf_idf(HashMap<String,HashMap<String, Float>> all_tf,HashMap<String, Float> idfs){
        HashMap<String, List<Map.Entry<String, Float>>> resTfIdf = new HashMap<String, List<Map.Entry<String, Float>>>();
            
        int docNum = FileList.size();
        for(int i = 0; i < docNum; i++){
            String filepath = FileList.get(i);
            HashMap<String, Float> tfidf = new HashMap<String, Float>();
            HashMap<String, Float> temp = all_tf.get(filepath);
            Iterator iter = temp.entrySet().iterator();
            while(iter.hasNext()){
                Map.Entry entry = (Map.Entry)iter.next();
                String word = entry.getKey().toString();
                Float value = (float)Float.parseFloat(entry.getValue().toString()) * idfs.get(word); 
                tfidf.put(word, value);
            }
            List<Map.Entry<String, Float>> ftidfEntryList = new ArrayList<Map.Entry<String, Float>>(tfidf.entrySet());
//            System.out.println(ftidfEntryList);
            Collections.sort(ftidfEntryList, new Comparator<Map.Entry<String, Float>>()
            {
                @Override
                public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
                {
                    return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
                }
            });
            System.out.println(ftidfEntryList);
            
            resTfIdf.put(filepath, ftidfEntryList);
        }
        System.out.println("TF-IDF for Every file is :");
        DisTfIdf(resTfIdf);
        return resTfIdf;
    }
    public  void DisTfIdf(HashMap<String, List<Map.Entry<String, Float>>> tfidf){
        Iterator iter1 = tfidf.entrySet().iterator();
        while(iter1.hasNext()){
            Map.Entry entrys = (Map.Entry)iter1.next();
            System.out.println("FileName: " + entrys.getKey().toString());
            System.out.print("{");
            List<Map.Entry<String, Float>> temp = (List<Map.Entry<String, Float>>) entrys.getValue();

            for (Map.Entry<String, Float> entry : temp) {
            	System.out.print(entry.getKey().toString() + " = " + entry.getValue().toString() + ", ");
			}

            System.out.println("}");
        }
        
    }
    
    //    tf-all saved to files
    public  void saveTfIdfAll(String dir,HashMap<String, List<Map.Entry<String, Float>>> tfidf) throws IOException{
        
        Iterator iter1 = tfidf.entrySet().iterator();
        String s = "";
        while(iter1.hasNext()){
            Map.Entry entrys = (Map.Entry)iter1.next();
            String filename = entrys.getKey().toString();
            List<Map.Entry<String, Float>> temp = (List<Map.Entry<String, Float>>) entrys.getValue();

            ArrayList<String> tfidfList = new ArrayList<String>();
            for (Map.Entry<String, Float> entry : temp) {
            	s = entry.getValue().toString() + "\t" + entry.getKey().toString() + "\n";
            	tfidfList.add(s);
			}
            writeFile(dir, filename, tfidfList);
        }
    }
    
    //    tf-all saved to files
    public  void saveTfAll(String dir,HashMap<String, List<Map.Entry<String, Float>>> tfidf) throws IOException{
        
        Iterator iter1 = tfidf.entrySet().iterator();
        String s = "";
        while(iter1.hasNext()){
            Map.Entry entrys = (Map.Entry)iter1.next();
            String filename = entrys.getKey().toString();
            List<Map.Entry<String, Float>> temp = (List<Map.Entry<String, Float>>) entrys.getValue();

            ArrayList<String> tfidfList = new ArrayList<String>();
            for (Map.Entry<String, Float> entry : temp) {
            	s = entry.getValue().toString() + "\t" + entry.getKey().toString() + "\n";
            	tfidfList.add(s);
			}
            writeFile(dir, filename, tfidfList);
        }
    }
    
    // 分词
    public List<String> getTerms(String file){
		List<String> terms = new ArrayList<String>();
        StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
        try{
	        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(file), "utf-8"); //byte streams to character streams
	        BufferedReader br = new BufferedReader(inStrR); 
	        String line = br.readLine();
	        String[] spilts = line.split("\t");
	        for (String it : spilts) {
	        	terms.add(it);
			}
	        inStrR.close();
       	}catch(Exception e){}
		return terms;
	}
    // 关键字
    public List<String> getKeys(String file){
		List<String> terms = new ArrayList<String>();
        StringBuffer strSb = new StringBuffer(); //String is constant， StringBuffer can be changed.
        try{
	        InputStreamReader inStrR = new InputStreamReader(new FileInputStream(file), "utf-8"); //byte streams to character streams
	        BufferedReader br = new BufferedReader(inStrR); 
	        String line = br.readLine();
	        String[] spilts = line.split("\t");
	        for (String it : spilts) {
	        	terms.add(it);
			}
	        inStrR.close();
       	}catch(Exception e){}
		return terms;
	}
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
    	String result_dir ="./result";
    	String tf_dir ="./tf";
    	String tf_idf_dir ="./tf_idf";
        String origin_dir = "E:\\ItemForGo\\src\\github.com\\shaalx\\sstruct\\static\\spilt\\";
        String key_dir = "E:\\ItemForGo\\src\\github.com\\shaalx\\sstruct\\static\\key\\";
        
        TFIDF TFIDF_v = new TFIDF();
        TFIDF_v.getTFIDF();
        HashMap<String,HashMap<String, Float>> all_tf = TFIDF_v.tfAllFiles(origin_dir);
        System.out.println();
        HashMap<String, Float> idfs = TFIDF_v.idf(all_tf);
        System.out.println();
        HashMap<String, List<Map.Entry<String, Float>>> all_tf_idfs = TFIDF_v.tf_idf(all_tf, idfs);
        TFIDF_v.saveTfIdfAll(tf_idf_dir,all_tf_idfs);
    }

}