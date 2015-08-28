  
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
import java.text.SimpleDateFormat;
import static java.nio.file.StandardCopyOption.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;

class Node implements Serializable {
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
    public Node(int i, String mess, Node prevhead, Set<String> staged, Set<String> removed, 
        HashMap<String, Integer> modnums) {
        id = i;
        message = mess;
        prev = prevhead;
        created = new Date();
        Set<String> prevfilelist = prev.fileList();
        Set<String> allfiles = new HashSet<String>(prevfilelist);
        allfiles.addAll(staged);
        int dotindex;
        File gitletfile;
        for (String s : allfiles) {
            dotindex = s.indexOf(".");
            try {
                if (!removed.contains(s)) {
                    if (!staged.contains(s)) {
                        filelastmodified.put(s, prevhead.filelastmodified.get(s));
                    } else {
                        String directory = ".gitlet/";
                        int slashindex;
                        String temp = s;
                        while (temp.contains("/")) {
                            slashindex = temp.indexOf("/");
                            directory += temp.substring(0, slashindex);
                            new File(directory).mkdir();
                            directory += "/";
                            if (slashindex != temp.length()) {
                                temp = temp.substring(slashindex + 1);
                            }
                            slashindex = 0;
                        } // directory creation
                        
                        if (!prevfilelist.contains(s)) {
                            Integer identity;
                            if (modnums.containsKey(s)) {
                                identity = modnums.get(s);
                            } else {
                                identity = 0;
                            }
                            filelastmodified.put(s, identity);
                            gitletfile = new File(".gitlet/" + s.substring(0, dotindex)
                                + identity.toString() + s.substring(dotindex));
                        } else {
                            Integer fileID = modnums.get(s);
                            filelastmodified.put(s, fileID);
                            gitletfile = new File(".gitlet/" + s.substring(0, dotindex)
                                + fileID.toString() + s.substring(dotindex));
                        }
                        Files.copy(new File(s).toPath(), gitletfile.toPath());
                    }
                }
            } catch (IOException e) {
                System.out.println(e);
            } catch (RuntimeException r) {
                System.out.println(r);
            }
        }
    }
    public Node(Node n, HashMap<String, Integer> propogated) {
        message = n.message;
        created = new Date();
        filelastmodified = new HashMap<String, Integer>(n.filelastmodified);
        filelastmodified.putAll(propogated);
        prev = null;
    }
    public void setPrev(Node n) {
        prev = n;
    }
    public void setMessage(String m) {
        message = m;
    }
    public int getID() {
        return id;
    }
    public boolean containsFile(String filename) {
        return filelastmodified.containsKey(filename);
    }
    public File get(String filename) { 
        int dotindex = filename.indexOf(".");
        return new File(".gitlet/" + filename.substring(0, dotindex) 
        + filelastmodified.get(filename).toString() + filename.substring(dotindex));
    }
    public Set<String> fileList() {
        return filelastmodified.keySet();
    }
    public Node prev() {
        return prev;
    }
    public void printDetails() {
        System.out.println("====");
        System.out.println("Commit " + id + ".");
        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(created));
        System.out.println(message);
        System.out.println();
        System.out.println();
    }
    public static boolean modified(Node future, Node previous, String filename) {
        if (!future.containsFile(filename)) {
            return false;
        }
        if (!previous.containsFile(filename)) {
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
    

public class Gitlet {
    HashSet<String> staged;
    HashSet<String> remove;
    HashMap<String, Node> branches;
    HashMap<Integer, Node> commitTree;
    String currentBranch;
    int lastCommit;
    HashMap<String, Integer> modnum;
    HashMap<String, HashSet<Integer>> commitmessages;

    private <E extends Serializable> void saveStateHelper(String save, E objecttosave)
    {
        try {
            File filename = new File(".gitlet/" + save + ".ser");
            FileOutputStream fileOut = new FileOutputStream(filename, false);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(objecttosave);
        } catch (IOException e) {
            String msg = "IOException while saving files." + objecttosave.getClass();
            System.out.println(msg);
        }
        
    }
    
    private void saveState() {
        saveStateHelper("staged", staged);
        saveStateHelper("remove", remove);
        saveStateHelper("branches", branches);
        saveStateHelper("commitTree", commitTree);
        saveStateHelper("currentBranch", currentBranch);
        saveStateHelper("lastCommit", lastCommit);
        saveStateHelper("modnum", modnum);
        saveStateHelper("commitmessages", commitmessages);
    }
    


    private void printState() {
        printStateHelper("staged", staged);
        printStateHelper("remove", remove);
        printStateHelper("branches", branches);
        printStateHelper("commitTree", commitTree);
        printStateHelper("currentBranch", currentBranch);
    }
    
    private void readState() {
        readStateHelper("staged");
        readStateHelper("remove");
        readStateHelper("branches");
        readStateHelper("commitTree");
        readStateHelper("currentBranch");
        readStateHelper("lastCommit");
        readStateHelper("modnum");
        readStateHelper("commitmessages");
    }
    private <T> void readStateHelper(String save) {
        File savedFile = new File(".gitlet/" + save + ".ser");
        if (savedFile.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(savedFile);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                this.getClass().getDeclaredField(save).set(this, (objectIn.readObject()));
            } catch (IOException e) {
                String msg = "IOException while loading myCat.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myCat.";
                System.out.println(msg);
            } catch (ReflectiveOperationException e) {
                System.out.println(e);
            }
        }
    }

    private <T> void printStateHelper(String save, T savedObject) {
        File hello = new File(".gitlet/" + save + ".ser");
        if (hello.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(hello);
                ObjectInputStream objectIn = new ObjectInputStream(fileIn);
                savedObject = (T) (objectIn.readObject());
                System.out.println(hello);
                System.out.println(savedObject);
            } catch (IOException e) {
                String msg = "IOException while loading myCat.";
                System.out.println(msg);
            } catch (ClassNotFoundException e) {
                String msg = "ClassNotFoundException while loading myCat.";
                System.out.println(msg);
            }
        }
    }
    private boolean equalFiles(File a, File b) {
        try {
            FileInputStream a1 = new FileInputStream(a);
            FileInputStream b1 = new FileInputStream(b);
            Scanner sc1 = new Scanner(a1);
            Scanner sc2 = new Scanner(b1);
            while (sc1.hasNext() && sc2.hasNext()) {
                String str1 = sc1.next();
                String str2 = sc2.next();
                if (!str1.equals(str2)) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
    }
    public void add(String file) {
        File fileObject = new File(file);
        if (staged.contains(file)) {
            return;
        }
        if (!fileObject.exists()) {
            System.out.println("File does not exist.");
            return;
        }
        Node currentNode = branches.get(currentBranch);
        if (currentNode.containsFile(file)) { 
            File temp = currentNode.get(file); //returns a File object corresponding to the commit
            if (fileObject.length() == temp.length() && equalFiles(fileObject, temp)) {
                System.out.println("File has not been modified since the last commit.");
                return;
            }
        }
        staged.add(file);
        if (modnum.containsKey(file)) {
            modnum.put(file, modnum.get(file) + 1);
        } else {
            modnum.put(file, 0);
        }
    }
    private void printFields() {
        System.out.println("Staged files = " + staged);
        System.out.println("Files marked for removal = " + remove);
        System.out.println("Current branch : " + currentBranch);
        System.out.println("BranchMap : " + branches);
        System.out.println("CommitTree : " + commitTree);
        System.out.println("LastCommit : " + lastCommit);
        System.out.println("Latest versions of files" + modnum);
    }

    public void commit(String message) {
        if (staged.size() == 0 && remove.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        lastCommit += 1;
        Node currentNode = branches.get(currentBranch);
        branches.put(currentBranch, new Node(lastCommit, message, currentNode, 
            staged, remove, modnum));
        commitTree.put(lastCommit, branches.get(currentBranch));
        if (commitmessages.containsKey(message)) {
            commitmessages.get(message).add(lastCommit);
        } else {
            HashSet<Integer> h = new HashSet<Integer>();
            h.add(lastCommit);
            commitmessages.put(message, h);
        }
        staged = new HashSet<String>();
        remove = new HashSet<String>();
    }
    public void remove(String filename) {
        if (staged.contains(filename)) {
            staged.remove(filename);
            if (modnum.get(filename) == 0) {
                modnum.remove(filename);
            } else {
                modnum.put(filename, modnum.get(filename) - 1);
            }
        } else if (branches.get(currentBranch).containsFile(filename)) {
            remove.add(filename);
        } else {
            System.out.println("No reason to remove the file.");
        }
    }
    public void log() {
        Node currentNode = branches.get(currentBranch);
        Node cycler = currentNode;
        while (cycler != null) {
            cycler.printDetails();
            cycler = cycler.prev();
        }
    }
    public void globallog() {
        int commit = lastCommit;
        while (commit >= 0) {
            commitTree.get(commit).printDetails();
            commit--;
        }
    }

    public void find(String message) {
        if (!commitmessages.containsKey(message)) {
            System.out.println("Found no commit with that message.");
            return;
        }
        HashSet<Integer> h = commitmessages.get(message);
        for (Integer i : h) {
            System.out.println(i);
        }

    }
    public void status() {
        System.out.println("=== Branches ===");
        for (String branch : branches.keySet()) {
            if (branch.equals(currentBranch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Staged Files ===");
        for (String s: staged) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println();
        System.out.println("=== Files Marked for Removal ===");
        for (String r: remove) {
            System.out.println(r);
        }
        System.out.println();
        System.out.println();

    }
    public void checkout(String branch) {
        if (!branches.containsKey(branch)) {
            checkoutfile(branch);
            return; //dont copy files which are the same
        }
        if (branch.equals(currentBranch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Node currentNode = branches.get(branch);
        currentBranch = branch;
        try {
            for (String f : currentNode.fileList()) {
                Files.copy(currentNode.get(f).toPath(), new File(f).toPath(), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public void checkoutfile(String file) {
        if (!branches.get(currentBranch).containsFile(file)) {
            System.out.println("File does not exist in the most recent commit, " 
                + "or no such branch exists.");
            return;
        }
        Node currentNode = branches.get(currentBranch);
        try {
            Files.copy(currentNode.get(file).toPath(), new File(file).toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public void checkoutID(Integer commit, String file) {
        if (!commitTree.containsKey(commit)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        Node currentNode = commitTree.get(commit);
        if (!currentNode.containsFile(file)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        try {
            Files.copy(currentNode.get(file).toPath(), new File(file).toPath(), REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public void branch(String branch) {
        if (branches.containsKey(branch)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        Node currentNode = branches.get(currentBranch);
        branches.put(branch, currentNode);
    }

    public void removebranch(String branch) {
        if (!branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branch);
    }
    public void reset(Integer commit) {
        if (!commitTree.containsKey(commit)) {
            System.out.println("No commit with that id exists.");
            return;
        }
        branches.put(currentBranch, commitTree.get(commit));
        Node currentNode = branches.get(currentBranch);
        try {
            for (String f : currentNode.fileList()) {
                Files.copy(currentNode.get(f).toPath(), new File(f).toPath(), REPLACE_EXISTING);
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public Node findSplitNode(String branch) {
        Set<Integer> branchids = new HashSet<Integer>();
        Node cycler = branches.get(branch);
        while (cycler != null) {
            branchids.add(cycler.getID());
            cycler = cycler.prev();
        }
        cycler = branches.get(currentBranch);
        while (cycler != null) {
            if (branchids.contains(cycler.getID())) {
                return cycler;
            }
            cycler = cycler.prev();
        }
        return null;
    }
    public void merge(String branch) {
        if (!branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (currentBranch.equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        Node splitPoint = findSplitNode(branch);
        Node branchhead = branches.get(branch);
        Node currentHead = branches.get(currentBranch);
        try {
            for (String filename : modnum.keySet()) {
                if (splitPoint.containsFile(filename) || currentHead.containsFile(filename) 
                    || branchhead.containsFile(filename)) {
                    if (Node.modified(branchhead, splitPoint, filename) 
                        && Node.modified(currentHead, splitPoint, filename)) {
                        Files.copy(branchhead.get(filename).toPath(), 
                            new File(filename + ".conflicted").toPath(), REPLACE_EXISTING);
                    } else if (Node.modified(branchhead, splitPoint, filename)) {
                        Files.copy(branchhead.get(filename).toPath(), 
                            new File(filename).toPath(), REPLACE_EXISTING);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    public void rebase(String branch, boolean interactive, BufferedReader br) {
        if (!branches.containsKey(branch)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branch.equals(currentBranch)) {
            System.out.println("Cannot rebase a branch onto itself.");
            return;
        }
        if (findSplitNode(branch) == branches.get(branch)) {
            System.out.println("Already up-to-date.");
            return;
        }
        try {
            if (findSplitNode(branch) == branches.get(currentBranch)) {
                branches.put(currentBranch, branches.get(branch));
            } else {
                Node splitPoint = findSplitNode(branch);
                Node given = branches.get(branch);
                Node current = branches.get(currentBranch);
                HashMap<String, Integer> propogated = new HashMap<String, Integer>();
                for (String file : modnum.keySet()) {
                    if (Node.modified(given, splitPoint, file) 
                        && !Node.modified(current, splitPoint, file)) {
                        if (current.containsFile(file)) {
                            propogated.put(file, given.filelastmodified.get(file));
                        }
                    }
                }
                branches.put(currentBranch, 
                    rebasechain(current, splitPoint, given, br, interactive, propogated));
            }
            Node currentNode = branches.get(currentBranch);
            for (String file : modnum.keySet()) {
                if (currentNode.containsFile(file) 
                    && !equalFiles(currentNode.get(file), new File(file))) {
                    Files.copy(currentNode.get(file).toPath(), 
                        new File(file).toPath(), REPLACE_EXISTING);
                } 
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    } 
    public Node rebasechain(Node current, Node split, Node given, BufferedReader br, 
        boolean interactive, HashMap<String, Integer> propogated) throws IOException {
        Node temp;
        if (interactive) { 
            System.out.println("Currently replaying:");
            current.printDetails();
            System.out.println("Would you like to (c)ontinue, (s)kip this commit," 
                + " or change this commit's (m)essage?");
            char decision = br.readLine().charAt(0);
            while (decision != 'c' && decision != 's' && decision != 'm') {
                System.out.println("Please enter (c), (s) or (m) only");
            }
            if (decision == 's') {
                if (current == branches.get(currentBranch) || current.prev() == split) {
                    while (decision != 'c' && decision != 'm') {
                        System.out.println("Would you like to (c)ontinue, (s)kip this commit," 
                            + " or change this commit's (m)essage?");
                        decision = br.readLine().charAt(0);
                    }
                } else {
                    return rebasechain(current.prev(), split, given, br, interactive, propogated);
                }
            }
            if (decision == 'm') {
                System.out.println("Please enter a new message for this commit.");
                String newmessage = br.readLine(); 
                current.setMessage(newmessage);
            }
        }

        
        temp = new Node(current, propogated);
        if (current.prev() == split) {
            temp.setPrev(given);
        } else {
            temp.setPrev(rebasechain(current.prev(), split, given, br, interactive, propogated));
        }
        lastCommit += 1;
        temp.setID(lastCommit);
        commitTree.put(lastCommit, temp);
        return temp;
    }
    public boolean dangerous(BufferedReader br) throws IOException {
        System.out.println("Warning: The command you entered may alter the files in your" 
            + "working directory. Uncommitted changes may be lost. "
            + "Are you sure you want to continue? (yes/no)");
        String answer = br.readLine();
        if (answer.equals("yes")) {
            return true;
        } else if (answer.equals("no")) {
            return false;
        }
        return dangerous(br);
    }
    public void init() {
        new File(".gitlet").mkdir();
        branches = new HashMap<String, Node>();
        Node firstnode = new Node();
        commitTree = new HashMap<Integer, Node>();
        commitTree.put(0, firstnode);
        currentBranch = "master";
        branches.put(currentBranch, firstnode);
        staged = new HashSet<String>();
        remove = new HashSet<String>();
        modnum = new HashMap<String, Integer>();
        commitmessages = new HashMap<String, HashSet<Integer>>(); 
        HashSet<Integer> h = new HashSet<Integer>();
        h.add(0);
        commitmessages.put("initial commit", h);
        lastCommit = 0;
    }
    
    public static void main(String[] args) {
        Gitlet g = new Gitlet();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String file;
        String branch;
        String message;
        if (args.length >= 1) {
            String command = args[0];
            if (!command.equals("init") && !(new File(".gitlet").exists())) {
                return;
            } 
            g.readState();
            try {
                switch (command) {
                    case "init":
                        if (new File(".gitlet").exists()) {
                            System.out.println("A gitlet version control system already" 
                                + " exists in the current directory.");
                            return;
                        }
                        g.init();
                        break;
                    case "add":
                        file = args[1];
                        g.add(file);
                        break;
                    case "commit":
                        if (args.length == 1) {
                            System.out.println("Please enter a commit message.");
                        }
                        if (args.length > 2) {
                            return;
                        }
                        g.commit(args[1]);
                        break;
                    case "rm":
                        file = args[1];
                        g.remove(file);
                        break;
                    case "log":
                        g.log();
                        break;
                    case "global-log":
                        g.globallog();
                        break;
                    case "find":
                        message = args[1];
                        g.find(message);
                        break;
                    case "status":
                        g.status();
                        break;
                    case "checkout":
                        if (args.length > 3 || !g.dangerous(br)) {
                            return;
                        } 
                        if (args.length == 2) {
                            branch = args[1];
                            g.checkout(branch);
                        } else if (args.length == 3) {
                            Integer commitid = Integer.parseInt(args[1]);
                            file = args[2];
                            g.checkoutID(commitid, file);
                        }
                        break;
                    case "branch":
                        branch = args[1];
                        g.branch(branch);
                        break;
                    case "rm-branch":
                        branch = args[1];
                        g.removebranch(branch);
                        break;
                    case "reset":
                        if (args.length != 2 || !g.dangerous(br)) {
                            return;
                        }
                        Integer id = Integer.parseInt(args[1]);
                        g.reset(id);
                        break;
                    case "merge" :
                        if (args.length != 2 || !g.dangerous(br)) {
                            return;
                        }
                        branch = args[1];
                        g.merge(branch);
                        break;
                    case "rebase" :
                        if (args.length != 2 || !g.dangerous(br)) {
                            return;
                        }
                        branch = args[1];
                        g.rebase(branch, false, br);
                        break;
                    case "i-rebase" :
                        if (args.length != 2 || !g.dangerous(br)) {
                            return;
                        }
                        branch = args[1];
                        g.rebase(branch, true, br);
                        break;
                    default:
                        System.out.println("unrecognized command.");
                }
            } catch (IOException e) {
                System.out.println(e);
            }
            g.saveState();
        }
    }
} //handle case of file stored in folder inside working directory;
