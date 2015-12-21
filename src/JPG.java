import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.codemodel.JCodeModel;
import org.jsonschema2pojo.Annotator;
import org.jsonschema2pojo.SchemaGenerator;
import org.jsonschema2pojo.SchemaMapper;
import org.jsonschema2pojo.SchemaStore;
import org.jsonschema2pojo.rules.RuleFactory;

import java.io.File;
import java.io.FileWriter;

/**
 * Created by jeff on 15/12/2015.
 */
class JPG {

    public static void generate(String src, File dest, String pkg, String main, Annotator annotator, SourceGenerationConfig config) {
        JCodeModel codeModel = new JCodeModel();
        try {
            System.out.println("Using URL: " + src.toString());

            File f = File.createTempFile("jgen", "json");
            FileWriter fw = new FileWriter(f);
            fw.write(src);
            fw.flush();
            fw.close();

            RuleFactory ruleFactory = new RuleFactory(config, annotator, new SchemaStore());
            SchemaMapper gen = new SchemaMapper(ruleFactory, new SchemaGenerator());
            gen.generate(codeModel, main, pkg, f.toURI().toURL());
            System.out.println("Using file: " + dest.getAbsolutePath() + " " + dest.exists() + " " + dest.isDirectory());
            System.out.println("Using package: " + pkg);
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
