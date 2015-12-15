import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

/**
 * Created by jeff on 15/12/2015.
 */
class JPG {

    public static void generate(File src, File dest, String pkg, String main, Annotator annotator, SourceGenerationConfig config) {
        JCodeModel codeModel = new JCodeModel();
        try {
            System.out.println("Using URL: " + src.toString());
        BufferedReader source = new BufferedReader(
                new FileReader(src), 4096);
        String file = "";
        try {
            String str;
            while ((str = source.readLine()) != null) {
                file += str;
            }
        } catch (Exception err) {
            err.printStackTrace();
        }
        System.out.println(file);
       RuleFactory ruleFactory = new RuleFactory(config, annotator, new SchemaStore());
       SchemaMapper gen =  new SchemaMapper(ruleFactory, new SchemaGenerator());
       gen.generate(codeModel, main, pkg, src.getAbsoluteFile().toURI().toURL());

            System.out.println("Using file: " + dest.getAbsolutePath() + " " + dest.exists() + " " + dest.isDirectory());
        if (!dest.exists()) {
            dest.mkdir();
        }

        codeModel.build(dest);

            VirtualFile virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(dest);
            if (virtualFile != null) {
                virtualFile.refresh(false, true);

            }

    } catch (Exception err) {
        err.printStackTrace();
    }
    }
}
