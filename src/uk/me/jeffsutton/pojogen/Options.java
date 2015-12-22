package uk.me.jeffsutton.pojogen;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Options extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private org.fife.ui.rsyntaxtextarea.TextEditorPane editorPane1;
    private JTextField textField2;
    private JButton browseButton;
    private JTextField textField3;
    private JButton browseButton1;
    private RTextScrollPane scroll;
    private JProgressBar progressBar1;
    private final Project project;
    private DocumentBuilderFactory dbf;

    public Options(final Project project) {
        this.project = project;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        progressBar1.setVisible(false);

        scroll.setViewportView(editorPane1);
        scroll.setLineNumbersEnabled(true);
        scroll.setFoldIndicatorEnabled(true);

        Gutter gutter = scroll.getGutter();
        gutter.setBackground(new Color(47, 47, 47));
        editorPane1.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
        SyntaxScheme scheme = editorPane1.getSyntaxScheme();
        scheme.getStyle(Token.MARKUP_COMMENT).foreground = Color.decode("#808080");
        scheme.getStyle(Token.MARKUP_CDATA).foreground = Color.decode("#A9B7C6");
        scheme.getStyle(Token.MARKUP_TAG_NAME).foreground = Color.decode("#E8BF6A");
        scheme.getStyle(Token.MARKUP_TAG_DELIMITER).foreground = Color.decode("#E8BF6A");
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE).foreground = Color.decode("#A9B7C6");
        scheme.getStyle(Token.MARKUP_TAG_ATTRIBUTE_VALUE).foreground = Color.decode("#6A8759");
        scheme.getStyle(Token.OPERATOR).foreground = Color.decode("#A9B7C6");
        scheme.getStyle(Token.MARKUP_ENTITY_REFERENCE).foreground = Color.decode("#6D9CBE");

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
                FileChooserDescriptor Descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
                Descriptor.setShowFileSystemRoots(true);
                Descriptor.withFileFilter(new Condition<VirtualFile>() {
                    @Override
                    public boolean value(VirtualFile virtualFile) {
                        return virtualFile != null && virtualFile.getExtension() != null && virtualFile.getExtension().equalsIgnoreCase("xml");
                    }
                });
                VirtualFile VirtualFile = FileChooser.chooseFile(Descriptor, project, null);
                if (VirtualFile != null) {
                    textField3.setText(VirtualFile.getCanonicalPath());
                    new LoadTask(VirtualFile).execute();

                }
            }
        });
    }

    class LoadTask extends SwingWorker<Void, Void> {

        VirtualFile VirtualFile;

        public LoadTask(VirtualFile f) {
            VirtualFile = f;
        }

        @Override
        protected Void doInBackground() throws Exception {
            progressBar1.setVisible(true);
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

                Source xmlInput = new StreamSource(new StringReader(file));
                StringWriter stringWriter = new StringWriter();
                StreamResult xmlOutput = new StreamResult(stringWriter);
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                transformerFactory.setAttribute("indent-number", 4);

                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.transform(xmlInput, xmlOutput);
                String s = xmlOutput.getWriter().toString();

                editorPane1.setText(format(s));
            } catch (Exception err) {
                err.printStackTrace();

                JOptionPane.showMessageDialog(textField1, "There was an error reading the source file.", "Error", JOptionPane.ERROR_MESSAGE);
                progressBar1.setVisible(false);
                return null;

            }
            progressBar1.setVisible(false);
            return null;
        }
    }

    public String format(String unformattedXml) {
        try {
            final Document document = parseXmlFile(unformattedXml);

            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);

            return out.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Document parseXmlFile(String in) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(in));
            return db.parse(is);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void onOK() {
        if (textField1.getText() == null || textField1.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(textField1, "You must select a destination directory!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new Task().execute();
    }

    class Task extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            progressBar1.setVisible(true);
            generatePOJO();
            dispose();
            return null;
        }
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
            File file = new File(textField2.getText(), simple.getMainClassName() + ".java");
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

    private void showFileChoicer() {
        FileChooserDescriptor Descriptor = new FileChooserDescriptor(false, true, false, false, false, false);
        Descriptor.setShowFileSystemRoots(true);
        VirtualFile VirtualFile = FileChooser.chooseFile(Descriptor, project, null);
        if (VirtualFile != null) {
            PsiDirectory Directory = PsiDirectoryFactory.getInstance(project).createDirectory(VirtualFile);

            String path = VirtualFile.getCanonicalPath();
            String pkg = "";
            if (path.contains("src") && path.contains("java")) {
                pkg = path.split("java/")[1].replaceAll("/", ".");
            } else if (path.contains("Java")) {
                pkg = path.split("Java/")[1].replaceAll("/", ".");
            } else if (path.contains("src")) {
                pkg = path.split("src/")[1].replaceAll("/", ".");
            }

            textField2.setText(VirtualFile.getCanonicalPath());
            textField1.setText(pkg);
        }
    }

}
