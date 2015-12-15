import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;


/**
 * Created by jeff on 09/12/2015.
 */
public class Generator extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        Project project = anActionEvent.getProject();
        Options form = new Options(project);
        form.setTitle("SimpleXML POJO Generator");
        form.pack();
        form.setLocationRelativeTo(anActionEvent.getInputEvent().getComponent());
        form.setResizable(false);
        form.setModal(true);
        form.setVisible(true);
    }
}
