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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Main extends JPanel implements ActionListener, ItemListener {
    JFileChooser chooser;
    JScrollPane scroll;
    JTree tree;
    JTree treeBackUp;
    JTextField text;
    JButton button;
    FileReader reader;
    DefaultTreeModel treeModel;
    DefaultMutableTreeNode rootNode;
    JComboBox choice;
    String keyWord;
    String action;
    String logPath = null;
    Boolean changed =false ;

    public static String CMD_FILTER = "filter";
    public static String CMD_SHOW = "show";
    public static String CMD_ALL = "all";
    public static String CMD_OPEN = "open";


    public Main(){
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(1000,600));
        init();
        setVisible(true);
    }

    public void init(){
        JPanel pTop =new JPanel();
        rootNode  = new DefaultMutableTreeNode("Root Node");
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());

        tree = new JTree(treeModel);

        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        scroll = new JScrollPane(tree);
        add(scroll,BorderLayout.CENTER);

        button = new JButton("打开文件..");
        button.setActionCommand(CMD_OPEN);
        button.addActionListener(this);
        pTop.add(button,BorderLayout.NORTH);

        text = new JTextField(10);

        JButton b1 = new JButton("过滤");
        JButton b2 = new JButton("显示相关");
        JButton b3 = new JButton("显示全部内容");
        b1.setActionCommand(CMD_FILTER);
        b2.setActionCommand(CMD_SHOW);
        b3.setActionCommand(CMD_ALL);
        b1.addActionListener(this);
        b2.addActionListener(this);
        b3.addActionListener(this);


        choice = new JComboBox<>();
        choice.addItem("只显示与其相关");
        choice.addItem("过滤掉与其相关");
        choice.addItemListener(this);

        pTop.add(text,BorderLayout.SOUTH);
        pTop.add(b1,BorderLayout.WEST);
        pTop.add(b2,BorderLayout.CENTER);
        pTop.add(b3,BorderLayout.EAST);


        add(pTop,BorderLayout.NORTH);
