import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.util.io.PersistentHashMapValueStorage;
import me.jeffsutton.SimplePOJO;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Options extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JEditorPane editorPane1;
    private JTextField textField2;
    private JButton browseButton;
    private JTextField textField3;
    private JButton browseButton1;
    private final Project project;

    public Options(final Project project) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileChoicer();
            }
        });
        browseButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileChooserDescriptor Descriptor =  new  FileChooserDescriptor ( true , false , false , false , false , false );
                Descriptor.setShowFileSystemRoots(true);
                Descriptor.withFileFilter(new Condition<VirtualFile>() {
                    @Override
                    public boolean value(VirtualFile virtualFile) {
                        return virtualFile != null && virtualFile.getExtension() != null && virtualFile.getExtension().equalsIgnoreCase("xml");
                    }
                });
                VirtualFile VirtualFile =  FileChooser.chooseFile (Descriptor, project, null );
                if (VirtualFile !=  null ) {
                    textField3.setText(VirtualFile.getCanonicalPath());

                    try {
                        URL oracle = new URL(VirtualFile.getUrl());
                        System.out.println("Using URL: " + oracle.toExternalForm());
                        BufferedReader source = new BufferedReader(
                                new InputStreamReader(oracle.openStream(), StandardCharsets.UTF_8), 4096);
                        String file = "";
                        try {
                            String str;
                            while ((str = source.readLine()) != null) {
                                file += str;
                            }
                        } catch (Exception err) {
                            err.printStackTrace();
                        }
                        editorPane1.setText(file);
                    } catch (Exception err) {
                        err.printStackTrace();
                    }
                }
            }
        });
    }

    private void onOK() {
        if (textField1.getText() == null || textField1.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(textField1, "You must select a destination directory!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        generatePOJO();
        dispose();
    }

    private void onCancel() {

        dispose();
    }

    private void generatePOJO() {
        String packageName = textField1.getText();
        SimplePOJO simple = new SimplePOJO(packageName);
        String source = editorPane1.getText();
        try {
            String generated = simple.generate(new BufferedReader(new StringReader(source)));
            File file = new File(textField2.getText(),simple.getMainClassName() + ".java");
            PrintWriter out = new PrintWriter(file);
            out.write(generated);
            out.close();
            if (file.exists()) {
                VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file);
                if (virtualFile != null) {
                    virtualFile.refresh(false, true);
                    FileEditorManager.getInstance(project).openTextEditor(new OpenFileDescriptor(project, virtualFile), true);
                }
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    private  void  showFileChoicer () {
        FileChooserDescriptor Descriptor =  new  FileChooserDescriptor ( false , true , false , false , false , false );
        Descriptor.setShowFileSystemRoots(true);
        VirtualFile VirtualFile =  FileChooser.chooseFile (Descriptor, project, null );
        if (VirtualFile !=  null ) {
            PsiDirectory Directory =  PsiDirectoryFactory . getInstance (project) . createDirectory (VirtualFile);

            String path = VirtualFile . getCanonicalPath();
            String pkg =  "" ;
            if (path . contains ( "src" ) && path.contains("java")) {
                pkg = path . split ( "java/" ) [ 1 ] . replaceAll ( "/" , "." );
            } else if (path . contains ( "Java" )) {
                pkg = path . split ( "Java/" ) [ 1 ] . replaceAll ( "/" , "." );
            } else  if (path . contains ( "src" )) {
                pkg = path . split ( "src/" ) [ 1 ] . replaceAll ( "/" , "." );
            }

            textField2.setText(VirtualFile.getCanonicalPath());
            textField1.setText(pkg);
        }
    }

}
