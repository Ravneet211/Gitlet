import java.io.File;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.CopyOption;
import java.text.SimpleDateFormat;
import static java.nio.file.StandardCopyOption.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;

public class Node implements Serializable {
	HashMap<String, Integer> filelastmodified = new HashMap<String, Integer>(); 
	String message;
	int id;
	Node prev;
	Date created;
	public Node() {
		id = 0;
		message = "initial commit";
		prev = null;
		created = new Date();
	}
	public Node(int i, String mess, Node prev_head, Set<String> staged, Set<String> removed, HashMap<String, Integer> mod_nums) {
		id = i;
		message = mess;
		prev = prev_head;
		created = new Date();
		Set<String> prev_files = prev.fileList();
		Set<String> allfiles = new HashSet<String>(prev_files);
		allfiles.addAll(staged);
		int dotindex;
		File gitletfile;
		for (String s : allfiles) {
			dotindex = s.indexOf(".");
		try {
			if (!removed.contains(s)) {
				if(!staged.contains(s)) {
					filelastmodified.put(s, prev_head.filelastmodified.get(s));
				}
				else {
					String directory = ".gitlet/";
					int slashindex;
					String temp = s;
					while(temp.contains("/")) {
						slashindex = temp.indexOf("/");
						directory += temp.substring(0,slashindex);
						new File(directory).mkdir();
						directory += "/";
						if(slashindex != temp.length()) {
							temp = temp.substring(slashindex + 1);
						}
						slashindex = 0;
					} // directory creation
					if(!prev_files.contains(s)) {
						Integer id;
						if(mod_nums.containsKey(s)){
							id = mod_nums.get(s);
						}
						else {
							id = 0;
						}
						filelastmodified.put(s,id);
						gitletfile = new File(".gitlet/"+s.substring(0,dotindex)+id.toString()+s.substring(dotindex));
					}
					else {
						Integer fileID = mod_nums.get(s);
						filelastmodified.put(s,fileID);
						gitletfile = new File(".gitlet/"+s.substring(0,dotindex)+fileID.toString()+s.substring(dotindex));
					}
					Files.copy(new File(s).toPath(),gitletfile.toPath());
				}
			}
		}
		catch(IOException e) {
				System.out.println(e);
			}
		catch(RuntimeException r) {
			System.out.println(r);
			}
		}
	}
	public Node(Node n, HashMap<String, Integer> propogated) {
		message = n.message;
		created = new Date();
		filelastmodified = new HashMap<String,Integer>(n.filelastmodified);
		filelastmodified.putAll(propogated);
		prev = null;
	}
		
		public void setPrev(Node n) {
			prev = n;
		}
		public void setMessage( String m) {
			message = m;
		}
		public int getID(){
			return id;
		}
		public boolean containsFile(String filename){
			return filelastmodified.containsKey(filename);
		}
		public File get(String filename) { 
			int dotindex = filename.indexOf(".");
			return new File(".gitlet/" + filename.substring(0,dotindex) + filelastmodified.get(filename).toString()+filename.substring(dotindex));
		}
		public Set<String> fileList() {
			return filelastmodified.keySet();
		}
		public Node prev() {
			return prev;
		}
		public void printDetails() {
			System.out.println("====");
			System.out.println("Commit " + id +".");
			System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(created));
			System.out.println(message);
			System.out.println();
			System.out.println();
		}
		public static boolean modified (Node future, Node previous, String filename) {
			if(!future.containsFile(filename)) {
				return false;
			}
			if(!previous.containsFile(filename)) {
				return true;
			}
			if (future.get(filename).equals(previous.get(filename))) {
				return false;
			}
			return true;
		}
		public void setID(int i) {
			id = i;
		}
	}