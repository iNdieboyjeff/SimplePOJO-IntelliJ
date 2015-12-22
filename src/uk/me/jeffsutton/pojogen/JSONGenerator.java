package uk.me.jeffsutton.pojogen;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * Created by jeff on 10/12/2015.
 */
public class JSONGenerator extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        JSONOptions form = new JSONOptions(project);
        form.setTitle("JSON POJO Generator");
        form.pack();
        form.setLocationRelativeTo(e.getInputEvent().getComponent().getParent());
        form.setResizable(true);
        form.setModal(true);
        form.setVisible(true);
    }
}