//        add(button,BorderLayout.NORTH);

 //       text = new JTextArea(26,50);
        chooser = new JFileChooser();

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();
        switch (action){
            case "filter":
                treeFilter(CMD_FILTER);
                break;
            case "show":
                treeFilter(CMD_SHOW);
                break;
            case "all":
                treeFilter(CMD_ALL);
                break;
            case "open":
                readLog();
                break;
             default:
                 System.out.println("default");
        }

    }

    public void treeFilter(String type){
        keyWord = text.getText().trim();
        if(logPath ==null || keyWord.length()==0){
            return;
        }
        String reg = ".*"+"(?i)"+keyWord+".*";
        if(changed == false){
            Enumeration ee=rootNode.children();
            List<Object> list= Collections.list(ee);

            if(type =="filter"){
                for(Object o :list){
                    if(       o.toString().matches(reg)      ){
                        rootNode.remove((MutableTreeNode) o);
                    }
                }
            }else if(type == "show"){
                for(Object o :list){
                    if(        !(o.toString().matches(reg))  ){
                        rootNode.remove((MutableTreeNode) o);
                    }
                }
            }
            treeModel.reload();
            changed = true;
        }else {
            //重新读取文件.并且逐行过滤
            System.out.println("此时需要重新读取文件");
            System.out.println(logPath);
            clear();
            File file = new File(logPath);
            try{
                BufferedReader br = new BufferedReader(new FileReader(file));
                String s= null;
                //tmp为node节点的上一个节点
                DefaultMutableTreeNode node=null,tmp=null,parent=null;
                while ( (s = br.readLine() )!=null){
                    if(  s.matches("^\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}\\:\\d{2}.*")  ){
                        // get the last node of the root
                        // check if the node matches the searching pattern
                        // if matches, do nothing
                        // else remove it from the root
                        node = addObject(rootNode,s,false);
                        parent = (DefaultMutableTreeNode) node.getParent();
                        if(parent.getIndex(node)>0){

                            tmp = (DefaultMutableTreeNode) parent.getChildAt(   parent.getIndex(node)-1   );
                            if(    type =="filter" ){
                                if(  tmp.getUserObject().toString().matches(reg) ){
                                    System.out.println("00");
                                    rootNode.remove(tmp);
                                }
                            }else if(   type == "show"  ){
                                System.out.println(keyWord);
                                if(  !tmp.getUserObject().toString().matches(reg) ){
                                    rootNode.remove(tmp);
                                }
                            }
                        }
                    }else{
                        addObject(node,s,false);
                    }
                }
            }catch (Exception fileNotFountException){
                System.out.println(fileNotFountException.toString());
            }

        }
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
                        // get the last node of the root
                        // check if the node matches the searching pattern
                        // if matches, do nothing
                        // else remove it from the root
                        node = addObject(rootNode,s,false);

                    }else{
                        addObject(node,s,false);
                    }
                }

            }catch (Exception e2){
                System.out.println(e2.toString());
            }
        }
    }

    public void itemStateChanged(ItemEvent e){
        if(e.getStateChange()== ItemEvent.SELECTED){
            keyWord = text.getText().trim();
            if(keyWord.length()==0){
                return;
            }
            Enumeration ee=rootNode.children();
            action = choice.getSelectedItem().toString();
            List<Object> list= Collections.list(ee);
            if(action =="只显示与其相关"){
                for(Object o :list){
                    if(       !o.toString().contains(keyWord)      ){
                        rootNode.remove((MutableTreeNode) o);
                    }
                }
            }else {
                for(Object o :list){
                    if(  (o.toString().contains(keyWord))  ){
                        rootNode.remove((MutableTreeNode) o);
                    }
                }
            }
            treeModel.reload();
        }
    }
    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

            int index = e.getChildIndices()[0];
            node = (DefaultMutableTreeNode)(node.getChildAt(index));

            System.out.println("The user has finished editing the node.");
            System.out.println("New value: " + node.getUserObject());
        }
        public void treeNodesInserted(TreeModelEvent e) {
        }
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }

    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("DynamicTreeDemo");
        frame.setLocation(400,300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        String testStr = ".*"+"(?i)"+"org"+".*"  ;
        String str ="2019-07-18 00:00:00 [ERROR] [org.springframework.scheduling.quartz.SchedulerFactoryBean#0_Worker-5] [com.virtual.plat.server.task.SyncCrlJobController::start] JobCrlSync Start." ;
        System.out.println(str.matches(".*(?i)org.*")  );


        //Create and set up the content pane.
        Main newContentPane = new Main();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
//        Main m = new Main();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        /*
        JFrame jf = new JFrame("测试窗口");
        jf.setSize(300, 300);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());

        // 创建根节点

        // 使用根节点创建树组件
        JTree tree = new JTree(rootNode);

        // 设置树显示根节点句柄
        tree.setShowsRootHandles(true);

        // 设置树节点可编辑
        tree.setEditable(true);

        // 设置节点选中监听器
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                System.out.println("当前被选中的节点: " + e.getPath());
            }
        });

        // 创建滚动面板，包裹树（因为树节点展开后可能需要很大的空间来显示，所以需要用一个滚动面板来包裹）
        JScrollPane scrollPane = new JScrollPane(tree);

        // 添加滚动面板到那内容面板
        panel.add(scrollPane, BorderLayout.CENTER);

        // 设置窗口内容面板并显示
        jf.setContentPane(panel);
        jf.setVisible(true);

        */
    }

    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
            parentNode = rootNode;
        return addObject(parentNode, child, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
        return addObject(parent, child, false);
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

    public void resolveFiles(String action,String keyWord){
        Boolean containStatus;
        int state = chooser.showOpenDialog(null);
        if(state == JFileChooser.APPROVE_OPTION){
            File dir = chooser.getCurrentDirectory();
            String fileName = chooser.getSelectedFile().getName();
            File file = new File(dir,fileName);
            try{
                reader = new FileReader(file);
                BufferedReader in =new BufferedReader(reader);
                String s= null;
                DefaultMutableTreeNode node=null;
                while ( (s=in.readLine())!=null){
                    if (action == "只显示与其相关"){

                    }
                    if(s.matches("^\\d{4}\\-\\d{2}\\-\\d{2}\\s\\d{2}\\:\\d{2}\\:\\d{2}.*")&&s.contains(keyWord) ){
                        node = addObject(rootNode,s,false);
                    }else{
                        //              node.setUserObject(node.getUserObject()+"\n"+s);
                        addObject(node,s,false);
                    }
                }
            }catch (Exception e2){
                System.out.println(e2.toString());
            }
        }
    }

}

