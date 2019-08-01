import com.sun.deploy.panel.ITreeNode;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;
import sun.reflect.generics.tree.Tree;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Main extends JPanel implements ActionListener{
    JFileChooser chooser;
    JScrollPane scroll;
    JTree tree;
    JTextField text;
    FileReader reader;
    DefaultTreeModel treeModel;
    DefaultMutableTreeNode rootNode;
    String[] keyWord ;
    String action;
    //导入的日志路径
    String logPath = "";
    //导出日志的路径
    String writeFilePath = "C:\\Users\\sunpeng\\Documents\\";

    public static String CMD_FILTER = "filter";
    public static String CMD_SHOW = "show";
    public static String CMD_EXPORT = "export";
    public static String CMD_OPEN = "open";


    public Main(){
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1200,720));
        init();
    }

    public void init(){
        JPanel pTop =new JPanel();

        rootNode  = new DefaultMutableTreeNode("Root Node");
        treeModel = new DefaultTreeModel(rootNode);

        tree = new JTree(treeModel);

        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //显示树的节点图标
        tree.setShowsRootHandles(true);

        scroll = new JScrollPane(tree);
        add(scroll,BorderLayout.CENTER);

        ///接受输入的文本框
        text = new JTextField(50);
        chooser = new JFileChooser();

        JButton b_open = new JButton("打开文件..");
        JButton b_filter = new JButton("过滤");
        JButton b_show = new JButton("显示相关");
        JButton b_export = new JButton("导出");

        pTop.add(b_open,BorderLayout.NORTH);
        pTop.add(text,BorderLayout.SOUTH);
        pTop.add(b_filter,BorderLayout.WEST);
        pTop.add(b_show,BorderLayout.CENTER);
        pTop.add(b_export,BorderLayout.EAST);

        b_open.setActionCommand(CMD_OPEN);
        b_filter.setActionCommand(CMD_FILTER);
        b_show.setActionCommand(CMD_SHOW);
        b_export.setActionCommand(CMD_EXPORT);

        b_open.addActionListener(this);
        b_filter.addActionListener(this);
        b_show.addActionListener(this);
        b_export.addActionListener(this);

        add(pTop,BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(text.getText().trim().indexOf("|")>0){
            keyWord = text.getText().trim().split("\\|");
        }
        else {
            keyWord = new String[1];
            keyWord[0] = text.getText().trim();
            //           keyWord = new String[]{text.getText().trim()};
        }
        action = e.getActionCommand();
        switch (action){
            case "filter":
            case "show":
                if(!keyWord[0].equals("")){
                    if(logPath.length()==0){
                        JOptionPane.showMessageDialog(this, "请先打开日志文件", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }else {
                        treeFilter(action,keyWord);
                    }
                }else {
                    JOptionPane.showMessageDialog(this, "请输入关键字", "提示", JOptionPane.INFORMATION_MESSAGE);
                }
                break;
            case "export":
                checkNode(rootNode);
                break;
            case "open":
                readLog();
                break;
             default:
                 System.out.println("default");
        }
    }

    public void treeFilter(String action,String[] keyword) {
        clear();
        String line="";
        DefaultMutableTreeNode node=null,previousNode=null;
        try{
            File file = new File(logPath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            while (   (line=br.readLine())!=null   ){
                if(  line.matches("^\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}\\:\\d{2}.*")  ){
                    node = addObject(rootNode,line,false);
                    if(node.getParent().getIndex(node)>0){
                        previousNode = (DefaultMutableTreeNode) node.getParent().getChildAt( ((DefaultMutableTreeNode)node.getParent()).getIndex(node)-1 );
                        if(action.equals("filter")){
                            if (hasKeyword(previousNode,keyword)) {
                                rootNode.remove(previousNode);
                            }
                        } else if(action.equals("show")){
                            if (!hasKeyword(previousNode,keyword)) {
                                rootNode.remove(previousNode);
                            }
                        }
                    }
                }else {
                    addObject(node,line,false);
                }
            }
            //最后一个节点判断
            if(action.equals("filter")){
                if (hasKeyword(node,keyword)) {
                    rootNode.remove(node);
                }
            } else if(action.equals("show")){
                if (!hasKeyword(node,keyword)) {
                    rootNode.remove(node);
                }
            }
            tree.expandPath(new TreePath(rootNode.getPath()) );
            treeModel.reload();
        }catch (Exception e2){
            System.out.println("ERROR"+e2.toString());
        }
    }

    public Boolean hasKeyword(DefaultMutableTreeNode node,String[] keyword){
        Boolean hasKeyword=false,tmpHasKey=false;
        String reg = "";
        for (String str : keyword) {
            if (str != null) {
                reg = ".*" + "(?i)" + str + ".*";
                tmpHasKey = node.getUserObject().toString().matches(reg);
                hasKeyword = tmpHasKey || hasKeyword;
            }
        }
        return hasKeyword;
    }

    public void readLog(){
        int state = chooser.showOpenDialog(null);
        if(state == JFileChooser.APPROVE_OPTION){
            clear();
            File dir = chooser.getCurrentDirectory();
            String fileName = chooser.getSelectedFile().getName();
            logPath = chooser.getSelectedFile().getAbsolutePath();
            rootNode.setUserObject(fileName);
            File file = new File(dir,fileName);
            try{
                reader = new FileReader(file);
                BufferedReader in =new BufferedReader(reader);
                String s= null;
                DefaultMutableTreeNode node=null;
                while ( (s=in.readLine())!=null){
                    if(  s.matches("^\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}\\:\\d{2}.*")  ){
                        node = addObject(rootNode,s,false);
                    }else{
                        addObject(node,s,false);
                    }
                }
                tree.expandPath(new TreePath(rootNode.getPath()) );
            }catch (Exception e2){
                System.out.println(e2.toString());
            }
        }
    }

    public void checkNode(DefaultMutableTreeNode node){
        Date date = new Date();
        //创建日期格式
        SimpleDateFormat ft = new SimpleDateFormat("yyy-MM-dd-hh-mm-ss");
        //文件名添加后缀
        String writeFileName = ft.format(date)+".log";
        //组合成完整路径
        String writeFileFullPath = writeFilePath + writeFileName;
        File writeFile = new File(writeFileFullPath);
        try{
            if(writeFile.exists()){
                JOptionPane.showMessageDialog(this, "该文件已存在", "提示", JOptionPane.INFORMATION_MESSAGE);
            }else {
                writeFile.createNewFile();
                BufferedWriter out = new BufferedWriter(new FileWriter(writeFile));
                visitNodes(node,out);
                out.close();
            }
        }catch (IOException ioe){
            System.out.println("ERROR"+ioe.toString());
        }
    }


    //遍历节点,并且输出到文件中
    public void visitNodes(DefaultMutableTreeNode node,BufferedWriter writer){
        try{
            if(!node.isRoot()){
                writer.write(node.getUserObject().toString()+"\n");
                writer.flush();
            }
        }catch (Exception e3){
            System.out.println("ERROR"+e3.toString());
        }

        if(node.getChildCount()>0){
            for (Enumeration e = node.children();e.hasMoreElements();){
                DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
                visitNodes(n,writer);
            }
        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("Log");
        frame.setLocation(300,200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //Create and set up the content pane.
        Main newContentPane = new Main();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        //大小自动适应
        frame.pack();
        frame.setVisible(true);
    }
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child, boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child);
        if (parent == null) {
            parent = rootNode;
        }
        //It is key to invoke this on the TreeModel, and NOT DefaultMutableTreeNode
        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    public void clear(){
        rootNode.removeAllChildren();
        treeModel.reload();
    }
}

