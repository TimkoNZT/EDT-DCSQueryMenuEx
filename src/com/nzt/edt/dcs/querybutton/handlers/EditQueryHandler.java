package com.nzt.edt.dcs.querybutton.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.embedded.EmbeddedEditor;
import org.eclipse.xtext.ui.editor.model.XtextDocument;

import com._1c.g5.v8.dt.core.platform.IV8Project;
import com._1c.g5.v8.dt.dcs.ui.DataCompositionSchemaEditor;
import com._1c.g5.v8.dt.qw.ui.dialogs.QueryTextEditDialog;

public class EditQueryHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        EmbeddedEditor embeddedEditor = findEmbeddedEditor(event);
        if (embeddedEditor == null) {
            Platform.getLog(getClass()).warn("activeEmbeddedEditor не найден");
            return null;
        }

        Shell shell = HandlerUtil.getActiveShell(event);
        if (shell == null || shell.isDisposed()) {
            Platform.getLog(getClass()).warn("Нет активного Shell при вызове редактора запроса");
            return null;
        }

        XtextDocument document = (XtextDocument) embeddedEditor.getDocument();
        String initialText = document.get();

        IProject project = findProject(event);

        QueryTextEditDialog dialog = new QueryTextEditDialog(project, shell, initialText, true, 0, false);
        if (dialog.open() == Dialog.OK) {
            document.set(dialog.getQueryText());
        }

        return null;
    }

    private static EmbeddedEditor findEmbeddedEditor(ExecutionEvent event) {
        Object variable = HandlerUtil.getVariable(event, "activeEmbeddedEditor");
        if (variable instanceof EmbeddedEditor editor)
            return editor;
        return null;
    }

    private static IProject findProject(ExecutionEvent event) {
        IEditorPart editor = HandlerUtil.getActiveEditor(event);
        if (editor == null)
            return null;

        // DataCompositionSchemaEditor — прямой доступ к проекту
        if (editor instanceof DataCompositionSchemaEditor dcsEditor) {
            IV8Project v8Project = dcsEditor.getV8project();
            if (v8Project != null)
                return v8Project.getProject();
        }

        // DtGranularEditor и другие редакторы через адаптер IV8Project
        IV8Project v8Project = editor.getAdapter(IV8Project.class);
        if (v8Project != null)
            return v8Project.getProject();

        Platform.getLog(EditQueryHandler.class).error(
            "Не удалось определить проект: активный редактор не поддерживает IV8Project" +
            " (editor=" + editor.getClass().getName() + ")"
        );

        return null;
    }
}
